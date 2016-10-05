package com.example.audioplayer;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;

public class ArtistBrowseFragment extends BrowseFragment {
    public static ArtistBrowseFragment getInstance(BrowseActivity activity,
                                                   boolean sortedAscending) {
        Bundle arguments = new Bundle();
        arguments.putString(
                BrowseFragment.ARGUMENT_SORT_COLUMN,
                MediaStore.Audio.Artists.ARTIST_KEY
        );
        arguments.putParcelable(
                BrowseFragment.ARGUMENT_CONTENT_URI,
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        );
        arguments.putStringArray(
                BrowseFragment.ARGUMENT_COLUMNS,
                new String[] {
                        MediaStore.Audio.Artists._ID,
                        MediaStore.Audio.ArtistColumns.ARTIST,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
                }
        );
        arguments.putString(
                BrowseFragment.ARGUMENT_EMPTY_TEXT,
                activity.getString(R.string.no_artists)
        );

        ArtistBrowseFragment fragment = new ArtistBrowseFragment();
        fragment.setArguments(arguments);
        fragment.setSortedAscending(sortedAscending);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                activity,
                R.layout.artist_browse_list_item,
                null,
                new String[] {
                        MediaStore.Audio.ArtistColumns.ARTIST,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
                },
                new int[] {
                        R.id.artist_name,
                        R.id.artist_info,
                        R.id.artist_info
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        adapter.setViewBinder(new ArtistBrowseFragmentViewBinder(activity.getResources()));

        fragment.setListAdapter(adapter);

        fragment.setOnItemClickListener(new ArtistBrowseListViewOnItemClickListener(activity));

        return fragment;
    }
}
