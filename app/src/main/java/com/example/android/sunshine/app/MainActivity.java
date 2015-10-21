package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;

import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

public class MainActivity extends ActionBarActivity implements  ForecastFragment.Callback{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private String location;
    private boolean twoPane;

    private final String URI_KEY = "intentKeyForecast";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        location = Utility.getPreferredLocation(this);

        SunshineSyncAdapter.initializeSyncAdapter(this);

        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            twoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                DetailFragment detailFragment = new DetailFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.weather_detail_container, detailFragment, DETAILFRAGMENT_TAG)
                        .commit();
            }

            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            forecastFragment.foreCastEntriesAdapter.setUseTodayLayout(false);
        } else {
            twoPane = false;
        }
    }

    public void onResume(){
        super.onResume();

        String preferredLocation = Utility.getPreferredLocation( this );
        ForecastFragment ff = null;
        // update the location in our second pane using the fragment manager
        if (preferredLocation != null && !preferredLocation.equals(location)) {
            ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        }
        if ( null != ff ) {
            ff.onLocationChanged();
        }
        DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
        if ( null != df ) {
            df.onLocationChanged(preferredLocation);
        }
        location = preferredLocation;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (twoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Log.v(LOG_TAG, "Two pane listener was called.");

            Bundle bundle = new Bundle();
            bundle.putParcelable(DetailFragment.URI_KEY, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
