package com.fransis1981.Android_Hymns;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

/**
 * Created by Fransis on 14/04/14 20.02.
 */
public class MyMainFragment extends Fragment
   implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener,
              Spinner.OnItemSelectedListener {

   class MainTabFactory implements TabHost.TabContentFactory {
      private final Context mContext;
      private View v;            //One-for-all dummy View pointers

      public MainTabFactory(Context context) {
         mContext = context;
         v = new View(mContext);
         v.setMinimumWidth(0);
         v.setMinimumHeight(0);
      }

      public View createTabContent(String tag) {
         return v;
      }
   }

    private static final String TAG_FTSWORKER_FRAGMENT = "FTS_WORKER";

    Context _context;
   MainScreenPagerAdapter mPagerAdapter;
   ViewPager mViewPager;
   TabHost mTabHost;

   Fragment_Keypad fragment_keypad;
   Fragment_HymnsList fragment_hymnslist;
   Fragment_RecentsList fragment_recent;
   Fragment_StarredList fragment_starred;

   TextView lblCategorie, lblInnari;
   Spinner mSpinnerInnari, mSpinnerCategoria;
   ArrayAdapter<String> spin_innariAdapter, spin_catAdapter;

   int currentInnariSelection = -1, currentCategoriaSelection = -1;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View v = inflater.inflate(R.layout.main_fragment, container, false);
      try {
         _context = getActivity();

         initUI(v);

      } catch (Exception e) {
         Log.e(MyConstants.LogTag_STR, "CATCHED SOMETHING WHILE CREATING MAIN FRAGMENT GUI...." + e.getMessage());
         e.printStackTrace();
      }

      if (savedInstanceState != null) {
         currentCategoriaSelection = savedInstanceState.getInt(MyActivity.CATEGORIASELECTION_BUNDLESTATE);
         currentInnariSelection = savedInstanceState.getInt(MyActivity.INNARIOSELECTION_BUNDLESTATE);
         String sss = savedInstanceState.getString(MyActivity.TAB_BUNDLESTATE);
         mTabHost.setCurrentTabByTag((sss == null)? MyConstants.TAB_MAIN_KEYPAD : sss);
      }

      return v;
   }

   private void initUI(View v) throws Exception {
      lblCategorie = (TextView) v.findViewById(R.id.lbl_categoria);
      lblInnari = (TextView) v.findViewById(R.id.lbl_innari);

      //Treating spinner innari
      mSpinnerInnari = (Spinner) v.findViewById(R.id.spinner_innari);
      spin_innariAdapter =
            new ArrayAdapter<String>(_context, android.R.layout.simple_spinner_item, HymnsApplication.getInnariTitles());
      spin_innariAdapter.insert(HymnsApplication.myResources.getString(R.string.generic_categoria_spinner_label), 0);
      spin_innariAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      mSpinnerInnari.setAdapter(spin_innariAdapter);
      mSpinnerInnari.setSelection(1);
      mSpinnerInnari.setOnItemSelectedListener(this);

      //Treating spinner categoria
      mSpinnerCategoria = (Spinner) v.findViewById(R.id.spinner_categoria);
      spin_catAdapter =
            new ArrayAdapter<String>(_context, android.R.layout.simple_spinner_item, Inno.Categoria.getCategoriesStringList());
      spin_catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      mSpinnerCategoria.setAdapter(spin_catAdapter);
      mSpinnerCategoria.setOnItemSelectedListener(this);

      //Treating tabs
      mTabHost = (TabHost) v.findViewById(android.R.id.tabhost);
      mTabHost.setup();
      addTabToTabHost(MyConstants.TAB_MAIN_KEYPAD, HymnsApplication.myResources.getDrawable(android.R.drawable.ic_dialog_dialer));
      addTabToTabHost(MyConstants.TAB_MAIN_HYMNSLIST, null);
      addTabToTabHost(MyConstants.TAB_MAIN_RECENT, null);
      addTabToTabHost(MyConstants.TAB_MAIN_STARRED, null);
      mTabHost.setOnTabChangedListener(this);

      //Treating ViewPager, fragments and related adapter
      if (fragment_keypad == null)
         fragment_keypad = (Fragment_Keypad) Fragment_Keypad.instantiate(_context, Fragment_Keypad.class.getName());

      if (fragment_hymnslist == null)
         fragment_hymnslist = (Fragment_HymnsList) Fragment_HymnsList.instantiate(_context, Fragment_HymnsList.class.getName());

      if (fragment_recent == null)
         fragment_recent = (Fragment_RecentsList) Fragment_RecentsList.instantiate(_context, Fragment_RecentsList.class.getName());

      if (fragment_starred == null)
         fragment_starred = (Fragment_StarredList) Fragment_StarredList.instantiate(_context, Fragment_StarredList.class.getName());

      mPagerAdapter = new MainScreenPagerAdapter(((FragmentActivity)getActivity()).getSupportFragmentManager(),
                                          fragment_keypad, fragment_hymnslist,fragment_recent, fragment_starred);
      mViewPager = (ViewPager) v.findViewById(R.id.main_viewpager);
      mViewPager.setAdapter(mPagerAdapter);
      mViewPager.setOnPageChangeListener(this);
      mViewPager.setOffscreenPageLimit(4);

      mPagerAdapter.bindEventListeners();
   }

   @Override
   public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      if (parent == mSpinnerInnari) {
         //Log.i(MyConstants.LogTag_STR, "A selection happened in the spinner Innari!!!! [" + selected_str + "]");
         //Handling selection on spinner Innari; 1st useful element is at index 1.

         //Se non si seleziona nessun innario, si seleziona almeno una categoria diversa da "NESSUNA"
         if (position == 0) {
            if (mSpinnerCategoria.getSelectedItemPosition() == 0) mSpinnerCategoria.setSelection(1);
         } else {
            String selected_str = (String) parent.getItemAtPosition(position);
            mSpinnerCategoria.setSelection(0);
            HymnsApplication.setCurrentInnario(selected_str);
            highlightLabelInnari();
            String sss = mTabHost.getCurrentTabTag();
            if (sss.equalsIgnoreCase(MyConstants.TAB_MAIN_RECENT) || sss.equalsIgnoreCase(MyConstants.TAB_MAIN_STARRED))
                mTabHost.setCurrentTabByTag(MyConstants.TAB_MAIN_HYMNSLIST);
         }
         currentInnariSelection = position;
      }
      else if (parent == mSpinnerCategoria) {
         //Handling selection on spinner Categoria
         if (position == 0) {
            if (mSpinnerInnari.getSelectedItemPosition() == 0) mSpinnerInnari.setSelection(1);
         } else {
            Inno.Categoria cat = Inno.Categoria.parseString((String) mSpinnerCategoria.getItemAtPosition(position));
            mSpinnerInnari.setSelection(0);
            HymnsApplication.setCurrentInnario(cat);
            highlightLabelCategoria();
            mTabHost.setCurrentTabByTag(MyConstants.TAB_MAIN_HYMNSLIST);
         }
      }
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      outState.putString(MyActivity.TAB_BUNDLESTATE, mTabHost.getCurrentTabTag());
      outState.putInt(MyActivity.CATEGORIASELECTION_BUNDLESTATE, currentCategoriaSelection);
      outState.putInt(MyActivity.INNARIOSELECTION_BUNDLESTATE, currentInnariSelection);
      super.onSaveInstanceState(outState);
   }


   @Override
   public void onPause() {
      currentInnariSelection = mSpinnerInnari.getSelectedItemPosition();
      currentCategoriaSelection = mSpinnerCategoria.getSelectedItemPosition();
      super.onPause();
   }

   @Override
   public void onResume() {
      super.onResume();
      if (currentInnariSelection == -1 && currentCategoriaSelection == -1) mSpinnerInnari.setSelection(1);
      else {
         if (currentInnariSelection > 0) mSpinnerInnari.setSelection(currentInnariSelection);
         else if (currentCategoriaSelection > 0) mSpinnerCategoria.setSelection(currentCategoriaSelection);
      }
   }

