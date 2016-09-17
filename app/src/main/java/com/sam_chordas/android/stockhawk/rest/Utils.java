package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList<ContentProviderOperation> quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                String created = jsonObject.getString("created");
                long createdTime = parseStringToDate(created).getTime();
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    ContentProviderOperation batchOperation = buildBatchOperation(jsonObject, createdTime);
                    if (batchOperation != null) {
                        batchOperations.add(batchOperation);
                    }
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            ContentProviderOperation batchOperation = buildBatchOperation(jsonObject, createdTime);
                            if (batchOperation != null) {
                                batchOperations.add(batchOperation);
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) throws NumberFormatException {
        bidPrice = String.format(Locale.getDefault(),"%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format(Locale.getDefault(),"%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject, long createdTime) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            String symbol = jsonObject.getString("symbol");
            String bid = jsonObject.getString("Bid");
            String changeinPercent = jsonObject.getString("ChangeinPercent");
            if (change.equals("null") || symbol.equals("null") || bid.equals("null") || changeinPercent.equals("null"))
                return null;
            builder.withValue(QuoteColumns.SYMBOL, symbol);
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(bid));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    changeinPercent, true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

            builder.withValue(QuoteColumns.CREATED, createdTime);

        } catch (JSONException | NumberFormatException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return builder.build();
    }

    public static Date parseStringToDate(String created) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.getDefault());
        Date date = null;
        try {
            date = format.parse(created.replace("Z", "UTC"));
        } catch (ParseException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return date;
    }

    public static String convertDateToString(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date).replace("UTC", "Z");
    }

    public static String format(Date created) {
        DateFormat format = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
        return format.format(created);
    }

    public static long trim(Date dateWithTime) {
        Calendar cal = Calendar.getInstance(); // locale-specific
        cal.setTime(dateWithTime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
