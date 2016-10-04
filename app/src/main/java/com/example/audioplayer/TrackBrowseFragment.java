package com.example.audioplayer;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;

public class TrackBrowseFragment extends BrowseFragment {
     public static TrackBrowseFragment getInstance(Context context, boolean sortedAscending) {
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
                 context.getString(R.string.no_tracks)
         );

         TrackBrowseFragment fragment = new TrackBrowseFragment();
         fragment.setArguments(arguments);
         fragment.setSortedAscending(sortedAscending);

         MediaStoreAudioAdapter mediaStoreAudioAdapter = new MediaStoreAudioAdapter(context);
         fragment.setListAdapter(mediaStoreAudioAdapter);

         return fragment;
     }
}
