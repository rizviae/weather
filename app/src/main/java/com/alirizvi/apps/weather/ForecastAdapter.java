package com.xtrange.apps.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    private boolean mUseTodayLayout;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 && mUseTodayLayout ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = viewType == VIEW_TYPE_TODAY ? R.layout.today_forecast_list_item : R.layout.forecast_list_item;

        View layout = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(layout);
        layout.setTag(viewHolder);

        return layout;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        // Use placeholder image for now
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        ImageView iconView = viewHolder.iconView;

        int artResourceId = getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY ? Utility.getArtResourceForWeatherCondition(cursor.getInt(ForecastFragment.COL_WEATHER_API_ID)) :
                Utility.getIconResourceForWeatherCondition(cursor.getInt(ForecastFragment.COL_WEATHER_API_ID));
        iconView.setImageResource(artResourceId);

        // Read date from cursor
        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        TextView dateView = viewHolder.dateView;
        dateView.setText(Utility.getFriendlyDayString(context, dateString));

        // Read weather forecast from cursor
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        // Find TextView and set weather forecast on it
        TextView descriptionView = viewHolder.descriptionView;
        descriptionView.setText(description);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView maxTempTv = viewHolder.highTempView;
        maxTempTv.setText(Utility.formatTemperature(mContext, high, isMetric));

        // Read low temperature from cursor
        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView minTempTv = viewHolder.lowTempView;
        minTempTv.setText(Utility.formatTemperature(mContext, low, isMetric));
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.mUseTodayLayout = useTodayLayout;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.forecast_img);
            dateView = (TextView) view.findViewById(R.id.forecast_date_tv);
            descriptionView = (TextView) view.findViewById(R.id.short_desc_tv);
            highTempView = (TextView) view.findViewById(R.id.max_temp_tv);
            lowTempView = (TextView) view.findViewById(R.id.min_temp_tv);
        }
    }
}