package com.fransis1981.Android_Hymns;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by Fransis on 02/03/14 21.30.
 */
public class Inni2RowsCursorAdapter extends CursorAdapter {
   private LayoutInflater li;

   private static class ViewHolder {
      TextView numero_inno;
      TextView titolo_inno;
      TextView titolo_innario;
      CheckBox chk_starred;
   }

   public Inni2RowsCursorAdapter(Context context, Cursor c, int flags) {
      super(context, c, flags);
      li = LayoutInflater.from(context);
   }

   @Override
   public void bindView(View view, Context context, Cursor cursor) {
      ViewHolder vh = (ViewHolder) view.getTag();
      int num = cursor.getInt(MyConstants.INDEX_INNI_NUMERO);
      vh.numero_inno.setText(String.valueOf(num) + ".");
      vh.titolo_inno.setText(cursor.getString(MyConstants.INDEX_INNI_TITOLO));
      Innario innario = HymnBooksHelper.me().getInnarioByID(cursor.getString(MyConstants.INDEX_INNI_ID_INNARIO));
      vh.titolo_innario.setText(innario.toString());
      vh.chk_starred.setChecked(innario.getInno(num).isStarred());
   }

   @Override
   public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
      ViewHolder vh;
      View innoTemplate;

      vh = new ViewHolder();
      innoTemplate = li.inflate(R.layout.hymnslist_tworows_item, null, true);
      vh.numero_inno = (TextView) innoTemplate.findViewById(R.id.item_Hymn_number);
      vh.titolo_inno = (TextView) innoTemplate.findViewById(R.id.item_hymn_title);
      vh.titolo_inno.setTypeface(HymnsApplication.fontTitolo1);
      vh.titolo_innario = (TextView) innoTemplate.findViewById(R.id.item_hymnbook_title);
      vh.chk_starred = (CheckBox) innoTemplate.findViewById(R.id.item_Hymn_starcheck);
      innoTemplate.setTag(vh);
      return innoTemplate;
   }

}
