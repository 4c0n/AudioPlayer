package com.example.audioplayer;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;

class TrackBrowseFragmentInitializer implements BrowseFragmentInitializer {
    private Context mContext;

    TrackBrowseFragmentInitializer(Context context) {
        mContext = context;
    }

    @Override
    public BrowseFragment initialize() {
        Bundle arguments = new Bundle();
        arguments.putString(
                BrowseFragment.ARGUMENT_SORT_COLUMN,
                MediaStore.Audio.Media.TITLE_KEY
        );
        arguments.putParcelable(
                BrowseFragment.ARGUMENT_CONTENT_URI,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        );
        arguments.putStringArray(
                BrowseFragment.ARGUMENT_COLUMNS,
                new String[] {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM_ID
                }
        );
        arguments.putString(
                BrowseFragment.ARGUMENT_SELECTION,
                MediaStore.Audio.Media.IS_MUSIC + "=1"
        );
        arguments.putString(
                BrowseFragment.ARGUMENT_EMPTY_TEXT,
                mContext.getString(R.string.no_tracks)
        );

        BrowseFragment fragment = new BrowseFragment();
        fragment.setArguments(arguments);
        fragment.setRetainInstance(true);

        MediaStoreAudioAdapter mediaStoreAudioAdapter = new MediaStoreAudioAdapter(mContext);

        fragment.setListAdapter(mediaStoreAudioAdapter);

        return fragment;
    }
}
