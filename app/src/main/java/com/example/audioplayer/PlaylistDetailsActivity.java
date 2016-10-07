package com.example.audioplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;


public class PlaylistDetailsActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_PLAYLIST_ID = "playlistId";
    public static final String INTENT_EXTRA_PLAYLIST_NAME = "playlistName";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playlist_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.playlist_details_activity_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            return;
        }

        TrackBrowseFragment.TrackBrowseListAdapter adapter =
                new TrackBrowseFragment.TrackBrowseListAdapter(this);

        TrackBrowseFragment fragment = TrackBrowseFragment.newPlaylistBasedInstance(
                getIntent().getLongExtra(INTENT_EXTRA_PLAYLIST_ID, -1)
        );

        fragment.setEmptyText(getString(R.string.no_tracks));
        fragment.setListAdapter(adapter);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.playlist_details_fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        TextView textView = (TextView) findViewById(R.id.menu_text);
        textView.setText(
                getIntent().getStringExtra(INTENT_EXTRA_PLAYLIST_NAME)
        );

        return true;
    }
}
