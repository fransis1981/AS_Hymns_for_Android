package com.fransis1981.Android_Hymns;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/*
 * This activity is invoked when there is a search to perform. Search keywords are passed
 * as a string extra to the intent, under the SearchManager.QUERY key.
 */
public class SearchActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>,
                                                                ListView.OnItemClickListener {
    private static final int SEARCHRESULTS_LOADER = 0;
    private static final int PROGRESS_DIALOG = 0;
    private static final String DIALOGSHOWN_BUNDLESTATE = "DlgShown";

    String mSearchKeywords;
    SearchResultsCursorAdapter mCa;
    ListView mLv;
    boolean mDialogShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_search);

        if (savedInstanceState != null) {
            mDialogShown = savedInstanceState.getBoolean(DIALOGSHOWN_BUNDLESTATE);
        } else {
            mDialogShown = false;
        }
        mSearchKeywords = getIntent().getStringExtra(SearchManager.QUERY);

        //Preparing cursor adapter
        mCa = new SearchResultsCursorAdapter(
                this,
                R.layout.searchresults_threerows_item,
                null,      //no cursor yet
                new String[] {MyConstants.FIELD_INNI_ID_INNARIO,
                              MyConstants.FTS_FIELD_INNI_ID,
                              MyConstants.FIELD_INNI_TITOLO,
                              MyConstants.FIELD_STROFE_TESTO},
                new int[] {R.id.searchresults_hymnbook_title, R.id.searchresults_hymn_number,
                           R.id.searchresults_hymn_title, R.id.searchresults_snippet},
                mSearchKeywords
        );
        mLv = (ListView) findViewById(android.R.id.list);
        mLv.setAdapter(mCa);
        mLv.setOnItemClickListener(this);

        if (MyConstants.DEBUG) Log.i(MyConstants.LogTag_STR, String.format("onCreate(): %d elements in the cursor.", mCa.getCount()));
        LoaderManager _lm = getSupportLoaderManager();
        _lm.initLoader(SEARCHRESULTS_LOADER, null, this);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(DIALOGSHOWN_BUNDLESTATE, mDialogShown);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(MyConstants.LogTag_STR, String.format("Tapped item with id %d", id));
        setResult(Activity.RESULT_OK, new Intent().setData(Uri.parse(String.valueOf(id))));
        finish();
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                ProgressDialog _pg = new ProgressDialog(this);
                _pg.setMessage(getString(R.string.searchprogressdialog_message));
                _pg.setTitle(getString(R.string.searchprogressdialog_title));
                _pg.setIndeterminateDrawable(HymnsApplication.myResources.getDrawable(R.drawable.indeterminate_spinner));
                _pg.setIndeterminate(true);
                _pg.setCanceledOnTouchOutside(false);
                _pg.setCancelable(false);
                return _pg;
        }
        return super.onCreateDialog(id);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (MyConstants.DEBUG) Log.i(MyConstants.LogTag_STR, "onCreateLoader() invoked!");
        mDialogShown = true;
        showDialog(PROGRESS_DIALOG);
        switch (id) {
            case SEARCHRESULTS_LOADER:
                return new CursorLoader(
                        this,               // Parent activity context
                        Uri.withAppendedPath(HymnsSuggestionsProvider.PROVIDER_URI_HYMNSFTS, mSearchKeywords),
                        null,               // Projection to return
                        null,               // No selection clause
                        null,               // No selection arguments
                        null                // Default sort order
                );

            default:
                return null;        // An invalid id was passed in
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (MyConstants.DEBUG) Log.i(MyConstants.LogTag_STR, "onLoadFinished() invoked!");
        if (mDialogShown) {
            mDialogShown = false;
            dismissDialog(PROGRESS_DIALOG);
        }
        if (data.getCount() > 0) {
            mCa.swapCursor(data);
        }
        else {
            Toast.makeText(this, getString(R.string.msg_empty_results), Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        try {
            mCa.changeCursor(null);
        }
        catch (Exception e) {}
    }


    @Override
    protected void onDestroy() {
        mCa = null;
        super.onDestroy();
    }
}
