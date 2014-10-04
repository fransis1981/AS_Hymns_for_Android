package com.fransis1981.Android_Hymns;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.text.Normalizer;
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

    public static final int PROGRESSBAR_MAX_VALUE = 9999;

    /*
     *     https://code.google.com/p/android/issues/detail?id=22564
     *     Costanti e variabili per supportare la conservazione dello stato del processo di background per la
     *     costruzione della tabella FTS. Questo è un work-around ad un bug della support-library di Google.
     */

    private int mTotalNumberOfHymns = 0;    //Actual value is calculated upon initialization
    /* ---------------------------------------------------------------------- */

   HymnBooksHelper(Context context) {
       super(context, MyConstants.DB_NAME, null, null, MyConstants.DB_VERSION);
       setForcedUpgrade();       //PLEASE NOTE: without these I was not able to open DB in write-mode to allow upgrade.
       mContext = context;
       singleton = this;
       getReadableDatabase();

       //Calculating the total number of hymns over all hymnbooks.
       Cursor c = mDB.rawQuery(MyConstants.QUERY_SELECT_INNARI, null);
       while (c.moveToNext()) {
           mTotalNumberOfHymns += c.getInt(MyConstants.INDEX_INNARI_NUM_INNI);
       }
       c.close();

       //Checking FTS availability and consistency.
       mFTSAvailable = isTableExisting(MyConstants.FTS_TABLE);
       if (mFTSAvailable) {
           //Just to be paranoid...
           c = mDB.query(MyConstants.FTS_TABLE, null, null, null, null, null, null);
           if (c.getCount() != mTotalNumberOfHymns)
               setFTSAvailable(false);
           c.close();
       }

       initDataStructures();
   }


    public static HymnBooksHelper me() { return singleton; }


    private void initDataStructures() {
        //Si prepara la struttura per gli innari di categoria
        categoricalInnari = new HashMap<Inno.Categoria, Innario>();
        for (Inno.Categoria cat: Inno.Categoria.values())
           categoricalInnari.put(cat, new Innario(cat.toString()));

        //Si prepara l'array di base degli innari
        innari = new ArrayList<Innario>();
    }


   /*
    * This methods populates the innari field with the hymnbooks matching the specified language.
    */
   void caricaInnari(String prm_language) {
      try {
          innari.clear();
          for (Inno.Categoria cat: Inno.Categoria.values())
              categoricalInnari.get(cat).clear();

          Cursor c = mDB.rawQuery(MyConstants.QUERY_SELECT_INNARI, null);
          while (c.moveToNext()) {
              if (c.getString(MyConstants.INDEX_INNARI_LANG_CODE).equalsIgnoreCase(prm_language))
                  innari.add(new Innario(c));
          }
          c.close();

      } catch (Exception e) {
         Log.e(MyConstants.LogTag_STR, String.format("CATCHED SOMETHING WHILE LOADING HYMNS [%s]...[%s]", prm_language, e.getMessage()));
         e.printStackTrace();
      }
   }


    /*
     * Access to a private field.
     */
    public int getTotalNumberOfHymns() {
        return mTotalNumberOfHymns;
    }


   public void addCategoricalInno(Inno _inno) {
      categoricalInnari.get(_inno.getCategoria()).addInno(_inno);
   }


    /*
     * Questo metodo restituisce l'oggetto Innario opportuno conoscendone l'ID nel database.
     * Ritorna NULL se nessun innario corrisponde al criterio specificato.
     */
    public Innario getInnarioByID(String _id) {
        for (Innario i: innari) {
           if (i.getId().equals(_id)) return i;
        }
        return null;
    }


    /*
    * Questo metodo restituisce l'oggetto Innario opportuno conoscendone il titolo.
    * Ritorna NULL se nessun innario corrisponde al criterio specificato.
    */
    public Innario getInnarioByTitle(String _title) {
        for (Innario i: innari) {
            if (i.getTitolo().equals(_title)) return i;
        }
        return null;
    }


    /*
     * If you pass false as parameter, the FTS gets actually dropped; if you pass true, the FTS
     * table must actually exist, otherwise an exception is thrown.
     * In addition to super implementation, FTS table is checked against the proper number of hymns.
     * Infact, if an external object calls this method it is expected that it also takes care of
     * properly creating the FTS table.
     */
    @Override
    public void setFTSAvailable(boolean _av) throws IllegalStateException {
        super.setFTSAvailable(_av);
        if (_av && !checkFTSTableConsistency()) {
            mFTSAvailable = false;
            throw new IllegalStateException("FTS table does not contain the correct number of hymns.");
        }
        mFTSAvailable = _av;
    }


    /*
     * check that the FTS table contains the right number of rows
     * (it could not because of some evil race condition or app termination).
     * This method returns true if the FTS table appears to have the correct number of hymns.
    */
    private boolean checkFTSTableConsistency() {
        Cursor _c = mDB.query(MyConstants.FTS_TABLE, null, null, null, null, null, null);
        return _c.getCount() == getTotalNumberOfHymns();
    }


    public int deleteHymnFromFTS_byID(int _id) {
        return
                mDB.delete(MyConstants.FTS_TABLE, MyConstants.FIELD_INNI_ID_INNARIO + "=?", new String[] {String.valueOf(_id)});
    }


    //Search query format: [SELECT * FROM FTS_TABLE WHERE FTS_TABLE MATCH 'word1* ... wordN*' LIMIT #]
    /*
     * Starting from a user-entered query text (passed in the prm_query parameter), this method:
     *  - Normalizes special characters
     *  - Add wildcards in between words
     *  - Builds a list of IDs for narrowing search only on the currently loaded hymnbooks (they depend on the language)
     *  - NOT DONE -> Builds actual query using the NEAR/2 FTS keyword [Wildcards should be enough]
     *  - Runs the query, limiting the number of results to the value passed in the prm_limit parameter
     *    (if this parameter is greater than 0)
     *
     *    Returned cursor already points to the first useful result. If there are no results, NULL is returned.
     */
    Cursor doFullTextSearch(String prm_query, int prm_limit) {
        if (TextUtils.isEmpty(prm_query))
            throw new IllegalArgumentException("Invoked doFullTextSearch() with an empty query string.");
        String search_terms =
                SearchTextUtils.addFinalWildcard(
                SearchTextUtils.stripPunctuation(
                SearchTextUtils.normalizeAndLower(prm_query)));
        String _sql_qry = MyConstants.QUERY_FTS_SEARCH_SELECTION_ON_HYMNBOOKS
                .replace(MyConstants.PLACEHOLDER_keywords, search_terms);
        _sql_qry = _sql_qry.replace(MyConstants.PLACEHOLDER_ids, TextUtils.join(",", getLoadedHymnbooksIds()));
        _sql_qry = _sql_qry + ((prm_limit == 0)? "" : String.format(" LIMIT %d", prm_limit));
        if (MyConstants.DEBUG) Log.i(MyConstants.LogTag_STR, _sql_qry);
        Cursor _c = mDB.rawQuery(_sql_qry, null);
        if (_c != null && !_c.moveToFirst()) {
            _c.close();
            _c = null;
        }

        return _c;
    }


    /*
     * This method returns an array of strings, and each string represents one hymnbook ID among those
     * currently loaded.
     */
    public String[] getLoadedHymnbooksIds() {
        String[] ttt = new String[innari.size()];
        for (int i = 0; i < innari.size(); i++) {
            ttt[i] = innari.get(i).getId();
        }
        return ttt;
    }


    //---------------------------------------------------------------------------------------
    // Container class for text utility methods to support search functionality.
    //---------------------------------------------------------------------------------------
    public static class SearchTextUtils {
        //Regular expression to match two or more consecutive white spaces.
        public static final String REGEX_DoubleSpace = "\\s(\\s)+";
        //Regular expression to match punctuation: . , ; : - ! ? ' ( ) "
        public static final String REGEX_Punctuations = "(\\.|,|;|:|-|!|\\?|'|\\(|\\)|\")";

        /*
         * For each word in the query parameter, this method adds a wildcard.
         */
        public static String addWildcards(String query) {
            if (TextUtils.isEmpty(query)) return query;

            final StringBuilder builder = new StringBuilder();
            final String[] splits = TextUtils.split(query, " ");
            for (String split : splits)
                builder.append(split).append("* ");

            return builder.toString().trim();
        }


        /*
         * This method adds a single wildcard at the end of the query parameter.
         */
        public static String addFinalWildcard(String query) {
            if (TextUtils.isEmpty(query)) return query;
            return query + "*";
        }


        /*
         * Helper method for preparing text either for FTS indexing and for building search query.
         */
        public static String normalizeAndLower(String str) {
            String normalized;
            if (Build.VERSION.SDK_INT < 9) {
                normalized = SupportNormalizer.unaccentify(str);
            } else {
                normalized = Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
            }
            return normalized.toLowerCase();
        }


        /*
         * Helper method for preparing text either for FTS indexing and for building search query.
         */
        public static String stripPunctuation(String str) {
            String cleaned = str;
            cleaned = cleaned.replaceAll(REGEX_Punctuations, " ");
            return cleaned.replaceAll(REGEX_DoubleSpace, " ");
        }


        /*
         * Given a full text (typically a strophe) and a string to match (typically a search query),
         * this method finds the match within the full text and extracts a substring with length prm_snippetLen.
         * The substring has at its center the match string; this rule does not apply if the match occurs either
         * at the very beginning or the very end of the fullText.
         * If no match occurs, the method returns the first prm_snippetLen chars from the full text.
         */
        public static String extractAndCenterSnippet(String prm_fullText, String prm_match, int prm_snippetLen) {
            String _sss;
            int _start = 0;
            try {
                //Case prm_snippetLen is large enough to contain the full text -> the full text is returned.
                if (prm_snippetLen >= prm_fullText.length()) return prm_fullText;

                int _idx = prm_fullText.indexOf(prm_match);

                //Case no match or match at the very beginning
                if (_idx <= 0) return prm_fullText.substring(0, prm_snippetLen - 1);

                //Calculate desired substring start in order to have the snippet centered;
                //if we get here, it means that full text is large enough to contain the snippet, so we cannot
                //overflow on both sides.
                _start = _idx + (prm_match.length() - prm_snippetLen) / 2;
                int _offset = _start;
                if (_offset < 0 || (_offset = _start + prm_snippetLen - prm_fullText.length()) > 0)     //EXTREME SHORT-CIRCUITING
                    _start -= _offset;

                _sss = prm_fullText.substring(_start, _start + prm_snippetLen - 1);
            }
            catch (StringIndexOutOfBoundsException obe) {
                Log.e(MyConstants.LogTag_STR,
                        String.format("EXTRACTING SNIPPET: Full length: %d - _start: %d", prm_fullText.length(), _start));
                Log.e(MyConstants.LogTag_STR, "EXTRACTING SNIPPET: " + prm_fullText);
                _sss = prm_fullText;
            }
            return _sss;
        }


        /*
         * This method surrounds the prm_match substring within prm_str (if present) with
         * HTML bold tags (<b> ... </b> for TextView visualization.
         */
        public static String addBoldTagsForMatch(String prm_str, String prm_match) {
            return prm_str.replace(prm_match, "<b>" + prm_match + "</b>");
        }


        /*
         * This method surrounds the prm_match substring within prm_str (if present) with
         * HTML text color tags (<font color="#RRGGBB"> ... </font> for TextView visualization.
         */
        public static String addColorTagsForMatch(String prm_str, String prm_match, int prm_color) {
            return prm_str.replace(prm_match,
                    String.format("<font color=\"#%06X\">",0xFFFFFF & prm_color)
                        + prm_match + "</font>");
        }

    }       //END class SearchTextUtils

}
