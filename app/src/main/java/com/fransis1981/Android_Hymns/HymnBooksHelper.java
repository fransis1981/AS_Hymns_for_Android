package com.fransis1981.Android_Hymns;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Fransis on 18/04/14 11.38.
 * Helper class introduced to gather in a single class methods for managing hymnbooks
 * and to possibly implement efficient persistence of them.
 * Current HymnBook XML version is stored in SharedPreferences and is checked to determine the
 * source for loading hymns (persistent storage or XML).
 */
public class HymnBooksHelper extends SQLiteAssetHelperWithFTS {
   private Context mContext;
   static HymnBooksHelper singleton;

   ArrayList<Innario> innari;
   HashMap<Inno.Categoria, Innario> categoricalInnari;    //One separate Innario for each category.
   SQLiteDatabase db;

    public static final int PROGRESSBAR_MAX_VALUE = 9999;

    /*
     *     https://code.google.com/p/android/issues/detail?id=22564
     *     Costanti e variabili per supportare la conservazione dello stato del processo di background per la
     *     costruzione della tabella FTS. Questo è un work-around ad un bug della support-library di Google.
     */
    public static final int FTS_BUILDING_STOPPED = -1;
    public int FTS_Building_CurrentProgressValue = FTS_BUILDING_STOPPED;
    public Cursor FTS_Building_Cursor;

    private int mTotalNumberOfHymns = 0;    //Actual value is calculated upon initialization
    /* ---------------------------------------------------------------------- */

   HymnBooksHelper(Context context) {
      super(context, MyConstants.DB_NAME, null, null, MyConstants.DB_VERSION);
      //setForcedUpgrade();
      mContext = context;
      singleton = this;

   }
   public static HymnBooksHelper me() { return singleton; }

   private void initDB() {
      if (db == null) db = getReadableDatabase();
   }

   private void initDataStructures() {
      initDB();
      //HymnsApplication.tl.addSplit("Preliminary operations upon DB.");

      //Si prepara la struttura per gli innari di categoria
      categoricalInnari = new HashMap<Inno.Categoria, Innario>();
      for (Inno.Categoria cat: Inno.Categoria.values())
         categoricalInnari.put(cat, new Innario(cat.toString()));

      //Qui si caricano gli innari veri e propri (da SD oppure file XML)
      innari = new ArrayList<Innario>();

      Cursor c = db.rawQuery(MyConstants.QUERY_SELECT_INNARI, null);
      while (c.moveToNext()) {
         innari.add(new Innario(c.getInt(MyConstants.INDEX_INNARI_NUM_INNI),
                                c.getString(MyConstants.INDEX_INNARI_TITOLO),
                                c.getString(MyConstants.INDEX_INNARI_ID)));
         //Log.i(MyConstants.LogTag_STR, "LETTO DAL DB: " + c.getString(MyConstants.INDEX_INNARI_TITOLO));

          mTotalNumberOfHymns += c.getInt(MyConstants.INDEX_INNARI_NUM_INNI);
      }
      c.close();
      //HymnsApplication.tl.addSplit("All hymnbooks acquired by means of the cursor (Dialer Lists and base Inno objects).");
   }

    public int getTotalNumberOfHymns() {
        return mTotalNumberOfHymns;
    }

   /*
    * Questo metodo popola l'array globale degli innari;
    * se è disponibile un file serializzato si carica da li, altrimenti si fa il parsing del file XML.
    * Se _forceXML è TRUE allora si obbliga l'algoritmo ad acquisire i dati da XML.
    */
   void caricaInnari(boolean _forceXML) {
      try {

         initDataStructures();

      } catch (Exception e) {
         Log.e(MyConstants.LogTag_STR, "CATCHED SOMETHING WHILE LOADING HYMNS...." + e.getMessage());
         e.printStackTrace();
      }
   }


   public void addCategoricalInno(Inno _inno) {
      categoricalInnari.get(_inno.getCategoria()).addInno(_inno);
   }

   /*
    * Questo metodo restituisce l'oggetto Innario opportuno conoscendone l'ID.
   */
   public Innario getInnarioByID(String _id) {
      for (Innario i: innari) {
         if (i.getId().equals(_id)) return i;
      }
      return null;
   }

}
