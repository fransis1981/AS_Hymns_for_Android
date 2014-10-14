package com.fransis1981.Android_Hymns;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

/**
 * Created by Fransis on 05/03/14 11.56.
 */
public class MainScreenPagerAdapter extends FragmentPagerAdapter
      implements HymnsApplication.OnCurrentInnarioChangedListener,
                 StarManager.StarredItemsChangedListener,
                 MRUManager.MRUStateChangedListener {

   public static enum FragmentContextEnum {
      KEYPAD,
      LIST,
      RECENTS,
      STARRED;

      public static FragmentContextEnum parseInt(int i) {
         switch (i) {
            case 0: return KEYPAD;
            case 1: return LIST;
            case 2: return RECENTS;
            case 3: return STARRED;
         }
         return KEYPAD;
      }
   }

   FragmentManager _fm;

   //Preallocating main fragments, each having its own layout and features
   Fragment_Keypad _fragment_keypad;
   Fragment_HymnsList _fragment_hymnslist;
   Fragment_RecentsList _fragment_recent;
   Fragment_StarredList _fragment_starred;

   FragmentContextEnum fragmentContext;     //Used to identify currently shown fragment.

   public MainScreenPagerAdapter(FragmentManager fm, Fragment_Keypad fk, Fragment_HymnsList fh, Fragment_RecentsList fr, Fragment_StarredList fs) {
      super(fm);
      _fm = fm;
      _fragment_keypad = fk;
      _fragment_hymnslist = fh;
      _fragment_recent = fr;
      _fragment_starred = fs;
      fragmentContext = FragmentContextEnum.KEYPAD;
      bindEventListeners();
   }


   void bindEventListeners() {
      //Listening to events for triggering fragment updates.
      HymnsApplication.setOnCurrentInnarioChangedListener(this);

      HymnsApplication.getRecentsManager().setMruStateChangedListener(this);

      HymnsApplication.getStarManager().setStarredItemsChangedListener(this);
   }

   public void setCurrentFragmentContext(int i) {
      if (fragmentContext == FragmentContextEnum.KEYPAD)
         ((Fragment_Keypad) getFragmentByPos(FragmentContextEnum.KEYPAD.ordinal())).abortKeypadTimeout();
      fragmentContext = FragmentContextEnum.parseInt(i);
   }

   @Override
   public int getCount() {
      return 4;
         //Si prevede di avere i seguenti tabs:
         //    - keypad
         //    - lista degli inni
         //    - i più cantati recentemente
         //    - i preferiti
   }

   @Override
   public CharSequence getPageTitle(int position) {
      switch (position) {
         case 0: return MyConstants.TAB_MAIN_KEYPAD;
         case 1: return MyConstants.TAB_MAIN_HYMNSLIST;
         case 2: return MyConstants.TAB_MAIN_RECENT;
         case 3: return MyConstants.TAB_MAIN_STARRED;
         default: return "Unexpected!!!";
      }
   }

   @Override
   public Fragment getItem(int i) {
      switch (i) {
         case 0: return (_fragment_keypad == null)?_fragment_keypad = new Fragment_Keypad():_fragment_keypad;
         case 1: return (_fragment_hymnslist == null)?_fragment_hymnslist = new Fragment_HymnsList():_fragment_hymnslist;
         case 2: return (_fragment_recent == null)?_fragment_recent = new Fragment_RecentsList():_fragment_recent;
         case 3: return (_fragment_starred == null)?_fragment_starred = new Fragment_StarredList():_fragment_starred;
         default: return null;
      }
   }

   @Override
   public void onCurrentInnarioChanged() {
      updateFragmentContent(0);
      updateFragmentContent(1);
   }


   @Override
   public void OnStarredItemsChanged() {
      if (MyConstants.DEBUG) Log.i(MyConstants.LogTag_STR, "Updating fragments with fresh new STAR information! Selected tab:" + fragmentContext.ordinal());
      for (int i = 1; i <= 3; i++)
         updateFragmentContent(i);
   }


   @Override
   public void OnMRUStateChanged() {
      //Log.i(MyConstants.LogTag_STR, "Updating fragments with fresh news RECENT information!");
      updateFragmentContent(2);
   }


   private String getFragmentTag(int pos) {
      return "android:switcher:" + R.id.main_viewpager + ":" + pos;
   }


   Fragment getFragmentByPos(int pos) {
         return _fm.findFragmentByTag(getFragmentTag(pos));
   }


   //This is a wrapper method for calling udpateContent on a given fragment. Useful for external callbacks.
   void updateFragmentContent(int pos) {
      try {
         ((UpdateContentItf) getFragmentByPos(pos)).updateContent();
      } catch (Exception e) {
          if (MyConstants.DEBUG) Log.e(MyConstants.LogTag_STR, "Just to avoid app crashes on null fragments if the SingleHymn activity is active.");
      }
   }

}
