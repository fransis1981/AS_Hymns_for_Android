package com.fransis1981.Android_Hymns;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Fransis on 27/02/14 15.08.
 */
public class SingleHymn_Fragment extends ListFragment {

    private Inno mHymnDisplayed;

    private TextView txt_number, title;
    private ListView lw;
    private StrofeAdapter mAdapter;


   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      //return super.onCreateView(inflater, container, savedInstanceState);
      View v = inflater.inflate(R.layout.single_hymn_fragment, container, false);

      txt_number = (TextView) v.findViewById(R.id.singleHymn_number);
      title = (TextView) v.findViewById(R.id.hymn_title);
      title.setTypeface(HymnsApplication.fontTitolo1);

       lw = (ListView) v.findViewById(android.R.id.list);
       registerForContextMenu(lw);

      return v;
   }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v == lw) {
            MenuInflater _mi = getActivity().getMenuInflater();
            _mi.inflate(R.menu.singlehymn_contextmenu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        switch (item.getItemId()) {
            case R.id.mnu_hymn_copy_strophe:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                int idx = info.position;
                HymnsApplication.ClipboardHelper.CopyStrophe(getActivity(), mAdapter.getItem(idx));
                return true;

            case R.id.mnu_hymn_copy_full:
                HymnsApplication.ClipboardHelper.CopyHymn(getActivity(), mHymnDisplayed);
                return true;
        }
        return false;
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

       setListAdapter(mAdapter = new StrofeAdapter(getActivity(), inno.getListStrofe()));

       mHymnDisplayed = inno;
   }

}