package com.fransis1981.Android_Hymns;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


public class MyActivity extends ActionBarActivity
                        implements FTSWorkerFragment.TaskCallbacks {

    //Tag string constant to associate with the FTS worker fragment.
    private static final String TAG_FTSWORKER_FRAGMENT = "FTS_WORKER";

   //Using symbolic constants for menu items, as by convention.
   private static final int MENU_PREFERENCES = Menu.FIRST;

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

    FragmentManager _fm;
    SingleHymn_Fragment singleHymn_fragment;
    FTSWorkerFragment mFTSFragment;

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

            // If the Fragment is non-null, then it is currently being retained across a configuration change.
            // IMPORTANT NOTE: Currently I am not retaining instance because of the issue (see inline comment below).
            //TODO: Actually create FTS wroker fragment if FTS table does not exist.
            if (HymnBooksHelper.me().isBuildingFTS() || !HymnBooksHelper.me().isFTSAvailable())
            if (mFTSFragment == null) {
                mFTSFragment = new FTSWorkerFragment();
                mFTSFragment.setTargetFragment(mFTSFragment, 0);    //[QUICK-FIX_API<13-NOT WORKING]: https://code.google.com/p/android/issues/detail?id=22564
                _fm.beginTransaction().add(mFTSFragment, TAG_FTSWORKER_FRAGMENT).commit();
            }

        } catch (Exception e) {
            Log.e(MyConstants.LogTag_STR, "CATCHED SOMETHING WHILE CREATNG MAIN ACTIVITY GUI...." + e.getMessage());
            e.printStackTrace();
        }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      //TODO: se mi conservo a livello di classe il puntatore al menu, posso cambiarne il contenuto a run-time
      //TODO: fintanto che questo metodo non viene di nuovo invocato; lo stesso vale per i singoli MenuItem.
      super.onCreateOptionsMenu(menu);
      //MenuItem mnu_pref = menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.mnu_move_controls_on_the_right);

      return true;
   }


   @Override
   protected void onDestroy() {
      //Saving preferences (recents and starred) before the activity gets destroyed
      HymnsApplication.getRecentsManager().saveToPreferences(this);
      HymnsApplication.getStarManager().saveToPreferences(this);
      super.onDestroy();
   }

   //This method is used to discriminate between different kinds of layouts.
   void callback_HymnSelected(Inno inno) {
      if (_tabletMode) {
          singleHymn_fragment.showHymn(inno);
          getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      }
      else {
          SingleHymn_Activity.startIntentWithHymn(this, inno);
      }
   }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Removing fragment and reattaching it if activity gets re-created.
        //The reason of this: https://code.google.com/p/android/issues/detail?id=22564
        if (mFTSFragment != null) {
            mFTSFragment.mTask.cancel(true);
            _fm.beginTransaction().remove(mFTSFragment).commit();
            mFTSFragment = null;
        }

        super.onSaveInstanceState(outState);
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
            _ft.replace(R.id.left_pane, _mmf).commit();
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

    /**********************************************************************
                    FTS Worker Fragment callbacks
     **********************************************************************/
    @Override
    public void onPreExecute() {
        setSupportProgressBarVisibility(true);
    }

    @Override
    public void onProgressUpdate(int val) {
        //Maximum displayable action bar progress value is 9999.
        setSupportProgress(val);
    }

    @Override
    public void onCancelled(String reason) {
        String txt = reason.length() == 0 ?
                      HymnsApplication.myResources.getString(R.string.fts_worker_cancel_reason0) : reason;
        Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPostExecute() {
        setSupportProgressBarVisibility(false);
    }
}
