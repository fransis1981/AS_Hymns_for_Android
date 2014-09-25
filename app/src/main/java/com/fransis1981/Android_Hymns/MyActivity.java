package com.fransis1981.Android_Hymns;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


public class MyActivity extends ActionBarActivity {

    //Tag string constant to associate with the FTS worker fragment.
    //private static final String TAG_FTSWORKER_FRAGMENT = "FTS_WORKER";

   //Using symbolic constants for menu items, as by convention.
   private static final int MENU_PREFERENCES = Menu.FIRST;

    private static final int ACTIVITYRESULT_SEARCHHYMN = 0;

   //Constants for bundle arguments
   static final String TAB_BUNDLESTATE = "SelectedTab";
   static final String CATEGORIASELECTION_BUNDLESTATE = "SelectedCategory";
   static final String INNARIOSELECTION_BUNDLESTATE = "SelectedHymnBook";

    //Dictionary preference for the positioning of the controls fragment in tablet mode.
    static final String PREF_Controls_On_The_Left = "Controls_On_The_Left";

    //This field keeps the ID of the frame where the hymns fragment gets instantiated.
    private int currentHymnsContainerID = 0;

    //This field is populated upon creation to reduce boiler plate code.
    private boolean _tabletMode;

    private MenuItem mSearchMenuItem;

    FragmentManager _fm;
    SingleHymn_Fragment singleHymn_fragment;
    boolean mFTSServiceWorking = false;
    ProgressReceiver mReceiver;
    android.support.v7.widget.SearchView mSearchView;


