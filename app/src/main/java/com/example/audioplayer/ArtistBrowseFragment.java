package com.example.audioplayer;

import android.os.Bundle;
import android.provider.MediaStore;

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

        return fragment;
    }
}
