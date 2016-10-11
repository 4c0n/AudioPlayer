package com.example.audioplayer;


import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.ImageView;

import java.io.File;

class AlbumArtImageViewInitializer {
    private Resources mResources;
    private ImageView mImageView;
    private ContentResolver mContentResolver;
    private long mAlbumId;

    AlbumArtImageViewInitializer(Resources resources, ImageView imageView,
                                 ContentResolver contentResolver, long albumId) {
        mResources = resources;
        mImageView = imageView;
        mContentResolver = contentResolver;
        mAlbumId = albumId;
    }

    public void initialize() {
        Cursor albumCursor = mContentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Albums.ALBUM_ART},
                "_id=" + mAlbumId,
                null,
                null
        );

        mImageView.setImageURI(null);
        mImageView.setImageDrawable(null);
        if (albumCursor != null) {
            if (albumCursor.getCount() > 0) {
                albumCursor.moveToFirst();

                String albumArtStr = albumCursor.getString(
                        albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)
                );

                if (albumArtStr != null) {
                    mImageView.setImageURI(Uri.fromFile(new File(albumArtStr)));
                } else {
                    mImageView.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                    mResources,
                                    R.drawable.ic_music_note_black_24dp,
                                    null
                            )
                    );
                }
            }
            albumCursor.close();
        }
    }
}
