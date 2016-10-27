package com.example.audioplayer;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;


class QueryParams implements Parcelable {
    private Uri contentUri;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private String sortOrder;

    QueryParams(Uri contentUri, String[] projection, String selection, String[] selectionArgs,
                String sortOrder) {
        this.contentUri = contentUri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
    }

    private QueryParams(Parcel in) {
        contentUri = in.readParcelable(ClassLoader.getSystemClassLoader());
        projection = in.createStringArray();
        selection = in.readString();
        selectionArgs = in.createStringArray();
        sortOrder = in.readString();
    }

    public static final Creator<QueryParams> CREATOR = new Creator<QueryParams>() {
        @Override
        public QueryParams createFromParcel(Parcel in) {
            return new QueryParams(in);
        }

        @Override
        public QueryParams[] newArray(int size) {
            return new QueryParams[size];
        }
    };

    @Override
    public int describeContents() {
        return Uri.CONTENTS_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(contentUri, 0);
        dest.writeStringArray(projection);
        dest.writeString(selection);
        dest.writeStringArray(selectionArgs);
        dest.writeString(sortOrder);
    }

    Uri getContentUri() {
        return contentUri;
    }

    String[] getProjection() {
        return projection;
    }

    String getSelection() {
        return selection;
    }

    String[] getSelectionArgs() {
        return selectionArgs;
    }

    String getSortOrder() {
        return sortOrder;
    }
}
