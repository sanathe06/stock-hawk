package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Binder;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Quote;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.StockDetailsActivity;

/**
 * Created by sanathnandasiri on 8/19/16.
 */

public class WidgetService extends RemoteViewsService {
    private static final String TAG = WidgetService.class.getSimpleName();
    private Cursor mCursor;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            @Override
            public void onCreate() {
                Log.d(TAG, "onCreate() called");
            }

            @Override
            public void onDataSetChanged() {
                Log.d(TAG, "onDataSetChanged()");
                loadData();
            }

            private void loadData() {
                if (mCursor != null) {
                    mCursor.close();
                }

                final long token = Binder.clearCallingIdentity();

                mCursor = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(token);
            }

            @Override
            public void onDestroy() {
                Log.d(TAG, "onDestroy()");
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
            }

            @Override
            public int getCount() {
                Log.d(TAG, "getCount( " + mCursor.getCount() + " )");
                return mCursor == null ? 0 : mCursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                Log.d(TAG, "getViewAt() called with: position = [" + position + "]");
                if (position == AdapterView.INVALID_POSITION || mCursor == null || !mCursor.moveToPosition(position)) {
                    return null;
                }
                mCursor.moveToPosition(position);
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);
                String symbol = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));
                String change = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE));
                String bidPrice = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE));
                int is_up = mCursor.getInt(mCursor.getColumnIndex(QuoteColumns.ISUP));
                Log.d(TAG, String.format("data symbol %s change %s isUp %s", symbol, change, is_up));

                views.setTextViewText(R.id.widget_stock_symbol, symbol);
                views.setTextViewText(R.id.widget_change, change);
                views.setTextViewText(R.id.bid_price, bidPrice);

                if (is_up == 1) {
                    views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                //lunching details activity for stock
                final Intent intent = new Intent();
                intent.putExtra(StockDetailsActivity.INTENT_ARGS_STOCK_SYMBOL, symbol);
                views.setOnClickFillInIntent(R.id.widget_item_root,intent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                Log.d(TAG, "getItemId() called with: position = [" + position + "]");
                if (mCursor != null && mCursor.moveToPosition(position)) {
                    return mCursor.getLong(mCursor.getColumnIndex(QuoteColumns._ID));
                }
                Log.d(TAG, "getItemId() returned: " + position);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
