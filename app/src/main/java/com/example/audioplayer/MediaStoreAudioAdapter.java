package com.example.audioplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class MediaStoreAudioAdapter extends BaseAdapter {
    private ContentResolver mContentResolver;
    private Cursor mMediaCursor;
    private LayoutInflater mInflater;

    public MediaStoreAudioAdapter(Context context, ContentResolver contentResolver) {
        mContentResolver = contentResolver;

        mMediaCursor = mContentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM_ID
                },
                MediaStore.Audio.Media.IS_MUSIC + "=1",
                null,
                null
        );

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mMediaCursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, parent, false);

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

        Cursor albumCursor = mContentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Albums.ALBUM_ART},
                "_id=" + albumId,
                null,
                null
        );

        if (albumCursor.getCount() > 0) {
            albumCursor.moveToFirst();

            String albumArtStr = albumCursor.getString(
                    albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)
            );

            if (albumArtStr != null) {
                Log.d("albumart", albumArtStr);
                holder.albumArt.setImageURI(Uri.fromFile(new File(albumArtStr)));
            } else {
                holder.albumArt.setImageURI(null);
            }
        } else {
            holder.albumArt.setImageURI(null);
        }

        albumCursor.close();

        return convertView;
    }

    static class ViewHolder {
        TextView title;
        TextView artist;
        ImageView albumArt;
    }
}
