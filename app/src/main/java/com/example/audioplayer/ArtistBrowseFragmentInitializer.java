package com.example.audioplayer;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;

class ArtistBrowseFragmentInitializer implements BrowseFragmentInitializer {
    private Context mContext;
    private FragmentManager mFragmentManager;

    ArtistBrowseFragmentInitializer(Context context, FragmentManager fragmentManager) {
        mContext = context;
        mFragmentManager = fragmentManager;
    }

    @Override
    public BrowseFragment initialize() {
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
                mContext.getString(R.string.no_artists)
        );

        BrowseFragment fragment = new BrowseFragment();
        fragment.setArguments(arguments);
        fragment.setRetainInstance(true);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                mContext,
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
        adapter.setViewBinder(new ArtistBrowseFragmentViewBinder(mContext.getResources()));

        fragment.setListAdapter(adapter);

        fragment.setOnItemClickListener(
                new ArtistBrowseListViewOnItemClickListener(
                        mFragmentManager,
                        LayoutInflater.from(mContext),
                        mContext.getResources(),
                        mContext.getContentResolver()
                )
        );

        return fragment;
    }
}
