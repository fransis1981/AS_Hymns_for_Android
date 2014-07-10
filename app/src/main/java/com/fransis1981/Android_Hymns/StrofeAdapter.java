package com.fransis1981.Android_Hymns;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Fransis on 02/03/14 21.30.
 */
public class StrofeAdapter extends ArrayAdapter<Strofa> {
   private final Activity _context;
   private final ArrayList<Strofa> _strofe;

   public static class ViewHolder {
      TextView label_strofa;
      TextView content_strofa;
   }

   public StrofeAdapter(Activity context, ArrayList<Strofa> objects) {
      super(context, R.layout.single_hymn_fragment, objects);
      _context = context;
      _strofe = objects;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder vh;
      View strofaTemplate;

      strofaTemplate = convertView;
      if (convertView != null) {
         //Per questa particolare posizione nella ListView un oggetto holder è stato già allocato
         vh = (ViewHolder) convertView.getTag();
      }
      else {
         vh = new ViewHolder();
         LayoutInflater li = _context.getLayoutInflater();
         strofaTemplate = li.inflate(R.layout.strofa_item, null, true);
         vh.label_strofa = (TextView) strofaTemplate.findViewById(R.id.strofa_label);
         vh.content_strofa = (TextView) strofaTemplate.findViewById(R.id.strofa_content);
         vh.content_strofa.setTypeface(HymnsApplication.fontContenutoStrofa);
         strofaTemplate.setTag(vh);
      }

      //Populating texts and customizing font properties
      Strofa ss = _strofe.get(position);
      vh.label_strofa.setText(ss.getLabel());
      if (!ss.IsChorus()) {
         vh.label_strofa.setTypeface(HymnsApplication.fontLabelStrofa, Typeface.BOLD);
      }
      else {
         vh.label_strofa.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
      }
      vh.content_strofa.setText(ss.getContenuto());
      return strofaTemplate;
   }
}
