package com.example.audioplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class Media extends AppCompatActivity {
    private MediaStoreAudioAdapter mMediaStoreAudioAdapter;

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

        mMediaStoreAudioAdapter = new MediaStoreAudioAdapter(this, getContentResolver());

        BrowseFragment browseFragment = new BrowseFragment();
        browseFragment.setRetainInstance(true);
        browseFragment.setListAdapter(mMediaStoreAudioAdapter);

        getSupportFragmentManager().beginTransaction().add(R.id.media_fragment_container,
                browseFragment).commit();
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
                mMediaStoreAudioAdapter.sort();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }
}