package com.alirizvi.weather.;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alirizvi.weather.data.WeatherContract;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER = 0;
    private static final String LOCATION_KEY = "location_key";

    private ShareActionProvider mShareActionProvider;

    private TextView mDateTv;
    private TextView mShortDescTv;
    private TextView mMinTempTv;
    private TextView mMaxTempTv;
    private TextView mHumidityTv;
    private TextView mWindSpeedTv;
    private TextView mPressureTv;
    private ImageView mWeatherConditionArt;

    private String mForecastDate;
    private String mLocation;

    public static Fragment newInstance(String date) {
        Fragment f = new DetailFragment();

        Bundle args = new Bundle();
        args.putString(DetailActivity.EXTRA_FORECAST_DATE, date);

        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mForecastDate);

        mShareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(getActivity(),
                    SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mLocation = Utility.getPreferredLocation(getActivity());
        mForecastDate = getArguments().getString(DetailActivity.EXTRA_FORECAST_DATE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }

        /*Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(DetailActivity.EXTRA_FORECAST_DATE)) {
            getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        }*/
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(LOCATION_KEY, mLocation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_detail, parent, false);
        mDateTv = (TextView)layout.findViewById(R.id.date_tv);
        mShortDescTv = (TextView)layout.findViewById(R.id.short_desc_tv);
        mMinTempTv = (TextView)layout.findViewById(R.id.min_temp_tv);
        mMaxTempTv = (TextView)layout.findViewById(R.id.max_temp_tv);

        mHumidityTv = (TextView)layout.findViewById(R.id.humidity_tv);
        mWindSpeedTv = (TextView)layout.findViewById(R.id.wind_speed_tv);
        mPressureTv = (TextView)layout.findViewById(R.id.pressure_tv);

        mWeatherConditionArt = (ImageView)layout.findViewById(R.id.forecast_img);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        /*Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(DetailActivity.EXTRA_FORECAST_DATE) &&
                mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }*/
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                mLocation, mForecastDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                ForecastFragment.FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int dateColIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT);
            String date = Utility.formatDate(cursor.getString(dateColIndex));

            int shortDescColIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
            String shortDesc = cursor.getString(shortDescColIndex);

            boolean isMetric = Utility.isMetric(getActivity());
            int maxColIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
            String max = Utility.formatTemperature(getActivity(), cursor.getDouble(maxColIndex), isMetric);

            int minColIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
            String min = Utility.formatTemperature(getActivity(), cursor.getDouble(minColIndex), isMetric);

            int humidityColIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY);
            String humidity = getActivity().getString(R.string.format_humidity, cursor.getDouble(humidityColIndex));

            int windSpeedColIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED);
            int windDegreesColIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES);
            String windSpeed = Utility.getFormattedWind(getActivity(), cursor.getFloat(windSpeedColIndex), cursor.getFloat(windDegreesColIndex));

            int pressureColIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE);
            String pressure = getActivity().getString(R.string.format_pressure, cursor.getDouble(pressureColIndex));

            int weatherIdColIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID);
            int weatherId = cursor.getInt(weatherIdColIndex);

            mDateTv.setText(date);
            mShortDescTv.setText(shortDesc);
            mMinTempTv.setText(min);
            mMaxTempTv.setText(max);
            mHumidityTv.setText(humidity);
            mWindSpeedTv.setText(windSpeed);
            mPressureTv.setText(pressure);
            mWeatherConditionArt.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
}
