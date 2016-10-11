package com.example.audioplayer;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.widget.SimpleCursorAdapter;
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

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.browse_list_item,
                null,
                new String[] {
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM_ID
                },
                new int[] {
                        R.id.browse_list_top_text,
                        R.id.browse_list_bottom_text,
                        R.id.browse_list_image
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        adapter.setViewBinder(
                new TrackBrowseFragment.ViewBinder(getResources(), getContentResolver())
        );

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
