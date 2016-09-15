package com.example.audioplayer;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

public class Media extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        if (savedInstanceState != null) {
            return;
        }

        BrowseFragment browseFragment = new BrowseFragment();

        String[] columns = {MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.TITLE};

        Cursor mediaCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,//columns,
                null,
                null,
                null);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.list_item,
                mediaCursor,
                new String[] {"Title", "Artist"},
                new int[] {R.id.track_title, R.id.track_artist},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        browseFragment.setListAdapter(adapter);

        getSupportFragmentManager().beginTransaction().add(R.id.media_fragment_container, browseFragment)
                .commit();
    }
}
