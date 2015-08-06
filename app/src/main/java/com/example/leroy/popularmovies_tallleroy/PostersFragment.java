package com.example.leroy.popularmovies_tallleroy;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;


/**
 * A placeholder fragment containing a simple view.
 */
public class PostersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public PostersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View mainView =  inflater.inflate(R.layout.fragment_posters, container, false);

        PostersAdapter postersAdapter = new PostersAdapter(getActivity(), PostersAdapter.getCurrentMovieList(getActivity()));

        GridView gridView = (GridView) mainView.findViewById(R.id.gridview);
        gridView.setAdapter(postersAdapter);

        return mainView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface Callback {
        public void onItemSelected(Uri dateUri);
    }
}
