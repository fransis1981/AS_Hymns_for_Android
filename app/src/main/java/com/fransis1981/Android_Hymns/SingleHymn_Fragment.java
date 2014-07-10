package com.fransis1981.Android_Hymns;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by Fransis on 27/02/14 15.08.
 */
public class SingleHymn_Fragment extends ListFragment {

   TextView txt_number, title;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      //return super.onCreateView(inflater, container, savedInstanceState);
      View v = inflater.inflate(R.layout.single_hymn_fragment, container, false);

      txt_number = (TextView) v.findViewById(R.id.singleHymn_number);
      title = (TextView) v.findViewById(R.id.hymn_title);
      title.setTypeface(HymnsApplication.fontTitolo1);

      return v;
   }

   void showHymn(final Inno inno) {
      //Adding selected hymn to recents list.
      HymnsApplication.getRecentsManager().pushHymn(inno);
      txt_number.setText(String.valueOf(inno.getNumero()) + ".");
      title.setText(inno.getTitolo());

      //Treating star check box
      final CheckBox chk_starred = (CheckBox) getView().findViewById(R.id.singleHymn_starcheck);
      chk_starred.setChecked(inno.isStarred());
      chk_starred.setOnClickListener(new CheckBox.OnClickListener() {
         @Override
         public void onClick(View v) {
            inno.setStarred(chk_starred.isChecked());
         }
      });

      setListAdapter(new StrofeAdapter(getActivity(), inno.getListStrofe()));
   }

}