package com.example.leroy.popularmovies_tallleroy;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.leroy.popularmovies_tallleroy.data.PostersContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeRoy on 8/5/2015.
 *
*/
public class PostersAdapter extends ArrayAdapter<MovieSummary> {
    private static final String LOG_TAG = PostersAdapter.class.getSimpleName();

    List<MovieSummary> list;
    Context context;

    public PostersAdapter(Context context, List<MovieSummary> newList) {
        super(context, 0, newList);
        this.context = context;
        this.list = newList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater.from(context));
        View gridCell;
        MovieSummary ms = list.get(position);
        gridCell = new View(context);
        gridCell = inflater.inflate(R.layout.poster_cell, null);
        ImageView posterImageView = (ImageView) gridCell.findViewById(R.id.posterimg);
        posterImageView.setContentDescription(ms.getTitle());
        posterImageView.setImageBitmap(ms.getPosterBitmap());

        return gridCell;
    }

    public static List<MovieSummary> getCurrentMovieList(Context context) {
        ArrayList<MovieSummary> movieList = new ArrayList<MovieSummary>(20);

       String sortRating = context.getResources().getString(R.string.pref_value_sort_order_rating);

        String sortOrder = PostersContract.PostersEntry.POSTERS_QUERY_SORT_ORDER_POPULARITY;
        if (Utility.getPreferredSortOrder(context).equals(sortRating)) {
            sortOrder = PostersContract.PostersEntry.POSTERS_QUERY_SORT_ORDER_RATING;
        }

        Cursor cursor = context.getContentResolver().query(PostersContract.PostersEntry.CONTENT_URI, null, null, null, sortOrder);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                movieList.add(new MovieSummary(cursor));
            }
        }


        return movieList;
    }

}
