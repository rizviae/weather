package com.xtrange.apps.sunshine.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;


public class DetailActivity extends ActionBarActivity {

    public static final String EXTRA_FORECAST_DATE = "extraForecastDate";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, DetailFragment.newInstance(getIntent().getStringExtra(EXTRA_FORECAST_DATE)))
                    .commit();
        }
    }


}
