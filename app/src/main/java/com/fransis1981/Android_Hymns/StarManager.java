package com.fransis1981.Android_Hymns;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Fransis on 10/03/14 15.12.
 * Class to keep track of starred hymns.
 */
public class StarManager {
   private static String StarredPreferences_STR = "Storage_Starred";
   private static String PREF_StarredNumber = "NumS";
   private static String PREF_HymnRef = "RefS";

   public interface StarredItemsChangedListener {
      public void OnStarredItemsChanged();
   }
   private StarredItemsChangedListener starredItemsChangedListener;

   private ArrayList<Inno> mStarredList;

   public StarManager() {
      mStarredList = new ArrayList<Inno>();
   }

   public void setStarredItemsChangedListener(StarredItemsChangedListener listener) {
      starredItemsChangedListener = listener;
   }
   private void raiseStarredItemsChangedEvent() {
      if (starredItemsChangedListener != null)
         starredItemsChangedListener.OnStarredItemsChanged();
   }

/*
   //Adding just a number, it is assuming that the number belong to current Innario.
   public void addStarred(int number) throws InnoNotFoundException {
      Innario iii = HymnsApplication.getCurrentInnario();
      if (!iii.hasHymn(number)) throw new InnoNotFoundException(number, iii);
      mStarredList.add(iii.getInno(number).setStarred(true));
   }

   public void addStarred(int number, Innario innario) throws InnoNotFoundException {
      if (!innario.hasHymn(number)) throw new InnoNotFoundException(number, innario);
      mStarredList.add(innario.getInno(number).setStarred(true));
   }
*/

   //Already passing in the pointer to the starred hymn.
   public void addStarred(Inno inno) {
       for (Inno i: mStarredList) {
           //Check if a hymn with the same ID has already been starred.
           if (i.getId() == inno.getId()) return;
       }
      mStarredList.add(inno);
      Collections.sort(mStarredList, new Inno.InnoComparator());
      raiseStarredItemsChangedEvent();
      //Log.i(MyConstants.LogTag_STR, "Added new starred hymn ... " + inno.getNumero());
   }

   public void removeStarred(Inno inno) {
      mStarredList.remove(inno);
      raiseStarredItemsChangedEvent();
      //Log.i(MyConstants.LogTag_STR, "!!Removed previously starred hymn ... " + inno.getNumero());
   }

   public ArrayList<Inno> getStarredList() { return mStarredList; }

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
       SharedPreferences sp = context.getSharedPreferences(StarredPreferences_STR + locale_suffix, Context.MODE_PRIVATE);
       SharedPreferences.Editor e = sp.edit().clear();
       e.putInt(PREF_StarredNumber, mStarredList.size());
       int n = 1;
       for (Inno i: mStarredList) {
           e.putString(PREF_HymnRef + n++, i.getParentInnario().getId() + "|" + i.getNumero());
       }
       e.commit();
   }


   public void readFromPreferences(Context context, String prm_lang) throws InnoNotFoundException {
       String locale_suffix = (prm_lang.equalsIgnoreCase("it"))? "" : prm_lang;
       SharedPreferences sp = context.getSharedPreferences(StarredPreferences_STR + locale_suffix, Context.MODE_PRIVATE);
       mStarredList.clear();
       int n = sp.getInt(PREF_StarredNumber, 0);
       for (int i = 1; i <= n; i++) {
           String[] tokens = sp.getString(PREF_HymnRef + i, "").split("\\|");
           Innario innario = HymnBooksHelper.me().getInnarioByID(tokens[0]);
           if (innario == null) continue;            //Se l'innario non viene trovato si salta quest'inno
           int num = Integer.parseInt(tokens[1]);
           Inno inno = innario.getInno(num);
           if (inno == null) throw new InnoNotFoundException(num);
           inno.mStarred = true;
           mStarredList.add(inno);
       }

       raiseStarredItemsChangedEvent();
   }
}
