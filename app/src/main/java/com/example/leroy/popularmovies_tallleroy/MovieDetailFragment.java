package com.example.leroy.popularmovies_tallleroy;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {
    MovieSummary mMovieSummary = null;

    TextView mTitleView;
    ImageView mPosterView;
    TextView mReleaseYearView;
    TextView mRuntimeView;
    TextView mRatingView;
    TextView mOverviewView;
    ListView mTrailersListView;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

   /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(getString(R.string.movieSummaryExtra))) {
            mMovieSummary = getArguments().getParcelable(getString(R.string.movieSummaryExtra));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        mTitleView = (TextView) rootView.findViewById(R.id.detail_movie_title_textview);
        mPosterView = (ImageView) rootView.findViewById(R.id.detail_poster_imageview);
        mReleaseYearView = (TextView) rootView.findViewById(R.id.detail_release_year_textview);
        mRuntimeView = (TextView) rootView.findViewById(R.id.detail_movie_runtime_textview);
        mRatingView = (TextView) rootView.findViewById(R.id.detail_movie_rating_textview);
        mOverviewView = (TextView) rootView.findViewById(R.id.detail_overview_textview);
        mTrailersListView = (ListView)rootView.findViewById(R.id.trailers_list_view);

        mTitleView.setText(mMovieSummary.getTitle());
        mPosterView.setImageBitmap(Utility.getBitmapFromFile(mMovieSummary.getPosterKey(), getActivity()));
        mReleaseYearView.setText(mMovieSummary.getRelease_date().split("-")[0]);
        mRuntimeView.setText(mMovieSummary.getRuntime() + " min");
        mRatingView.setText(mMovieSummary.getVote_average() + "/10");
        mOverviewView.setText(mMovieSummary.getOverview());
        MovieSummary.Trailer[] trailerArray = new MovieSummary.Trailer[mMovieSummary.getTrailers().size()];
        mMovieSummary.getTrailers().toArray(trailerArray);
        MovieDetailFragment.TrailersListAdapter trailersListAdapter = new MovieDetailFragment.TrailersListAdapter(getActivity(),
                mMovieSummary.getTrailers());
        mTrailersListView.setAdapter(trailersListAdapter);
        mTrailersListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        setListViewHeightBasedOnChildren(mTrailersListView);

        return rootView;
    }

    // based on a solution found at http://stackoverflow.com/questions/18367522/android-list-view-inside-a-scroll-view
    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public class TrailersListAdapter extends ArrayAdapter<MovieSummary.Trailer> {
        Context context;
        List<MovieSummary.Trailer> trailers;
        TextView title;

        public TrailersListAdapter(Context context, List<MovieSummary.Trailer> trailers) {
            super(context, 0, trailers);
            this.context = context;
            this.trailers = trailers;
        }

        @Override
        public int getCount() {
            return trailers.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.trailer_line, parent, false);
                title = (TextView)rowView.findViewById(R.id.trailer_text_view);
            }
            MovieSummary.Trailer trailer = trailers.get(position);
            rowView.setTag(trailer);
            String description = trailer.getType() + " - " + trailer.getTitle();
            title.setText(description);
            rowView.setContentDescription(description);

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View rowView = (ImageView) v;
                    MovieSummary.Trailer trailer = (MovieSummary.Trailer) rowView.getTag();
                    String trailerURLString = trailer.getURLString();
                    // show the trailer
                }
            });


            return rowView;
        }
    }
}
