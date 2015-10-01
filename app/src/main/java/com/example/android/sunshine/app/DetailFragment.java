package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

/**
 * Created by Rachel on 9/30/2015.
 */
public class DetailFragment extends Fragment {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private final String FORECAST_KEY = "intentKeyForecast";
    private final String FORECAST_SHARE_HASTAG = "#SunshineApp";
    private ShareActionProvider mShareActionProvider;
    String foreCastFromMain;

    public DetailFragment() {
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = this.getArguments();
        foreCastFromMain = bundle.getString(FORECAST_KEY);
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

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        TextView detailText = (TextView) rootView.findViewById(R.id.detail_text);
        detailText.setText(foreCastFromMain);

        return rootView;
    }

    //Helper function to update the intent of the Shared Action provider.  This is neeeded
    //so that the intent can be updated whenever the user changes settings
    private void setShareIntent(){
        // Create the share Intent
        String shareText = foreCastFromMain + FORECAST_SHARE_HASTAG;
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
}
