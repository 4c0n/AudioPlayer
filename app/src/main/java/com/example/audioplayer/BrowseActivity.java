package com.example.audioplayer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class BrowseActivity extends AppCompatActivity {
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

        MediaStoreAudioAdapter mediaStoreAudioAdapter = new MediaStoreAudioAdapter(this, getContentResolver());
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
        return true;
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
}