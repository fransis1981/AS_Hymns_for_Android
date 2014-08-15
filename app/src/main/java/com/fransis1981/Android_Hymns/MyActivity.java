package com.fransis1981.Android_Hymns;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;


public class MyActivity extends ActionBarActivity {

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

    /** Called when the activity is first created.  */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    //TODO: this method is currently
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
}
