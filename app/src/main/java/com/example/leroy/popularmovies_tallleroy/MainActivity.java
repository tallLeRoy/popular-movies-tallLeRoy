package com.example.leroy.popularmovies_tallleroy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
    public static final boolean RUN_IN_EMULATOR = false;
    public static final boolean CLEAN_LOCAL_FILES = false;

    private static PostersFragment mPostersFragment;

    // all access to our application context without passing it
    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }
    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    private boolean mTwopane = false;

    public MainActivity() {
        instance = this;
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
            // prompt for the API key if this is the first time
            checkForAPI_key();

            // initialize the sync adapter
            SyncAdapter.initializeSyncAdapter(getContext());

            // for debug, remove any old bitmaps in the file system
            if (CLEAN_LOCAL_FILES) {
                new Utility.CleanupAllFiledBitmaps(getContext()).execute(new ArrayList<MovieSummary>());
            }

            if (mPostersFragment == null) {
                mPostersFragment = (PostersFragment) fragmentManager.findFragmentById(R.id.PostersFragment);
            }
        }
        // start up the sync mOurActivity to retrieve our posters from themoviedb
        SyncAdapter.syncImmediately(getContext());
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
    public void onItemSelected(MovieSummary movieSummary) {
        if (mTwopane) {
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
