package com.fransis1981.Android_Hymns;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.fransis1981.Android_Hymns.R;

public class SearchActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ActionBar ab = getSupportActionBar();
        ab.hide();

        handleIntent(getIntent());
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    //Discriminate between ACTION_SEARCH and ACTION_VIEW.
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String _qry = intent.getStringExtra(SearchManager.QUERY);
            //TODO: process Cursor and display results
        }
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String _s = intent.getDataString();
            if (_s.length() > 0) {
                if (MyConstants.DEBUG) Log.i(MyConstants.LogTag_STR, "Intercepted an ACTION_VIEW from suggestion with data " + _s);

            }
        }
    }       //END handleIntent()

}
