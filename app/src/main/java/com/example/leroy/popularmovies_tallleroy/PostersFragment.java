package com.example.leroy.popularmovies_tallleroy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.leroy.popularmovies_tallleroy.sync.SyncAdapter;
import com.example.leroy.popularmovies_tallleroy.sync.SyncWorker;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class PostersFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    PostersAdapter mPostersAdapter;
    static Activity mOurActivity;
    static Resources mOurResources;
    static int mGridPosition = GridView.INVALID_POSITION;
    GridView mGridView = null;

    public PostersFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (savedInstanceState == null) {
            mOurActivity = getActivity();
            mOurResources = mOurActivity.getResources();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View mainView =  inflater.inflate(R.layout.fragment_posters, container, false);

        List<MovieSummary> movieSummaries = MovieSummaries.getMovieSummaries();
        if (movieSummaries == null) {
            movieSummaries = new ArrayList<MovieSummary>(20);
            MovieSummaries.setMovieSummaries(movieSummaries);
        }
        mPostersAdapter = new PostersAdapter(getActivity(),  movieSummaries);
        mPostersAdapter.setCallback(getActivity());
        mPostersAdapter.setmMainView(mainView);

        mGridView = (GridView) mainView.findViewById(R.id.gridview);

        mGridView.setAdapter(mPostersAdapter);

        return mainView;
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String prefSortOrder = mOurResources.getString(R.string.pref_sort_order_list);
        String prefAPIKey = mOurResources.getString(R.string.pref_api_key_text);
        String prefNewData = mOurResources.getString(R.string.pref_new_data_loaded);

        if (key.equals(prefSortOrder)) {
            mPostersAdapter.changeSortOrder();
        }
        if (key.startsWith(prefNewData)) {
            String value = sharedPreferences.getString(key, null);
            if (value != null) {
                // remove the new data flag
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(prefNewData);
                editor.commit();
                // update the view
                mPostersAdapter.updateMovieList();
            }
        }
        if (key.equals(prefAPIKey)) {
            SyncWorker.cleanDatabase(mOurActivity);
            SyncAdapter.syncImmediately(mOurActivity);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("grid_position")) {
            mGridPosition = savedInstanceState.getInt("grid_position");
        }
        if (mGridView != null && mGridPosition != GridView.INVALID_POSITION) {
            mGridView.setSelection(mGridPosition);
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mGridView != null) {
            mGridPosition = mGridView.getFirstVisiblePosition();
        }
        super.onSaveInstanceState(outState);
    }


}
