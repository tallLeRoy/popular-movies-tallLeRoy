package com.example.leroy.popularmovies_tallleroy;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.leroy.popularmovies_tallleroy.data.PostersContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by LeRoy on 8/5/2015.
 *
*/
public class PostersAdapter extends ArrayAdapter<MovieSummary> implements AsyncSupplyBitmapResponse {
    private static final String LOG_TAG = PostersAdapter.class.getSimpleName();

    List<MovieSummary> movieList;
    Context context;
    PostersAdapter.Callback callback;
    String currentSortOrder;
    ImageView posterImageView;

    public interface Callback {
        public void onItemSelected(MovieSummary movieSummary);
    }

    public void setCallback(Activity destination) {
        callback = (PostersAdapter.Callback)destination;
    }

    public PostersAdapter(Context context, List<MovieSummary> movieList) {
        super(context, 0, movieList);
        this.context = context;
        this.movieList = movieList;
        updateMovieList();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridCell = convertView;
        if (gridCell == null) {
            LayoutInflater inflater = (LayoutInflater.from(context));
            gridCell = inflater.inflate(R.layout.poster_cell, null);
            posterImageView = (ImageView) gridCell.findViewById(R.id.posterimg);
        }
        MovieSummary ms = movieList.get(position);
        // set the view with the poster and give the title to the content description
        posterImageView.setContentDescription(ms.getTitle());
        posterImageView.setImageBitmap(ms.getPosterBitmap());
        posterImageView.setTag(ms);
        posterImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView posterImageView = (ImageView) v;
                MovieSummary ms = (MovieSummary) posterImageView.getTag();
                callback.onItemSelected(ms);
            }
        });

        return gridCell;
    }

    // grab the movieList from our content provider
    public void updateMovieList() {
        List<MovieSummary> newList = new ArrayList<MovieSummary>(20);

        String sortRating = context.getResources().getString(R.string.pref_value_sort_order_rating);
        String sortOrder = PostersContract.PostersEntry.POSTERS_QUERY_SORT_ORDER_POPULARITY;
        currentSortOrder = Utility.getPreferredSortOrder(context);
        if (currentSortOrder.equals(sortRating)) {
            sortOrder = PostersContract.PostersEntry.POSTERS_QUERY_SORT_ORDER_RATING;
        }


        Cursor cursor = context.getContentResolver().query(PostersContract.PostersEntry.CONTENT_URI, null, null, null, sortOrder);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                newList.add(new MovieSummary(cursor));
            }
        }

        cursor.close();

        if (newList.size() > 0) {
            // load the bitmaps locally
            SupplyBitmaps supplyBitmaps = new SupplyBitmaps(this, context);
            supplyBitmaps.execute(newList);
        }
    }


    // if the user just wants to change the sort order, use the same movieList,
    // but sort it based on popularity or ratings
    public void changeSortOrder() {
        String sortRating = context.getResources().getString(R.string.pref_value_sort_order_rating);
        String newSortOrder = Utility.getPreferredSortOrder(context);
        if (!newSortOrder.equals(currentSortOrder)) {
            currentSortOrder = newSortOrder;
            if (currentSortOrder.equals(sortRating)) {
                // a rating sort
                Collections.sort(movieList, new Comparator<MovieSummary>() {
                    @Override
                    public int compare(MovieSummary lhs, MovieSummary rhs) {
                        Double d1 = Double.parseDouble(lhs.getVote_average());
                        Double d2 = Double.parseDouble(rhs.getVote_average());
                        return d2.compareTo(d1);
                    }
                });
            } else {
                // a popularity sort
                Collections.sort(movieList, new Comparator<MovieSummary>() {
                    @Override
                    public int compare(MovieSummary lhs, MovieSummary rhs) {
                        Double d1 = Double.parseDouble(lhs.getPopularity());
                        Double d2 = Double.parseDouble(rhs.getPopularity());
                        return d2.compareTo(d1);
                    }
                });
            }
            // have the view redrawn
            notifyDataSetChanged();
        }
    }

    @Override
    public void bitmapsAvailable(List<MovieSummary> movieSummaries) {
        clear();
        addAll(movieSummaries);
        notifyDataSetChanged();
        // delete any unused bitmap files in the file system
        new Utility.CleanupFiledBitmaps(context).execute(movieSummaries);
    }
}
