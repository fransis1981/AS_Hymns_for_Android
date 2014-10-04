package com.fransis1981.Android_Hymns;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Fransis on 14/03/14 11.56.
 * This class supports management of most recently used (sung) hymns according to a FIFO strategy.
 */
public class MRUManager {
   private static String RecentPreferences_STR = "Storage_Recents";
   private static String PREF_RecentsNumber = "Num";
   private static String PREF_HymnRef = "Ref";

   public interface MRUStateChangedListener {
      public void OnMRUStateChanged();
   }
   private MRUStateChangedListener mruStateChangedListener;

   ArrayList<Inno> fifo_arrlist;
   private int capacity;

   public MRUManager() {
      capacity = HymnsApplication.myResources.getInteger(R.integer.default_recents_capacity);
      fifo_arrlist = new ArrayList<Inno>();
   }

   public void setMruStateChangedListener(MRUStateChangedListener listener) {
      mruStateChangedListener = listener;
   }
   private void raiseMruStateChangedEvent() {
      if (mruStateChangedListener != null)
         mruStateChangedListener.OnMRUStateChanged();
   }

   /*
    * First find if the same hymn is already in list (pop it and push it back on top).
    * If hymn is not already in the list, than just push it checking capacity.
    */
   public void pushHymn(Inno inno) {
      if (fifo_arrlist.contains(inno))
         fifo_arrlist.remove(inno);        //Hymn already present; updating its fifo position.
      else if (fifo_arrlist.size() == capacity)
         fifo_arrlist.remove(fifo_arrlist.size() - 1);
      fifo_arrlist.add(0, inno);
      raiseMruStateChangedEvent();
      //Log.i(MyConstants.LogTag_STR, "Pushed hymn " + inno.getNumero() + " into recents list.");
   }

   //Drops all MRU content.
   public void clearMRU() {
      fifo_arrlist.clear();
      raiseMruStateChangedEvent();
   }

   //Convenience method for usage with List Adapters.
   public ArrayList<Inno> getMRUList() {
      return fifo_arrlist;
   }

   /*
    * Structure of recent preferences:
    * 1- Save the number or recent hymns
    * 2- For each recent hymn save a string with the following format:
    *             <ID_Innario>|<NumeroInno>
    *
    *  NOTE: the language code is appended to the string identifying the set of preferences.
    *        For backward compatibility, the "it" code is replaced with the empty string.
    */
   public void saveToPreferences(Context context, String prm_lang) {
       String locale_suffix = (prm_lang.equalsIgnoreCase("it"))? "" : prm_lang;
       SharedPreferences sp = context.getSharedPreferences(RecentPreferences_STR + locale_suffix, Context.MODE_PRIVATE);
       SharedPreferences.Editor e = sp.edit().clear();
       e.putInt(PREF_RecentsNumber, fifo_arrlist.size());
       if (MyConstants.DEBUG) Log.i(MyConstants.LogTag_STR, "Writing preferences to: " + RecentPreferences_STR + locale_suffix);
       int n = 1;
       for (Inno i: fifo_arrlist) {
           if (MyConstants.DEBUG) Log.i(MyConstants.LogTag_STR, "Recent stored: " +  i.getParentInnario().getId() + "|" + i.getNumero());
           e.putString(PREF_HymnRef + n++, i.getParentInnario().getId() + "|" + i.getNumero());
       }
       e.commit();
   }


   public void readFromPreferences(Context context, String prm_lang) throws InnoNotFoundException {
       String locale_suffix = (prm_lang.equalsIgnoreCase("it"))? "" : prm_lang;
       SharedPreferences sp = context.getSharedPreferences(RecentPreferences_STR + locale_suffix, Context.MODE_PRIVATE);
       fifo_arrlist.clear();
       int n = sp.getInt(PREF_RecentsNumber, 0);
       for (int i = 1; i <= n; i++) {
           String[] tokens = sp.getString(PREF_HymnRef + i, "").split("\\|");
           Innario innario = HymnBooksHelper.me().getInnarioByID(tokens[0]);
           if (innario == null) continue;            //Se l'innario non viene trovato si salta quest'inno
           int num = Integer.parseInt(tokens[1]);
           Inno inno = innario.getInno(num);
           if (inno == null) throw new InnoNotFoundException(num);
           fifo_arrlist.add(inno);
       }

       raiseMruStateChangedEvent();
   }
}
