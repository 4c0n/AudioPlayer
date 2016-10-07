package com.example.audioplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;


public class AlbumDetailsActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_ALBUM_ID = "albumId";
    public static final String INTENT_EXTRA_ALBUM_NAME = "albumName";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.album_details_activity_toolbar);
        toolbar.setTitle(getIntent().getStringExtra(INTENT_EXTRA_ALBUM_NAME));
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            return;
        }

        TrackBrowseFragment.TrackBrowseListAdapter adapter =
                new TrackBrowseFragment.TrackBrowseListAdapter(this);

        TrackBrowseFragment fragment = TrackBrowseFragment.newInstance(
                getIntent().getStringExtra(INTENT_EXTRA_ALBUM_ID)
        );

        fragment.setEmptyText(getString(R.string.no_tracks));
        fragment.setListAdapter(adapter);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.album_details_fragment_container, fragment)
                .commit();
    }
}
