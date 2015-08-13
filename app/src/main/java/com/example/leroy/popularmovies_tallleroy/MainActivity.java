package com.example.leroy.popularmovies_tallleroy;

import android.app.AlertDialog;
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


public class MainActivity extends AppCompatActivity implements PostersAdapter.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MovieSummary.setActivity(this);
        // start up the sync activity to retrieve our posters from themoviedb
        if(savedInstanceState == null) {
            checkForAPI_key();
            SyncAdapter.initializeSyncAdapter(this);
        } else {
            SyncAdapter.syncImmediately(this);
        }
        setContentView(R.layout.activity_main);
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

//            userInput.setText(apiKey);

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
        // as you specify a parent activity in AndroidManifest.xml.
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
        // new item selected, start the detail activity
        Intent detailIntent = new Intent(this, MovieDetailActivity.class);
        detailIntent.putExtra(getString(R.string.movieSummaryExtra), movieSummary);
        startActivity(detailIntent);
    }

}
