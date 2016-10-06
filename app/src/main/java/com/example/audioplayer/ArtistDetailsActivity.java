package com.example.audioplayer;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


public class ArtistDetailsActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_ARTIST_ID = "artistId";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_artist_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.artist_details_activity_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            return;
        }

        ArtistDetailsFragment.ArtistDetailsExpandableListAdapter adapter =
                new ArtistDetailsFragment.ArtistDetailsExpandableListAdapter(
                        getLayoutInflater(),
                        getResources(),
                        getContentResolver()
                );

        String artistId = getIntent().getStringExtra(INTENT_EXTRA_ARTIST_ID);

        ArtistDetailsFragment fragment = ArtistDetailsFragment.newInstance(artistId);
        fragment.setAdapter(adapter);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.artist_details_fragment_container, fragment)
                .commit();
    }
}