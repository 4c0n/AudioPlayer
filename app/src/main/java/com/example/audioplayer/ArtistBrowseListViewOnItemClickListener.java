package com.example.audioplayer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

class ArtistBrowseListViewOnItemClickListener implements AdapterView.OnItemClickListener {
    private BrowseActivity mActivity;

    ArtistBrowseListViewOnItemClickListener(BrowseActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("4c0n", "onItemClick " + view.toString());

        // TODO: start new activity

        Bundle arguments = new Bundle();
        arguments.putString(ArtistDetailsFragment.ARGUMENT_ARTIST_ID, "" + id);

        ArtistDetailsExpandableListAdapter adapter = new ArtistDetailsExpandableListAdapter(
                mActivity.getLayoutInflater(),
                mActivity.getResources(),
                mActivity.getContentResolver()
        );

        ArtistDetailsFragment fragment = new ArtistDetailsFragment();
        fragment.setArguments(arguments);
        fragment.setAdapter(adapter);

        Spinner browseTypeSpinner = (Spinner) mActivity.findViewById(R.id.browse_type_spinner);
        browseTypeSpinner.setVisibility(View.GONE);

        TextView artistNameTextView = (TextView) view.findViewById(R.id.browse_list_top_text);
        TextView menuTextView = (TextView) mActivity.findViewById(R.id.menu_text);
        menuTextView.setText(artistNameTextView.getText());
        menuTextView.setVisibility(View.VISIBLE);

        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.media_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
