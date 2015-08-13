package com.example.leroy.popularmovies_tallleroy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        setRetainInstance(true);
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

        mTitleView.setText(mMovieSummary.getTitle());
        mPosterView.setImageBitmap(mMovieSummary.getPosterBitmap());
        mReleaseYearView.setText(mMovieSummary.getRelease_date().split("-")[0]);
        mRuntimeView.setText(mMovieSummary.getRuntime() + " min");
        mRatingView.setText(mMovieSummary.getVote_average() + "/10");
        mOverviewView.setText(mMovieSummary.getOverview());

        // add the trailers if they are present
        List<MovieSummary.Trailer> trailers = mMovieSummary.getTrailers();
        if (trailers != null && trailers.size() > 0) {
            rootView.findViewById(R.id.trailers_heading).setVisibility(View.VISIBLE);
            LinearLayout outerLayout = (LinearLayout)rootView.findViewById(R.id.details_outer_layout);
            for (MovieSummary.Trailer trailer : trailers) {
                View trailerView = inflater.inflate(R.layout.trailer_line, outerLayout, false);
                trailerView.setTag(trailer);
                String description = trailer.getType() + " - " + trailer.getTitle();
                trailerView.setContentDescription(description);
                ((TextView) trailerView.findViewById(R.id.trailer_text_view)).setText(description);
                trailerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View rowView = v;
                        MovieSummary.Trailer trailer = (MovieSummary.Trailer) rowView.getTag();
                        // show the trailer
                        if (trailer.isYoutube()) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("vnd.youtube:" + trailer.getSource()));
                            intent.putExtra("force_fullscreen", true);
                            if (Utility.isIntentAvailable(getActivity(), intent)) {
                                // run this when the youtube app is installed (preferred)
                                startActivity(intent);
                            } else {
                                // use the web browser if the youtube app is not installed
                                Intent intentURL = new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getURLString()));
                                intentURL.putExtra("force_fullscreen", true);
                                startActivity(intentURL);
                            }
                        }
                    }
                });
                outerLayout.addView(trailerView);
            }
        }

        return rootView;
    }

}
