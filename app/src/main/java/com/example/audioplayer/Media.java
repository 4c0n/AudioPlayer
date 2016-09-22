package com.example.audioplayer;

import android.content.ContentResolver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class Media extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        if (savedInstanceState != null) {
            return;
        }

        BrowseFragment browseFragment = new BrowseFragment();

        MediaStoreAudioAdapter adapter = new MediaStoreAudioAdapter(this, getContentResolver());

        browseFragment.setListAdapter(adapter);

        getSupportFragmentManager().beginTransaction().add(R.id.media_fragment_container,
                browseFragment).commit();
    }
}
