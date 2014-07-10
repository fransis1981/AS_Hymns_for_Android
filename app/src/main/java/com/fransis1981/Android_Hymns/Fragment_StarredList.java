package com.fransis1981.Android_Hymns;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Fransis on 05/03/14 11.38.
 */
public class Fragment_StarredList extends Fragment implements UpdateContentItf {
   private ListView _starredlist;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.mainscreen_fragment_starredlist, container, false);
      StarManager _starManager = HymnsApplication.getStarManager();
      _starredlist = (ListView) rootView.findViewById(R.id.list_starred_hymns);
      _starredlist.setAdapter(new
            Inni2RowsAdapter(getActivity(), R.layout.mainscreen_fragment_starredlist, _starManager.getStarredList()));
      _starredlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Inno clicked_inno = (Inno) parent.getAdapter().getItem(position);
            ((MyActivity) getActivity()).callback_HymnSelected(clicked_inno);
         }
      });

      return rootView;
   }



   public void updateContent() {
      try {
         if (_starredlist != null) ((ArrayAdapter<Inno>) _starredlist.getAdapter()).notifyDataSetChanged();
      } catch (Exception e) {
         //Log.w(MyConstants.LogTag_STR, "Catched EXCEPTION in Fragment Starred while in updateContent().");
      }
   }
}