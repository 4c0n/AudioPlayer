package com.example.audioplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

public class BrowseActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    private TrackBrowseFragment mTrackBrowseFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        Toolbar toolbar = (Toolbar) findViewById(R.id.media_toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mTrackBrowseFragment = (TrackBrowseFragment) fragmentManager.findFragmentById(
                R.id.media_fragment_container
        );

        if (mTrackBrowseFragment == null) {
            mTrackBrowseFragment = new TrackBrowseFragment();
            mTrackBrowseFragment.setRetainInstance(true);
        }

        MediaStoreAudioAdapter mediaStoreAudioAdapter = new MediaStoreAudioAdapter(this,
                getContentResolver());

        mTrackBrowseFragment.setListAdapter(mediaStoreAudioAdapter);

        if (savedInstanceState != null) {
            return;
        }

        getSupportFragmentManager().beginTransaction().add(R.id.media_fragment_container,
                mTrackBrowseFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browse_menu, menu);

        MenuItem item = menu.findItem(R.id.menu_browse_type);
        Spinner browseTypeSpinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.browse_types, android.R.layout.simple_spinner_item);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        browseTypeSpinner.setAdapter(spinnerAdapter);
        browseTypeSpinner.setOnItemSelectedListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.sort_menu_button:
                mTrackBrowseFragment.sort();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        TextView textView = (TextView) view;
        String text = textView.getText().toString();

        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragmentList = fragmentManager.getFragments();


        if (text.equals(getString(R.string.browse_type_tracks))) {
            Log.d("onItemSelected", "Tracks");
        } else if (text.equals(getString(R.string.browse_type_artists))) {
            Log.d("onItemSelected", "Artists");
        } else if (text.equals(getString(R.string.browse_type_albums))) {
            Log.d("onItemSelected", "Albums");
        } else if (text.equals(getString(R.string.browse_type_playlists))) {
            Log.d("onItemSelected", "Playlists");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // The way the spinner is set up it is impossible to select nothing
    }
}