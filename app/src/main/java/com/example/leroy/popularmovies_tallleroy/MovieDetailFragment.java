package com.example.leroy.popularmovies_tallleroy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

    private static final String TRAILER_SHARE_HASHTAG = " #PopMoviesApp";
    private static String mFirstTrailerURL = null;

    private ShareActionProvider mShareActionProvider;


    TextView mTitleView;
    ImageView mPosterView;
    TextView mReleaseYearView;
    TextView mRuntimeView;
    TextView mRatingView;
    TextView mOverviewView;
    CheckBox mFavoriteCheckBox;

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
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(getString(R.string.movieSummaryExtra))) {
            mMovieSummary = getArguments().getParcelable(getString(R.string.movieSummaryExtra));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mMovieSummary != null) {
            List<MovieSummary.Trailer> list = mMovieSummary.getTrailers();
            if (list != null && list.size() > 0) {
                MovieSummary.Trailer t = list.get(0);
                mFirstTrailerURL = t.getURLString();
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mFirstTrailerURL + TRAILER_SHARE_HASHTAG);
        return shareIntent;
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
        mFavoriteCheckBox = (CheckBox)rootView.findViewById(R.id.detail_favorite_star);

        mTitleView.setText(mMovieSummary.getTitle());
        mPosterView.setImageBitmap(mMovieSummary.getPosterBitmap());
        mReleaseYearView.setText(mMovieSummary.getRelease_date().split("-")[0]);
        mRuntimeView.setText(mMovieSummary.getRuntime() + " min");
        mRatingView.setText(mMovieSummary.getVote_average() + "/10");
        mOverviewView.setText(mMovieSummary.getOverview());
        mFavoriteCheckBox.setChecked(mMovieSummary.isFavorite());
        if (mMovieSummary.isFavorite()) {
            mFavoriteCheckBox.setButtonDrawable(R.drawable.btn_star_big_on);
        } else {
         }
        mFavoriteCheckBox.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 CheckBox cb = (CheckBox)v;
                 if (mMovieSummary.isFavorite()) {
                     if(!cb.isChecked()) {
                         mFavoriteCheckBox.setButtonDrawable(R.drawable.btn_star_big_off);
                         mMovieSummary.setFavorite(cb.isChecked());
                     }
                 } else {
                     if(cb.isChecked()) {
                         mFavoriteCheckBox.setButtonDrawable(R.drawable.btn_star_big_on);
                         mMovieSummary.setFavorite(cb.isChecked());
                     }
                 }
            }
         });

        // add the trailers if they are present
        List<MovieSummary.Trailer> trailers = mMovieSummary.getTrailers();
        if (trailers != null && trailers.size() > 0) {
            LinearLayout outerLayout = (LinearLayout)rootView.findViewById(R.id.trailers_layout);
            outerLayout.setVisibility(View.VISIBLE);
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
        List<MovieSummary.Review> reviews = mMovieSummary.getReviews();
        if (reviews != null && reviews.size() > 0) {
            LinearLayout outerLayout = (LinearLayout)rootView.findViewById(R.id.reviews_layout);
            outerLayout.setVisibility(View.VISIBLE);
            for (MovieSummary.Review review : reviews) {
                View reviewView = inflater.inflate(R.layout.review_line, outerLayout, false);
                reviewView.setTag(review);
                reviewView.setContentDescription(review.getContent() + "... by " + review.getAuthor());
                ((TextView)reviewView.findViewById(R.id.review_textview)).setText(review.getContent());
                ((TextView)reviewView.findViewById(R.id.author_textview)).setText(review.getAuthor());
                 outerLayout.addView(reviewView);
            }
        }

        return rootView;
    }

}
