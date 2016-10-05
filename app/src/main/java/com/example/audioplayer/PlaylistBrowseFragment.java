package com.example.audioplayer;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;

public class PlaylistBrowseFragment extends BrowseFragment {
    public static PlaylistBrowseFragment getInstance(Context context, boolean sortedAscending) {
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
                context.getString(R.string.no_playlists)
        );

        PlaylistBrowseFragment fragment = new PlaylistBrowseFragment();
        fragment.setArguments(arguments);
        fragment.setSortedAscending(sortedAscending);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                context,
                R.layout.browse_list_item,
                null,
                new String[] {
                        MediaStore.Audio.PlaylistsColumns.NAME,
                        MediaStore.Audio.Playlists._ID
                },
                new int[] {
                        R.id.browse_list_top_text,
                        R.id.browse_list_image
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        adapter.setViewBinder(new PlaylistBrowseFragmentViewBinder(context.getResources()));

        fragment.setListAdapter(adapter);

        return fragment;
    }
}
