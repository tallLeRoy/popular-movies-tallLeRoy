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
    String mSortRating;
    String mSortFavorite;

    List<MovieSummary> mMovieList;
    Context mContext;
    PostersAdapter.Callback mCallback;
    String mCurrentSortOrder;
    View mMainView;

    public void setmMainView(View mMainView) {
        this.mMainView = mMainView;
    }

    public interface Callback {
        public void onItemSelected(MovieSummary movieSummary);
    }

    public void setCallback(Activity destination) {
        mCallback = (PostersAdapter.Callback)destination;
    }

    public PostersAdapter(Context context, List<MovieSummary> movies) {
        super(context, 0, movies);
        this.mContext = context;
        this.mMovieList = movies;
        mSortRating = context.getResources().getString(R.string.pref_value_sort_order_rating);
        mSortFavorite = context.getResources().getString(R.string.pref_value_sort_order_favorite);
        if(!MainActivity.CLEAN_LOCAL_FILES) {
            updateMovieList();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridCell = convertView;
        if (gridCell == null) {
            LayoutInflater inflater = (LayoutInflater.from(mContext));
            gridCell = inflater.inflate(R.layout.poster_cell, null);
       }
        MovieSummary ms = mMovieList.get(position);
        // set the view with the poster and give the title to the content description
        ImageView posterImageView = (ImageView) gridCell.findViewById(R.id.posterimg);
        posterImageView.setContentDescription(ms.getTitle());
        posterImageView.setImageBitmap(ms.getPosterBitmap());
        posterImageView.setTag(ms);
        posterImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView posterImageView = (ImageView) v;
                MovieSummary ms = (MovieSummary) posterImageView.getTag();
                mCallback.onItemSelected(ms);
            }
        });
        ImageView starView = (ImageView) gridCell.findViewById(R.id.poster_favorite_star);
        if(ms.isFavorite()) {
            starView.setVisibility(View.VISIBLE);
            starView.setTag(ms);
            starView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView starView = (ImageView) v;
                    MovieSummary ms = (MovieSummary) starView.getTag();
                    mCallback.onItemSelected(ms);
                }
            });
        } else {
            starView.setVisibility(View.INVISIBLE);
        }

        return gridCell;
    }

    // grab the mMovieList from our content provider
    public void updateMovieList() {
        List<MovieSummary> newList = new ArrayList<MovieSummary>(20);

        // always pull movies in popularity order
        String sortOrder = PostersContract.PostersEntry.POSTERS_QUERY_SORT_ORDER_POPULARITY;
        Cursor cursor = mContext.getContentResolver().query(PostersContract.PostersEntry.CONTENT_URI, null, null, null, sortOrder);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                newList.add(new MovieSummary(cursor));
            }
        }

        cursor.close();

        if (newList.size() > 0) {
            // now fix the sort order for display
            setSortOrder();
            // load the bitmaps locally
            SupplyBitmaps supplyBitmaps = new SupplyBitmaps(this, mContext);
            supplyBitmaps.execute(newList);
        }
    }


    // if the user just wants to change the sort order, use the same mMovieList,
    // but sort it based on popularity or ratings
    public void changeSortOrder() {
        String newSortOrder = Utility.getPreferredSortOrder(mContext);
        if (!newSortOrder.equals(mCurrentSortOrder)) {
            setSortOrder();
            // have the view redrawn
            notifyDataSetChanged();
        }
    }

    private void setSortOrder() {
        String newSortOrder = Utility.getPreferredSortOrder(mContext);
        mCurrentSortOrder = newSortOrder;
        if (mCurrentSortOrder.equals(mSortRating)) {
            // a rating sort
            Collections.sort(mMovieList, new Comparator<MovieSummary>() {
                @Override
                public int compare(MovieSummary lhs, MovieSummary rhs) {
                    Double d1 = Double.parseDouble(lhs.getVote_average());
                    Double d2 = Double.parseDouble(rhs.getVote_average());
                    return d2.compareTo(d1);
                }
            });
        } else if (mCurrentSortOrder.equals(mSortFavorite)) {
            // favorite sort is based on favorite and popularity
            Collections.sort(mMovieList, new Comparator<MovieSummary>() {
                @Override
                public int compare(MovieSummary lhs, MovieSummary rhs) {
                    Double d1 = Double.parseDouble(lhs.getPopularity()) + (lhs.isFavorite() ? 1000d : 0d);
                    Double d2 = Double.parseDouble(rhs.getPopularity()) + (rhs.isFavorite() ? 1000d : 0d);
                    return d2.compareTo(d1);
                }
            });
        } else {
            // a popularity sort
            Collections.sort(mMovieList, new Comparator<MovieSummary>() {
                @Override
                public int compare(MovieSummary lhs, MovieSummary rhs) {
                    Double d1 = Double.parseDouble(lhs.getPopularity());
                    Double d2 = Double.parseDouble(rhs.getPopularity());
                    return d2.compareTo(d1);
                }
            });
        }
    }

    public void refreshFavorites() {
        if (mCurrentSortOrder.equals(mSortFavorite)) {
            // we need to refresh the adapter
            List<MovieSummary> clone = new ArrayList<MovieSummary>(mMovieList.size());
            clone.addAll(mMovieList);
            clear();
            addAll(clone);
            setSortOrder();
            notifyDataSetChanged();
        }

    }

    @Override
    public void bitmapsAvailable(List<MovieSummary> movieSummaries) {
        clear();
        addAll(movieSummaries);
        setSortOrder();
        notifyDataSetChanged();
        // hide themoviedb logo
        if(mMainView != null) {
            mMainView.findViewById(R.id.logo).setVisibility(View.INVISIBLE);
        }
        // delete any unused bitmap files in the file system
        new Utility.CleanupFiledBitmaps(mContext).execute(movieSummaries);
    }
}
