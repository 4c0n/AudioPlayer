package com.example.audioplayer;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;

class PlaylistBrowseFragmentInitializer implements BrowseFragmentInitializer {
    private Context mContext;

    PlaylistBrowseFragmentInitializer(Context context) {
        mContext = context;
    }

    @Override
    public BrowseFragment initialize() {
        Bundle arguments = new Bundle();
        arguments.putString(
                BrowseFragment.ARGUMENT_SORT_COLUMN,
                MediaStore.Audio.PlaylistsColumns.NAME
        );
        arguments.putParcelable(
                BrowseFragment.ARGUMENT_CONTENT_URI,
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
        );
        arguments.putStringArray(
                BrowseFragment.ARGUMENT_COLUMNS,
                new String[] {
                        MediaStore.Audio.Playlists._ID,
                        MediaStore.Audio.PlaylistsColumns.NAME
                }
        );
        arguments.putString(
                BrowseFragment.ARGUMENT_EMPTY_TEXT,
                mContext.getString(R.string.no_playlists)
        );

        BrowseFragment fragment = new BrowseFragment();
        fragment.setArguments(arguments);
        fragment.setRetainInstance(true);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                mContext,
                R.layout.playlist_browse_list_item,
                null,
                new String[] {MediaStore.Audio.PlaylistsColumns.NAME},
                new int[] {R.id.playlist_name},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        fragment.setListAdapter(adapter);

        return fragment;
    }
}
