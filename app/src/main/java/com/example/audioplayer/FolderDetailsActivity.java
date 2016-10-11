package com.example.audioplayer;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;


public class FolderDetailsActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_FOLDER_PATH = "folderPath";
    public static final String INTENT_EXTRA_FOLDER_NAME = "folderName";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_folder_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.folder_details_activity_toolbar);
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

        TrackBrowseFragment fragment = TrackBrowseFragment.newFolderBasedInstance(
                getIntent().getStringExtra(INTENT_EXTRA_FOLDER_PATH)
        );
        fragment.setEmptyText(getString(R.string.no_tracks));
        fragment.setListAdapter(adapter);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.folder_details_fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        TextView menuText = (TextView) findViewById(R.id.menu_text);
        menuText.setText(getIntent().getStringExtra(INTENT_EXTRA_FOLDER_NAME));

        return true;
    }
}
