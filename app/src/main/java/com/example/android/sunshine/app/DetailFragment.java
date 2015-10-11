package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Created by Rachel on 9/30/2015.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private final String URI_KEY = "intentKeyForecast";
    private final String FORECAST_SHARE_HASTAG = "#SunshineApp";
    private ShareActionProvider mShareActionProvider;
    private String uriFromMain;
    String weatherDetails = "";
    final int LOADER_ID = 1;
    private View rootView;

    public DetailFragment() {
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = this.getArguments();
        uriFromMain = bundle.getString(URI_KEY);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = new ShareActionProvider(getActivity());
        MenuItemCompat.setActionProvider(item, mShareActionProvider);
        setShareIntent();
        //return super.onCreateOptionsMenu(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        updateDetails();

        getLoaderManager().initLoader(LOADER_ID, null, this);
        return rootView;
    }

    public void updateDetails() {
        TextView detailText = (TextView) rootView.findViewById(R.id.detail_text);
        detailText.setText(weatherDetails);
    }

    //Helper function to update the intent of the Shared Action provider.  This is neeeded
    //so that the intent can be updated whenever the user changes settings
    private void setShareIntent(){
        // Create the share Intent
        String shareText = weatherDetails + FORECAST_SHARE_HASTAG;
//        Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity()).setType("text/plain").
//                setText(shareText).getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        // Set the share Intent
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
        else
        {
            Log.d(LOG_TAG, "Share Action Provider is null.");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        Uri weatherWithDate = Uri.parse(uriFromMain);

        return new CursorLoader(getActivity(),
                weatherWithDate,
                WeatherContract.FORECAST_COLUMNS,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();

        weatherDetails = Utility.formatDate(data.getLong(WeatherContract.COL_WEATHER_DATE)) + "-" +
                            data.getString(WeatherContract.COL_WEATHER_DESC) + "-" +
                            data.getLong(WeatherContract.COL_WEATHER_MAX_TEMP) + "/" +
                            data.getLong(WeatherContract.COL_WEATHER_MIN_TEMP);
        updateDetails();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