    /** Called when the activity is first created.  */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_PROGRESS);

        _tabletMode = HymnsApplication.myResources.getBoolean(R.bool.isTableLayout);
        _fm = getSupportFragmentManager();
        currentHymnsContainerID = R.id.right_pane;

        try {
           setContentView(R.layout.main);

            //Retrieving last saved or default positioning for the controls fragment.
            SharedPreferences sp = this.getSharedPreferences(HymnsApplication.HelperPreferences_STR, Context.MODE_PRIVATE);
            boolean on_the_left = sp.getBoolean(PREF_Controls_On_The_Left,
                    HymnsApplication.myResources.getBoolean(R.bool.default_controls_on_the_left));

            //deployFragments(on_the_left);
            if (_tabletMode) {
                singleHymn_fragment = (SingleHymn_Fragment) _fm.findFragmentById(currentHymnsContainerID);
                singleHymn_fragment.showHymn(HymnsApplication.getCurrentInnario().getInno(1));
            }

            ActionBar ab = getSupportActionBar();
            setSupportProgressBarIndeterminate(false);


        } catch (Exception e) {
            Log.e(MyConstants.LogTag_STR, "CATCHED SOMETHING WHILE CREATNG MAIN ACTIVITY GUI...." + e.getMessage());
            e.printStackTrace();
        }
   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mSearchMenuItem = menu.findItem(R.id.mnu_search);
        mSearchMenuItem.setVisible(HymnBooksHelper.me().isFTSAvailable());
        SearchManager _sm = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchMenuItem);
        mSearchView.setSearchableInfo(_sm.getSearchableInfo(getComponentName()));

        menu.findItem(R.id.mnu_app_settings).setIntent(new Intent(this, PrefsActivity.class));
        menu.findItem(R.id.mnu_system_search_options).setIntent(new Intent(Settings.ACTION_SEARCH_SETTINGS));

        return true;
    }


    @Override
    protected void onResume() {
        if (!HymnBooksHelper.me().isFTSAvailable() && !mFTSServiceWorking) {
            manageFTSServiceStart();
        }
        else if (!HymnBooksHelper.me().isFTSAvailable() && mFTSServiceWorking) {
            try {
                if (mReceiver == null) mReceiver = new ProgressReceiver();
                IntentFilter _ifilter = new IntentFilter(FTSIndexerSvc.FTS_INDEX_PROGRESSED);
                registerReceiver(mReceiver, _ifilter);
            }
            catch (Exception e) {
                Log.e(MyConstants.LogTag_STR, "BAD ISSUE while registering the progress receiver: " + e.getMessage());
            }
        }
        else if (HymnBooksHelper.me().isFTSAvailable() && mFTSServiceWorking) {
            manageFTSServiceEnd();
        }

        super.onResume();
    }


    @Override
    protected void onPause() {
        if (mFTSServiceWorking) {
            try {
                unregisterReceiver(mReceiver);
                mReceiver = null;
            }
            catch (Exception e) {
            }
        }

        super.onPause();
    }


    @Override
    protected void onDestroy() {
        //Saving preferences (recents and starred) before the activity gets destroyed
        HymnsApplication.getRecentsManager().saveToPreferences(this);
        HymnsApplication.getStarManager().saveToPreferences(this);
        super.onDestroy();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String _qry = intent.getStringExtra(SearchManager.QUERY).trim();
            if (_qry.length() == 1) {
                Toast t = Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.msg_single_char_searches),
                        Toast.LENGTH_SHORT
                );
                t.setGravity(Gravity.TOP, 0, 0);
                t.show();
                return;
            }
            startActivityForResult(
                    new Intent(this, SearchActivity.class).putExtra(SearchManager.QUERY, _qry),
                    ACTIVITYRESULT_SEARCHHYMN
            );
        }
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String _s = intent.getDataString().trim();
            if (_s.length() > 0) {
                if (MyConstants.DEBUG) Log.i(MyConstants.LogTag_STR, "Intercepted ACTION_VIEW intent from suggestion with data " + _s);
                try {
                    callback_HymnSelected(Inno.findInnoById(Long.parseLong(_s)));
                }
                catch (InnoNotFoundException infe) {
                    Log.e(MyConstants.LogTag_STR, "Suggestion clicked: inno not found exception ---> this should be impossible.");
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MenuItemCompat.collapseActionView(mSearchMenuItem);

        //Got a single possible request code for now, so not implementing switch statement.
        if (resultCode == Activity.RESULT_OK) {
            String _s = data.getDataString();
            try {
                callback_HymnSelected(Inno.findInnoById(Long.parseLong(_s)));
            }
            catch (InnoNotFoundException infe) {
                Log.e(MyConstants.LogTag_STR, "Search result clicked: inno not found exception ---> this should be impossible.");
            }
        }
    }


    /*
     * This method is used to discriminate between different kinds of layouts.
     */
    void callback_HymnSelected(Inno inno) {
        if (_tabletMode) {
            singleHymn_fragment.showHymn(inno);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else {
            SingleHymn_Activity.startIntentWithHymn(this, inno);
        }
    }


    //TODO: this method is currently unused (waiting for the implementation of dynamic fragments)
    private void deployFragments(boolean prm_controls_on_left) {
        //An additional check for tablet mode.
        if (!_tabletMode) return;

        SingleHymn_Fragment _shf = (SingleHymn_Fragment)
                SingleHymn_Fragment.instantiate(this, SingleHymn_Fragment.class.getName());
        MyMainFragment _mmf = (MyMainFragment)
                MyMainFragment.instantiate(this, MyMainFragment.class.getName());
        FragmentTransaction _ft;

        if (prm_controls_on_left) {
            currentHymnsContainerID = R.id.right_pane;
            _ft = _fm.beginTransaction();
            _ft.replace(R.id.left_pane, _mmf).commit();     //TODO: should use add instead of replace?
        }
        else {
            currentHymnsContainerID = R.id.left_pane;
            _ft = _fm.beginTransaction();
            _ft.replace(R.id.right_pane, _mmf).commit();
        }
        _ft = _fm.beginTransaction();
        _ft.replace(currentHymnsContainerID, _shf).commit();
        _shf.showHymn(HymnsApplication.getCurrentInnario().getInno(1));
    }


    /*
     * Helper method for taking all actions related to FTS Indexer service start
     * (e.g., preparing receiver, progress bar, etc.)
     */
    void manageFTSServiceStart() {
        setSupportProgressBarVisibility(true);
        mFTSServiceWorking = true;
        IntentFilter _ifilter = new IntentFilter(FTSIndexerSvc.FTS_INDEX_PROGRESSED);
        mReceiver = new ProgressReceiver();
        registerReceiver(mReceiver, _ifilter);
        startService(new Intent(this, FTSIndexerSvc.class));
    }

    /*
     * Helper method for taking all actions related to FTS Indexer service end. (Note that the service self stops).
     * (e.g., nulling the receiver, hide progress bar, etc.)
     */
    void manageFTSServiceEnd() {
        setSupportProgressBarVisibility(false);
        mFTSServiceWorking = false;
        if (!mSearchMenuItem.isVisible()) supportInvalidateOptionsMenu();
        try {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        catch (Exception e) {
            Log.e(MyConstants.LogTag_STR, "MyActivity.manageFTSServiceEnd(): " + e.getMessage());
        }
    }


    /**********************************************************************
     Class used to receive progress broadcast updates from the FTS indexer service.
     **********************************************************************/
    private class ProgressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int _val = intent.getIntExtra(FTSIndexerSvc.FTS_PROGRESS_EXTRA_VAL, 0);
            if (_val == FTSIndexerSvc.FTS_PROGRESS_COMPLETE) {
                manageFTSServiceEnd();
                Toast t = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);
                LayoutInflater li = LayoutInflater.from(context);
                View v = li.inflate(R.layout.toast_indexing_finished, null);
                t.setView(v);
                t.show();
            }
            else {
                setSupportProgress(_val);
            }
        }
    }       //END class ProgressReceiver

}
