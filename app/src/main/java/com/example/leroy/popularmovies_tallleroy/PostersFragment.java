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
    // need static here for the OnSharedPreferencesChanged callback
    static Activity activity;
    static Resources res;
    static int gridPosition = GridView.INVALID_POSITION;
    GridView gridView = null;

    public PostersFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (savedInstanceState == null) {
            activity = getActivity();
            res = activity.getResources();
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
        mPostersAdapter.setMainView(mainView);

        gridView = (GridView) mainView.findViewById(R.id.gridview);

        gridView.setAdapter(mPostersAdapter);
        if (savedInstanceState != null && savedInstanceState.containsKey("grid_position")) {
            gridPosition = savedInstanceState.getInt("grid_position");
        }
        if (gridPosition != GridView.INVALID_POSITION) {
            gridView.setSelection(gridPosition);
        }

        return mainView;
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String prefSortOrder = res.getString(R.string.pref_sort_order_list);
        String prefAPIKey = res.getString(R.string.pref_api_key_text);
        String prefNewData = res.getString(R.string.pref_new_data_loaded);

        if (key.equals(prefSortOrder)) {
            mPostersAdapter.changeSortOrder();
        }
        if (key.equals(prefNewData)) {
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
            SyncWorker.cleanDatabase(activity);
            SyncAdapter.syncImmediately(activity);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        gridPosition = gridView.getFirstVisiblePosition();
        super.onSaveInstanceState(outState);
    }


}
