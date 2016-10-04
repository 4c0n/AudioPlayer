package com.example.audioplayer;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

public class ArtistBrowseListViewOnItemClickListener implements AdapterView.OnItemClickListener {
    private FragmentManager mFragmentManager;
    private LayoutInflater mInflater;
    private Resources mResources;
    private ContentResolver mContentResolver;

    ArtistBrowseListViewOnItemClickListener(
            FragmentManager fragmentManager,
            LayoutInflater inflater,
            Resources resources,
            ContentResolver contentResolver
    ) {
        mFragmentManager = fragmentManager;
        mInflater = inflater;
        mResources = resources;
        mContentResolver = contentResolver;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bundle arguments = new Bundle();
        arguments.putString(ArtistDetailsFragment.ARGUMENT_ARTIST_ID, "" + id);

        ArtistDetailsFragment fragment = new ArtistDetailsFragment();
        fragment.setArguments(arguments);

        ArtistDetailsExpandableListAdapter adapter = new ArtistDetailsExpandableListAdapter(
                mInflater,
                mResources,
                mContentResolver
        );

        fragment.setAdapter(adapter);
        fragment.setRetainInstance(true);

        mFragmentManager.beginTransaction()
                .replace(R.id.media_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
