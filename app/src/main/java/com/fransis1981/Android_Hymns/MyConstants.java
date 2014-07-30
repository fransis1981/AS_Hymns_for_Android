package com.fransis1981.Android_Hymns;

/**
 * Created by francesco.vitullo on 27/01/14.
 */
public final class MyConstants {
   public static final String LogTag_STR = "HYMNS";
   public static final String DB_NAME = "DB_Inni.s3db";
   public static final int DB_VERSION = 5;            //Increment this number when a new DB file is going to be shipped.

   //Database tables and fields naming and indexing
   public static final String TABLE_INNARI = "Innari";
   public static final String FIELD_INNARI_ID = "ID_Innario";
   public static final String FIELD_INNARI_TITOLO = "Titolo";
   public static final String FIELD_INNARI_NUM_INNI = "Numero_Inni";
   public static final int INDEX_INNARI_ID = 0;
   public static final int INDEX_INNARI_TITOLO = 1;
   public static final int INDEX_INNARI_NUM_INNI = 2;

   public static final String TABLE_INNI = "Inni";
   public static final String FIELD_INNI_ID = "ID_Inno";
   public static final String FIELD_INNI_ID_INNARIO = "ID_Innario";
   public static final String FIELD_INNI_NUMERO = "Numero";
   public static final String FIELD_INNI_TITOLO = "Titolo";
   public static final String FIELD_INNI_NUM_STROFE = "Numero_Strofe";
   public static final String FIELD_INNI_CATEGORIA = "Categoria";
   public static final int INDEX_INNI_ID = 0;
   public static final int INDEX_INNI_ID_INNARIO = 1;
   public static final int INDEX_INNI_NUMERO = 2;
   public static final int INDEX_INNI_TITOLO = 3;
   public static final int INDEX_INNI_NUM_STROFE = 4;
   public static final int INDEX_INNI_CATEGORIA = 5;

   public static final String TABLE_STROFE = "Strofe";
   public static final String FIELD_STROFE_ID_INNO = "ID_Inno";
   public static final String FIELD_STROFE_ID_NUM_STROFA = "Num_Strofa";
   public static final String FIELD_STROFE_TESTO = "Testo";
   public static final String FIELD_STROFE_ISCHORUS = "IS_Chorus";
   public static final int INDEX_STROFE_ID_INNO = 0;
   public static final int INDEX_STROFE_ID_NUM_STROFA = 1;
   public static final int INDEX_STROFE_TESTO = 2;
   public static final int INDEX_STROFE_ISCHORUS = 3;

   //Titoli dei tab nella pagina principale
   public static final String TAB_MAIN_KEYPAD = HymnsApplication.myResources.getString(R.string.tab_keypad);
   public static final String TAB_MAIN_HYMNSLIST = HymnsApplication.myResources.getString(R.string.tab_list);
   public static final String TAB_MAIN_RECENT = HymnsApplication.myResources.getString(R.string.tab_recents);
   public static final String TAB_MAIN_STARRED = HymnsApplication.myResources.getString(R.string.tab_starred);

   //Queries
   public static final String QUERY_SELECT_INNARI = "SELECT * FROM Innari";

}
