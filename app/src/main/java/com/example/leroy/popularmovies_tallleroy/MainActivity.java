package com.example.leroy.popularmovies_tallleroy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.leroy.popularmovies_tallleroy.sync.SyncAdapter;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements PostersAdapter.Callback {
    // helpers for debugging this app
    public static final boolean RUN_IN_EMULATOR = false;
    public static final boolean CLEAN_LOCAL_FILES = false;

    private static PostersFragment sPostersFragment;
    private boolean mTwopane = false;
    private MovieSummary mSelectedMovieSummary = null;

    // all access to our application mContext without passing it
    private static MainActivity sInstance;

    public static MainActivity getInstance() {
        return sInstance;
    }
    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    public MainActivity() {
        sInstance = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MovieSummary.setActivity(getInstance());
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwopane = true;
       }

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        if(savedInstanceState == null) {

            // initialize the sync adapter
            SyncAdapter.initializeSyncAdapter(getContext());

            // for debug, remove any old bitmaps in the file system
            if (CLEAN_LOCAL_FILES) {
                new Utility.CleanupAllFiledBitmaps(getContext()).execute(new ArrayList<MovieSummary>());
            }

            if (sPostersFragment == null) {
                sPostersFragment = (PostersFragment) fragmentManager.findFragmentById(R.id.PostersFragment);
                if (checkForNetworkConnection()) {
                    // prompt for the API key if this is the first time
                    checkForAPI_key();
                }
            }
        }
        // start up the sync mOurActivity to retrieve our posters from themoviedb
        SyncAdapter.syncImmediately(getContext());

    }

    private boolean checkForNetworkConnection() {
        boolean bConnected = isNetworkAvailable();
        if (!bConnected) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Continue without Network Connection?");
            alert.setMessage("Pop Movies requires Internet access to pull movie information from themoviedb.org. Choose Continue to " +
                    "review the movie information that we pulled the last time you used the program. " +
                    "If this is your first time using the program you should reply Cancel. ");
            alert.setCancelable(true);
            alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    finish();
                }
            });
            alert.setIcon(android.R.drawable.ic_dialog_alert);
            AlertDialog dialog = alert.create();
            dialog.show();
        }
        return bConnected;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void checkForAPI_key() {
        // see if the API_Key is set in our shared preferences,
        // if not put up a dialog box to collect it
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String apiKey = Utility.getPreferredApiKey(this);
        if (apiKey == null || apiKey.equals("")) {
            LayoutInflater li = LayoutInflater.from(this);
            View gatherAPIkeyView = li.inflate(R.layout.gather_api_key, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(gatherAPIkeyView);

            final EditText userInput = (EditText) gatherAPIkeyView
                    .findViewById(R.id.gather_api_key_edittext);

            // set dialog message
            alertDialogBuilder
                    .setTitle(R.string.gather_api_key_dialog_title)
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    // get user input and set it to the API_key
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString(getString(R.string.pref_api_key_text), userInput.getText().toString());
                                    editor.commit();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent mOurActivity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            if (mSelectedMovieSummary != null) {
                outState.putParcelable("selectedMovieSummary", mSelectedMovieSummary);
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey("selectedMovieSummary")) {
            MovieSummary movieSummary = (MovieSummary) savedInstanceState.getParcelable("selectedMovieSummary");
            onItemSelected(movieSummary);
        }
    }

    @Override
    public void onItemSelected(MovieSummary movieSummary) {
        if (mTwopane) {
            mSelectedMovieSummary = movieSummary;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(getString(R.string.movieSummaryExtra), movieSummary);
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();
        } else {
            // new item selected, start the detail mOurActivity
            Intent detailIntent = new Intent(this, MovieDetailActivity.class);
            detailIntent.putExtra(getString(R.string.movieSummaryExtra), movieSummary);
            startActivity(detailIntent);
        }
    }

}
