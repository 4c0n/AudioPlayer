package com.example.audioplayer;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;

// TODO: nest in activity (static final)
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

        // TODO: init adapter outside of this method
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                activity,
                R.layout.browse_list_item,
                null,
                new String[] {
                        MediaStore.Audio.ArtistColumns.ARTIST,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS,
                        MediaStore.Audio.Artists._ID
                },
                new int[] {
                        R.id.browse_list_top_text,
                        R.id.browse_list_bottom_text,
                        R.id.browse_list_bottom_text,
                        R.id.browse_list_image
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        adapter.setViewBinder(new ArtistBrowseFragmentViewBinder(activity.getResources()));

        fragment.setListAdapter(adapter);

        fragment.setOnItemClickListener(new ArtistBrowseListViewOnItemClickListener(activity));

        return fragment;
    }
}
