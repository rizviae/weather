package com.alirizvi.weather.;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.alirizvi.weather.WeatherContract;
import com.alirizvi.weather.SunshineSyncAdapter;

import java.util.Date;


public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = ForecastFragment.class.getSimpleName();

    private static final String SELECTED_ITEM = "selectedItem";

    private Callback mCallback;
    private String mLocation;
    private static final int FORECAST_LOADER = 0;

    public static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_API_ID = 10;
    public static final int COL_LOCATION_LAT = 11;
    public static final int COL_LOCATION_LONG = 12;

    private ForecastAdapter mForecastAdapter;
    private ListView mForecastListview;
    private boolean mUseTodayLayout;

    private int mSelectedItemPosition = ListView.INVALID_POSITION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback)activity;
        } catch (ClassCastException e) {
            Log.e(TAG, activity.getClass().getSimpleName() + " must implement " + Callback.class.getSimpleName());
        }
    }

    public void setUseTodayLayout(boolean b) {
        mUseTodayLayout = b;

        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(b);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecast_fragment, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SELECTED_ITEM, mForecastListview.getSelectedItemPosition());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_loc:
                openPreferredLocation();
                break;
        }

        return true;
    }

    private void openPreferredLocation() {

        Cursor cursor = mForecastAdapter.getCursor();
        String posLat = cursor.getString(COL_LOCATION_LAT);
        String posLong = cursor.getString(COL_LOCATION_LONG);
        Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(geoLocation);

        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.no_app_handle, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastListview = (ListView)rootView.findViewById(R.id.listview_forecast);

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        mForecastListview.setAdapter(mForecastAdapter);
        mForecastListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = ((ForecastAdapter)parent.getAdapter()).getCursor();
                if (cursor.moveToPosition(position)) {
                    int dateColIndex = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT);


                    mCallback.onItemSelected(cursor.getString(dateColIndex));
                }
                mSelectedItemPosition = position;
            }
        });

        if (savedInstanceState != null) {
            mSelectedItemPosition = savedInstanceState.getInt(SELECTED_ITEM);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    private void updateWeather() {
        String zipCode = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));


        SunshineSyncAdapter.syncImmediately(getActivity());
    }


    public static Account CreateSyncAccount(Context context) {

        Account newAccount = new Account(
                "dummyAccount", context.getString(R.string.sync_account_type));

        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        Context.ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(newAccount, null, null)) {

        } else {

        }

        return newAccount;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String startDate = WeatherContract.getDbDateString(new Date());


        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation, startDate);

        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mForecastAdapter.swapCursor(cursor);
        if (mSelectedItemPosition != ListView.INVALID_POSITION)
            mForecastListview.setSelection(mSelectedItemPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mForecastAdapter.swapCursor(null);
    }


    public interface Callback {

        public void onItemSelected(String date);
    }
}
