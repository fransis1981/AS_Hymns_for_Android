package com.fransis1981.Android_Hymns;

import android.provider.BaseColumns;

/**
 * Created by francesco.vitullo on 27/01/14.
 */
public final class MyConstants {
    public static final boolean DEBUG = true;      //Used to conditionally execute some statements during debug.

    //Titoli dei tab nella pagina principale
    public static final String TAB_MAIN_KEYPAD = HymnsApplication.myResources.getString(R.string.tab_keypad);
    public static final String TAB_MAIN_HYMNSLIST = HymnsApplication.myResources.getString(R.string.tab_list);
    public static final String TAB_MAIN_RECENT = HymnsApplication.myResources.getString(R.string.tab_recents);
    public static final String TAB_MAIN_STARRED = HymnsApplication.myResources.getString(R.string.tab_starred);

   //DB related constants
   public static final String LogTag_STR = "HYMNS_LOG";
   public static final String DB_NAME = "DB_Inni.s3db";
   public static final int DB_VERSION = 9;            //Increment this number when a new DB file is going to be shipped.

    //Database tables and fields naming and indexing
    public static final String TABLE_INNARI = "Innari";
    public static final String FIELD_INNARI_ID = "ID_Innario";
    public static final String FIELD_INNARI_TITOLO = "Titolo";
    public static final String FIELD_INNARI_LANG_CODE = "Language_Code";
    public static final String FIELD_INNARI_COUNTRY_CODE = "Country_Code";
    public static final int INDEX_INNARI_ID = 0;
    public static final int INDEX_INNARI_TITOLO = 1;
    public static final int INDEX_INNARI_NUM_INNI = 2;
    public static final int INDEX_INNARI_LANG_CODE = 3;
    public static final int INDEX_INNARI_COUNTRY_CODE = 4;

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

   public static final String FTS_TABLE = "hymns_FTS";

   /*
    * FTS TABLE SCHEMA
    *   - ID Innario
    *   - ID Inno
    *   - Numero Inno
    *   - Titolo Inno
    *   - Testo Inno (column "Testo" in the FTS table is the union of all verses for a given hymn)
    *   - Language code (e.g., 'it', 'pt', etc.)
    */
    public static final String FTS_FIELD_INNI_ID = BaseColumns._ID;
    public static final String[] FTS_COLUMN_NAMES = new String[] {
            FIELD_INNI_ID_INNARIO,
            FTS_FIELD_INNI_ID,
            FIELD_INNI_NUMERO,
            FIELD_INNI_TITOLO,
            FIELD_STROFE_TESTO
    };
    public static final String QUERY_CREATE_FTS_TABLE =
           "CREATE VIRTUAL TABLE " + FTS_TABLE + " USING fts3 (" +
           FIELD_INNI_ID_INNARIO + ", " +
           FTS_FIELD_INNI_ID + ", " +
           FIELD_INNI_NUMERO + ", " +
           FIELD_INNI_TITOLO + ", " +
           FIELD_STROFE_TESTO
                   + ")";


    // ---------------------------  Queries  ----------------------------------------------
    public static final String QUERY_SELECT_INNARI = "SELECT * FROM " + TABLE_INNARI;

    public static final String QUERY_DROP_FTS_TABLE = "DROP TABLE IF EXISTS " + FTS_TABLE;

    //Parameter 1: language code to filter hymnbooks.
    public static final String QUERY_SELECT_INNARI_WITH_LANG = "SELECT * FROM " + TABLE_INNARI
            + " WHERE " + FIELD_INNARI_LANG_CODE + "=?";


    //Template for a search query on the FTS table; append 'search keywords' to this string.
    //If needed, append also the LIMIT # statement at the end for limiting the number of results.
    public static final String QUERY_FTS_SEARCH = "SELECT * FROM " + FTS_TABLE +
            " WHERE " + FTS_TABLE + " MATCH ";


    //WHERE condition with ?s for searching FTS table with language condition
    //Placeholder <ids>: comma separated list of the desired hymnbook IDs for narrowing the search;
    //Placeholder <keywords>: phrase keywords
    public static final String PLACEHOLDER_ids = "<ids>";
    public static final String PLACEHOLDER_keywords = "<keywords>";
    public static final String QUERY_FTS_SEARCH_SELECTION_ON_HYMNBOOKS =
            "SELECT * FROM " + FTS_TABLE +
                    " WHERE " + FIELD_INNI_ID_INNARIO + " IN (" + PLACEHOLDER_ids + ") AND " +
                    FTS_TABLE + " MATCH \"" + PLACEHOLDER_keywords + "\"";

}
