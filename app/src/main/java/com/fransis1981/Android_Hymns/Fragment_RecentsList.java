package com.fransis1981.Android_Hymns;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * Created by Fransis on 05/03/14 11.38.
 */
public class Fragment_RecentsList extends Fragment implements UpdateContentItf {
   private ListView _recentslist;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.mainscreen_fragment_recentslist, container, false);
      MRUManager _recentsManager = HymnsApplication.getRecentsManager();
      _recentslist = (ListView) rootView.findViewById(R.id.list_recent_hymns);
      _recentslist.setAdapter(new
            Inni2RowsAdapter(getActivity(), R.layout.mainscreen_fragment_recentslist, _recentsManager.getMRUList()));
      _recentslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Inno clicked_inno = (Inno) parent.getAdapter().getItem(position);
            ((MyActivity) getActivity()).callback_HymnSelected(clicked_inno);
         }
      });

      //Treating button for clearing history
      Button btn_clear = (Button) rootView.findViewById(R.id.btn_clear_history);
      btn_clear.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            HymnsApplication.getRecentsManager().clearMRU();
         }
      });

      return rootView;
   }

   public void updateContent() {
      try {
         if (_recentslist != null) ((ArrayAdapter<Inno>) _recentslist.getAdapter()).notifyDataSetChanged();
      } catch (Exception e) {
         //Log.w(MyConstants.LogTag_STR, "Catched EXCEPTION in Fragment Recents while in updateContent().");
      }
   }
}