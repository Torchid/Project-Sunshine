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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * Created by Rachel on 9/30/2015.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    public static final String URI_KEY = "intentKeyForecast";
    private final String FORECAST_SHARE_HASTAG = "#SunshineApp";
    private ShareActionProvider mShareActionProvider;
    private Uri uriFromMain;
    String weatherDetails = "";
    final int LOADER_ID = 1;
    private View rootView;

    private static final int DETAIL_LOADER = 0;

    private TextView highView;
    private TextView lowView;
    private ImageView iconView;
    private TextView descriptionView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;

    public DetailFragment() {
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = this.getArguments();
        if(bundle != null)
            uriFromMain = bundle.getParcelable(URI_KEY);
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

        highView = (TextView) rootView.findViewById(R.id.detail_high);
        lowView = (TextView) rootView.findViewById(R.id.detail_low);
        iconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        descriptionView = (TextView) rootView.findViewById(R.id.detail_description);
        humidityView = (TextView) rootView.findViewById(R.id.detail_humidity);
        windView = (TextView) rootView.findViewById(R.id.detail_wind);
        pressureView = (TextView) rootView.findViewById(R.id.detail_pressure);

        getLoaderManager().initLoader(LOADER_ID, null, this);
        return rootView;
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

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        if (null != uriFromMain) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uriFromMain);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            uriFromMain = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Intent intent = getActivity().getIntent();
        if (intent == null || intent.getData() == null) {
            return null;
        }

        String locationSetting = Utility.getPreferredLocation(getActivity());
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        Uri weatherWithDate = uriFromMain;

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

        TextView dayView = (TextView) rootView.findViewById(R.id.detail_day);
        dayView.setText(Utility.getFriendlyDayString(getActivity(), data.getLong(WeatherContract.COL_WEATHER_DATE)));

        TextView dateView = (TextView) rootView.findViewById(R.id.detail_date);
        dateView.setText(Utility.formatDate(data.getLong(WeatherContract.COL_WEATHER_DATE)));

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(getActivity());

        highView.setText(Utility.formatTemperature(getActivity(), data.getLong(WeatherContract.COL_WEATHER_MAX_TEMP), isMetric));

        lowView.setText(Utility.formatTemperature(getActivity(), data.getLong(WeatherContract.COL_WEATHER_MIN_TEMP), isMetric));

        iconView.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(WeatherContract.COL_WEATHER_CONDITION_ID)));

        descriptionView.setText(data.getString(WeatherContract.COL_WEATHER_DESC));

        humidityView.setText(getActivity().getString(R.string.format_humidity, data.getFloat(WeatherContract.COL_WEATHER_HUMIDITY)));

        windView.setText(Utility.getFormattedWind(getActivity(), data.getFloat(WeatherContract.COL_WEATHER_WIND), data.getFloat(WeatherContract.COL_WEATHER_DEGREES)));

        pressureView.setText(getActivity().getString(R.string.format_pressure, data.getFloat(WeatherContract.COL_WEATHER_PRESSURE)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
