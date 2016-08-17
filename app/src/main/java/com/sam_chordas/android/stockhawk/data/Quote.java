package com.sam_chordas.android.stockhawk.data;

import android.database.Cursor;

import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.Date;

/**
 * Created by sanathnandasiri on 8/13/16.
 */

public class Quote {
    public int id;
    public String symbol;
    public double bidPrice;
    public String percentChange;
    public double change;
    public Date created;
    public boolean isUp;
    public boolean isCurrent;

    public Quote(int id, String symbol, double bidPrice, String percentChange, double change,
                 Date created, boolean isUp, boolean isCurrent) {
        this.id = id;
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.percentChange = percentChange;
        this.change = change;
        this.created = created;
        this.isUp = isUp;
        this.isCurrent = isCurrent;
    }

    public static Quote getQuote(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        long milliseconds = cursor.getLong(cursor.getColumnIndex(QuoteColumns.CREATED));
        Date createdDate = milliseconds > 0 ? new Date(milliseconds) : null;
        return new Quote(
                cursor.getInt(cursor.getColumnIndex(QuoteColumns._ID)),
                cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)),
                cursor.getDouble(cursor.getColumnIndex(QuoteColumns.BIDPRICE)),
                cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)),
                cursor.getDouble(cursor.getColumnIndex(QuoteColumns.CHANGE)),
                createdDate,
                cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP)) == 1,
                cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISCURRENT)) == 1
        );

    }

    @Override
    public String toString() {
        return "Quote{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", bidPrice=" + bidPrice +
                ", percentChange='" + percentChange + '\'' +
                ", change=" + change +
                ", created=" + Utils.format(created) +
                ", isUp=" + isUp +
                ", isCurrent=" + isCurrent +
                '}';
    }
}
