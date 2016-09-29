package com.example.audioplayer;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;

class GenreBrowseFragmentInitializer implements BrowseFragmentInitializer {
    private Context mContext;

    GenreBrowseFragmentInitializer(Context context) {
        mContext = context;
    }

    @Override
    public BrowseFragment initialize() {
        Bundle arguments = new Bundle();
        arguments.putString(
                BrowseFragment.ARGUMENT_SORT_COLUMN,
                MediaStore.Audio.GenresColumns.NAME
        );
        arguments.putParcelable(
                BrowseFragment.ARGUMENT_CONTENT_URI,
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
        );
        arguments.putStringArray(
                BrowseFragment.ARGUMENT_COLUMNS,
                new String[] {
                        MediaStore.Audio.Genres._ID,
                        MediaStore.Audio.GenresColumns.NAME
                }
        );
        arguments.putString(
                BrowseFragment.ARGUMENT_EMPTY_TEXT,
                mContext.getString(R.string.no_genres)
        );

        BrowseFragment fragment = new BrowseFragment();
        fragment.setArguments(arguments);
        fragment.setRetainInstance(true);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                mContext,
                R.layout.genre_browse_list_item,
                null,
                new String[] {MediaStore.Audio.GenresColumns.NAME},
                new int[] {R.id.genre_name},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        fragment.setListAdapter(adapter);

        return fragment;
    }
}
