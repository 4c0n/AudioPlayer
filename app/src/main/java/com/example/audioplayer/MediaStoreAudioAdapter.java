package com.example.audioplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

/**
 * TODO: Register as data observer so the list is updated when songs are added to the MediaStore
 */
class MediaStoreAudioAdapter extends BaseAdapter {
    private Cursor mMediaCursor;
    private LayoutInflater mInflater;
    private ContentResolver mContentResolver;

    private static class ViewHolder {
        TextView title;
        TextView artist;
        ImageView albumArt;
    }

    MediaStoreAudioAdapter(Context context) {
        mContentResolver = context.getContentResolver();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (mMediaCursor != null) {
            return mMediaCursor.getCount();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mMediaCursor != null) {
            mMediaCursor.moveToPosition(position);
            return mMediaCursor;
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        if (mMediaCursor != null) {
            if (mMediaCursor.moveToPosition(position)) {
                return mMediaCursor.getLong(
                        mMediaCursor.getColumnIndex(MediaStore.Audio.Media._ID)
                );
            }

            return 0;
        }

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.track_browse_list_item, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.track_title);
            holder.artist = (TextView) convertView.findViewById(R.id.track_artist);
            holder.albumArt = (ImageView) convertView.findViewById(R.id.track_album_art);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        mMediaCursor.moveToPosition(position);

        String titleText = mMediaCursor.getString(
                mMediaCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
        );
        holder.title.setText(titleText);

        String artistText = mMediaCursor.getString(
                mMediaCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
        );
        holder.artist.setText(artistText);

        int albumId = mMediaCursor.getInt(
                mMediaCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
        );

        // TODO: use cursorloader or other means of threading
        Cursor albumCursor = mContentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Albums.ALBUM_ART},
                "_id=" + albumId,
                null,
                null
        );

        holder.albumArt.setImageURI(null);
        if (albumCursor != null) {
            if (albumCursor.getCount() > 0) {
                albumCursor.moveToFirst();

                String albumArtStr = albumCursor.getString(
                        albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)
                );

                if (albumArtStr != null) {
                    holder.albumArt.setImageURI(Uri.fromFile(new File(albumArtStr)));
                }
            }
            albumCursor.close();
        }

        return convertView;
    }

    void changeCursor(Cursor cursor) {
        if (mMediaCursor != null) {
            mMediaCursor.close();
        }

        if (cursor != null) {
            mMediaCursor = cursor;
            notifyDataSetChanged();
        } else {
            notifyDataSetInvalidated();
        }
    }
}