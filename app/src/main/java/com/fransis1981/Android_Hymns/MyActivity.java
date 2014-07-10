package com.fransis1981.Android_Hymns;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MyActivity extends FragmentActivity {

   //Using symbolic constants for menu items, as by convention.
   private static final int MENU_PREFERENCES = Menu.FIRST;

   //Constants for bundle arguments
   static final String TAB_BUNDLESTATE = "SelectedTab";
   static final String CATEGORIASELECTION_BUNDLESTATE = "SelectedCategory";
   static final String INNARIOSELECTION_BUNDLESTATE = "SelectedHymnBook";

    /** Called when the activity is first created.  */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
           setContentView(R.layout.main);
           //_context = this;

           //initUI();

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
      MenuItem mnu_pref = menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.mnu_options_str);
      mnu_pref.setIcon(android.R.drawable.ic_menu_preferences);
      return true;
   }

//   @Override
//   protected void onRestoreInstanceState(Bundle savedInstanceState) {
//      super.onRestoreInstanceState(savedInstanceState);
//
//      if (savedInstanceState != null) {
//         currentCategoriaSelection = savedInstanceState.getInt(CATEGORIASELECTION_BUNDLESTATE);
//         currentInnariSelection = savedInstanceState.getInt(INNARIOSELECTION_BUNDLESTATE);
//         mTabHost.setCurrentTabByTag(savedInstanceState.getString(TAB_BUNDLESTATE, MyConstants.TAB_MAIN_KEYPAD));
//      }
//   }
//
//   @Override
//   protected void onSaveInstanceState(Bundle outState) {
//      outState.putString(TAB_BUNDLESTATE, mTabHost.getCurrentTabTag());
//      outState.putInt(CATEGORIASELECTION_BUNDLESTATE, currentCategoriaSelection);
//      outState.putInt(INNARIOSELECTION_BUNDLESTATE, currentInnariSelection);
//      super.onSaveInstanceState(outState);
//   }

   @Override
   protected void onDestroy() {
      //Saving preferences (recents and starred) before the activity gets destroyed
      HymnsApplication.getRecentsManager().saveToPreferences(this);
      HymnsApplication.getStarManager().saveToPreferences(this);
      super.onDestroy();
   }

//   @Override
//   protected void onPause() {
//      currentInnariSelection = mSpinnerInnari.getSelectedItemPosition();
//      currentCategoriaSelection = mSpinnerCategoria.getSelectedItemPosition();
//      super.onPause();
//   }

//   //Using this callback to manage spinners' state.
//   @Override
//   protected void onResume() {
//      super.onResume();
//      if (currentInnariSelection == -1 && currentCategoriaSelection == -1) mSpinnerInnari.setSelection(1);
//      else {
//         if (currentInnariSelection > 0) mSpinnerInnari.setSelection(currentInnariSelection);
//         else if (currentCategoriaSelection > 0) mSpinnerCategoria.setSelection(currentCategoriaSelection);
//      }
//   }

   //This method is used to discriminate between different kinds of layouts.
   void callback_HymnSelected(Inno inno) {
      //TODO: discriminate implementation between tablet and handset
      SingleHymn_Activity.startIntentWithHymn(this, inno);
   }
}
