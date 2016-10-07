package com.example.audioplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;


public class AlbumDetailsActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_ALBUM_ID = "albumId";
    public static final String INTENT_EXTRA_ALBUM_NAME = "albumName";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.album_details_activity_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            return;
        }

        TrackBrowseFragment.TrackBrowseListAdapter adapter =
                new TrackBrowseFragment.TrackBrowseListAdapter(this);

        TrackBrowseFragment fragment = TrackBrowseFragment.newAlbumBasedInstance(
                getIntent().getLongExtra(INTENT_EXTRA_ALBUM_ID, -1)
        );

        fragment.setEmptyText(getString(R.string.no_tracks));
        fragment.setListAdapter(adapter);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.album_details_fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String albumName = getIntent().getStringExtra(INTENT_EXTRA_ALBUM_NAME);

        TextView menuText = (TextView) findViewById(R.id.menu_text);
        menuText.setText(albumName);

        return true;
    }
}