//   @Override
//   public void onConfigurationChanged(Configuration newConfig) {
//      super.onConfigurationChanged(newConfig);
//
//      try {
//         LayoutInflater inflater = LayoutInflater.from(getActivity());
//         ViewGroup vg = ((ViewGroup) getView());
//         vg.removeAllViewsInLayout();
//         View v = inflater.inflate(R.layout.main_fragment, vg);
//
//         initUI(v);
//      } catch (Exception e) {
//         Log.e(MyConstants.LogTag_STR, "CATCHED SOMETHING WHILE CHANGING MAIN FRAGMENT ORIENTATION...." + e.getMessage());
//         e.printStackTrace();
//      }
//   }

   @Override
   public void onNothingSelected(AdapterView<?> parent) {

   }

   @Override
   public void onPageScrollStateChanged(int i) {
      //TODO: Da approfondire la semantica di questo listener.
   }

   @Override
   public void onPageScrolled(int i, float v, int i2) {      //TODO: Da approfondire la semantica di questo listener.

   }

   @Override
   public void onPageSelected(int i) {
      mTabHost.setCurrentTab(i);
      mPagerAdapter.setCurrentFragmentContext(i);
   }

   @Override
   public void onTabChanged(String tabId) {
      int i = mTabHost.getCurrentTab();
      mViewPager.setCurrentItem(i, true);
   }

   private void addTabToTabHost(String _tabName, Drawable _drawable) {
      TabHost.TabSpec ts = mTabHost.newTabSpec(_tabName);
      View v = LayoutInflater.from(_context).inflate(R.layout.tab_indicator_holo, mTabHost.getTabWidget(), false);
      ((TextView) v.findViewById(android.R.id.title)).setText(_tabName);
      ts.setIndicator(v);
      //Managing exception: you must specify a way to create the tab content (even if producing here dummy views).
      ts.setContent(new MainTabFactory(_context));
      mTabHost.addTab(ts);
   }

   /*
 * Call this method to make reverse color on the label innari; convenience method to save status between orientations.
 * This method eventually also remove special formatting from the label categoria.
 */
   private void highlightLabelInnari() {
      lblInnari.setTextAppearance(_context, R.style.spinners_labels_style_inverse);
      lblInnari.setBackgroundColor(HymnsApplication.myResources.getColor(android.R.color.white));
      lblCategorie.setTextAppearance(_context, R.style.spinners_labels_style_direct);
      lblCategorie.setBackgroundColor(HymnsApplication.myResources.getColor(android.R.color.transparent));
   }

   /*
    * Call this method to make reverse color on the label categoria; convenience method to save status between orientations.
    * This method eventually also remove special formatting from the label innari.
    */
   private void highlightLabelCategoria() {
      lblCategorie.setTextAppearance(_context, R.style.spinners_labels_style_inverse);
      lblCategorie.setBackgroundColor(HymnsApplication.myResources.getColor(android.R.color.white));
      lblInnari.setTextAppearance(_context, R.style.spinners_labels_style_direct);
      lblInnari.setBackgroundColor(HymnsApplication.myResources.getColor(android.R.color.transparent));
   }


}