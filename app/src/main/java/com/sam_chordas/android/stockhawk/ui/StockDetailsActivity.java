package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.ElasticEase;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Quote;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import butterknife.BindView;

public class StockDetailsActivity extends BaseActivity {
    public static final String TAG = StockDetailsActivity.class.getSimpleName();

    public static final String INTENT_ARGS_STOCK_SYMBOL = "intent_args_stock_symbol";
    public String mSymbol;


    @BindView(R.id.textViewStock)
    TextView textViewStock;
    @BindView(R.id.textViewDate)
    TextView textViewDate;
    @BindView(R.id.textViewBidPrice)
    TextView textViewBidPrice;
    @BindView(R.id.textViewChange)
    TextView textViewChange;
    @BindView(R.id.textViewChangePercentage)
    TextView textViewChangePercentage;
    @BindView(R.id.chartQuoteHistory)
    LineChartView lineChartQuoteHistory;
    private Typeface robotoLight;

    ArrayList<Quote> filteredQuote = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().show();
        getSupportActionBar().setHomeButtonEnabled(true);
        mSymbol = getIntent().getStringExtra(INTENT_ARGS_STOCK_SYMBOL);
        robotoLight = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");
        setupUi();
        setStock();

        Cursor cursorStockChanged = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.CREATED, QuoteColumns.ISUP, QuoteColumns.ISCURRENT},
                QuoteColumns.SYMBOL + "= ?",
                new String[]{mSymbol},
                null
        );

        filteredQuote = getQuotesToShow(cursorStockChanged);

        Log.d(TAG, "Quotes : " + filteredQuote);
        showChart(filteredQuote);
        showDetails(filteredQuote.get(filteredQuote.size() - 1));
    }

    private void setupUi() {
        textViewStock.setTypeface(robotoLight);
        textViewChange.setTypeface(robotoLight);
        textViewChangePercentage.setTypeface(robotoLight);
        textViewBidPrice.setTypeface(robotoLight);
        textViewDate.setTypeface(robotoLight);
    }

    private void setStock() {
        textViewStock.setText(mSymbol);
        textViewStock.setContentDescription(String.format(Locale.getDefault(),getString(R.string.content_description_chart),mSymbol));
    }

    private void showDetails(Quote quote) {
        int sdk = Build.VERSION.SDK_INT;
        if (quote.isUp) {
            Drawable drawableGreen = ContextCompat.getDrawable(this, R.drawable.percent_change_pill_green);
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                textViewChange.setBackgroundDrawable(drawableGreen);
                textViewChangePercentage.setBackgroundDrawable(drawableGreen);
            } else {
                textViewChange.setBackground(drawableGreen);
                textViewChangePercentage.setBackground(drawableGreen);
            }
        } else {
            Drawable drawableRed = ContextCompat.getDrawable(this, R.drawable.percent_change_pill_red);

            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                textViewChange.setBackgroundDrawable(drawableRed);
                textViewChangePercentage.setBackgroundDrawable(drawableRed);
            } else {
                textViewChange.setBackground(drawableRed);
                textViewChangePercentage.setBackground(drawableRed);
            }
        }
        textViewChangePercentage.setText(quote.percentChange);
        textViewChange.setText(String.valueOf(quote.change));
        textViewBidPrice.setText(String.valueOf(quote.bidPrice));
        textViewDate.setText(Utils.format(quote.created));

    }

    private void showChart(final ArrayList<Quote> filteredQuote) {
        final ArrayList<Double> entries = new ArrayList<>();
        LineSet lineSet = new LineSet();
        for (Quote quote : filteredQuote) {
            lineSet.addPoint(new Point(Utils.format(quote.created), (float) quote.bidPrice));
            entries.add(quote.bidPrice);
        }


        lineSet.setColor(ContextCompat.getColor(this, R.color.material_blue_500));
        lineSet.setDotsColor(ContextCompat.getColor(this, R.color.material_blue_700));
        lineSet.setThickness(getResources().getDimension(R.dimen.chart_line_thickness));
        lineSet.setDotsRadius(getResources().getDimension(R.dimen.chart_dot_thickness));
        int colorLight = ContextCompat.getColor(this, R.color.primary_text_dark);
        Collections.sort(entries);
        //find max and min value for series
        double max = entries.get(entries.size() - 1) + 1;
        double min = entries.get(0) - 1;
        lineChartQuoteHistory.setAxisBorderValues((int) min, (int) max);
        Paint paint = new Paint();
        paint.setColor(colorLight);
        lineChartQuoteHistory.setGrid(ChartView.GridType.FULL, paint);
        lineChartQuoteHistory.setAxisColor(colorLight);
        lineChartQuoteHistory.setLabelsColor(colorLight);
        lineChartQuoteHistory.addData(lineSet);
        lineChartQuoteHistory.setTypeface(robotoLight);

        lineChartQuoteHistory.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int setIndex, int entryIndex, Rect entryRect) {
                if (filteredQuote != null && filteredQuote.size() > 0) {
                    showDetails(filteredQuote.get(entryIndex));
                }
            }
        });

        Animation anim = new Animation(500);
        anim.setEasing(new ElasticEase());
        lineChartQuoteHistory.show(anim);
    }

    @NonNull
    private ArrayList<Quote> getQuotesToShow(Cursor cursorStockChanged) {
        ArrayList<Long> datesLongShowings = new ArrayList<>();
        ArrayList<Quote> quotes = new ArrayList<>();

        if (cursorStockChanged != null && cursorStockChanged.moveToFirst()) {
            do {
                Quote quote = Quote.getQuote(cursorStockChanged);
                quotes.add(quote);
                datesLongShowings.add(quote.created.getTime());
            } while (cursorStockChanged.moveToNext());
        }
        Comparator<Long> comparatorReversLong = new Comparator<Long>() {
            @Override
            public int compare(Long lhs, Long rhs) {
                return lhs < rhs ? 1 : lhs == rhs ? 0 : -1;
            }
        };
        Collections.sort(datesLongShowings, comparatorReversLong);
        Set<String> datesShowings = new HashSet<>(7);
        ArrayList<Long> datesLongShowingsFiltered = new ArrayList<>();
        for (Long datesLongShowing : datesLongShowings) {
            //only grab last 7 day or less
            Date created = new Date(Utils.trim(new Date(datesLongShowing)));
            if (datesShowings.add(Utils.format(created))) {
                datesLongShowingsFiltered.add(created.getTime());
            }
            if (datesShowings.size() >= 7) break;
        }
        Collections.sort(datesLongShowingsFiltered);
        datesShowings.clear();
        ArrayList<String> datesShowingsSorted = new ArrayList<>(7);
        for (Long aLong : datesLongShowingsFiltered) {
            datesShowingsSorted.add(Utils.format(new Date(Utils.trim(new Date(aLong)))));
        }

        ArrayList<Quote> filteredQuote = new ArrayList<>();
        for (String datesShowing : datesShowingsSorted) {
            ArrayList<Quote> subListForDate = new ArrayList<>();
            for (Quote quote : quotes) {
                if (datesShowing.equalsIgnoreCase(Utils.format(quote.created))) {
                    subListForDate.add(quote);
                }
            }
            if (subListForDate.size() > 0) {
                Collections.sort(subListForDate, new Comparator<Quote>() {
                    @Override
                    public int compare(Quote lhs, Quote rhs) {
                        return lhs.bidPrice < rhs.bidPrice ? 1 : lhs.bidPrice == rhs.bidPrice ? 0 : -1;
                    }
                });

                //check for is current
                Quote quoteSelected = getCurrentQuoteIf(subListForDate);
                filteredQuote.add(quoteSelected != null ? quoteSelected : subListForDate.get(0));
            }
        }
        return filteredQuote;
    }

    private Quote getCurrentQuoteIf(ArrayList<Quote> subListForDate) {
        for (Quote quote : subListForDate) {
            if (quote.isCurrent) {
                return quote;
            }
        }
        return null;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_stock_details;
    }
}
