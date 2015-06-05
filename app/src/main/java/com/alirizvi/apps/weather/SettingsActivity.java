package com.xtrange.apps.sunshine.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import com.xtrange.apps.sunshine.app.data.WeatherContract;
import com.xtrange.apps.sunshine.app.sync.SunshineSyncAdapter;


public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    private boolean mIsBindingPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));

    }


    private void bindPreferenceSummaryToValue(Preference preference) {
        mIsBindingPreference = true;

        preference.setOnPreferenceChangeListener(this);


        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));

        mIsBindingPreference = false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (!mIsBindingPreference) {
            if (preference.getKey().equals(getString(R.string.pref_location_key))) {
                SunshineSyncAdapter.syncImmediately(this);
            } else if (preference.getKey().equals(getString(R.string.pref_units_key))) {
                getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
            }
        }

        if (preference instanceof ListPreference) {

            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {

            preference.setSummary(stringValue);
        }
        return true;
    }

}