package com.example.audioplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class Media extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Media", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        Toolbar toolbar = (Toolbar) findViewById(R.id.media_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            return;
        }

        MediaStoreAudioAdapter adapter = new MediaStoreAudioAdapter(this, getContentResolver());

        BrowseFragment browseFragment = new BrowseFragment();
        browseFragment.setRetainInstance(true);
        browseFragment.setListAdapter(adapter);

        getSupportFragmentManager().beginTransaction().add(R.id.media_fragment_container,
                browseFragment).commit();
    }
}
