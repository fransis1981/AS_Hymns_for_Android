package com.fransis1981.Android_Hymns;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by francesco.vitullo on 27/01/14.
 */
public class HymnsApplication extends Application {
   private static HymnsApplication singleton;
   static final String HelperPreferences_STR = "HelperPreferences";

   //Providing at application level a one-time instantiation of the Resources table (for efficiency).
   public static Resources myResources;
   public static AssetManager assets;
   public static Typeface fontTitolo1;
   public static Typeface fontLabelStrofa;
   public static Typeface fontContenutoStrofa;

    //Two-chars identifier of the selected language. This is used for selecting relevant hymnbooks.
    public static String mCurrentLanguageLocale;

   public static ArrayList<Innario> innari;
   public static HashMap<Inno.Categoria, Innario> categoricalInnari;    //One separate Innario for each category.
   private static Innario currentInnario;
   //////private static Cursor currentInnario;

   //Definizione evento per gestire il cambiamento di innario corrente
   public interface OnCurrentInnarioChangedListener {
      public void onCurrentInnarioChanged();
   }
   private static OnCurrentInnarioChangedListener mOnCurrentInnarioChangedListener;
   public static void setOnCurrentInnarioChangedListener(OnCurrentInnarioChangedListener listener) {
      mOnCurrentInnarioChangedListener = listener;
   }


   private static MRUManager recentsManager;
   private static StarManager starManager;

   //static TimingLogger tl;

   public static HymnsApplication getInstance() {
        return singleton;
    }


    @Override
    public void onCreate() {
       super.onCreate();
       //tl = new TimingLogger(MyConstants.LogTag_STR, "HymnsApplication.onCreate");

       singleton = this;
       assets = getAssets();
       myResources = getResources();
       fontTitolo1 = Typeface.createFromAsset(assets , "Caudex_Italic.ttf");
       fontLabelStrofa = Typeface.createFromAsset(assets, "WetinCaroWant.ttf");
       fontContenutoStrofa = Typeface.createFromAsset(assets, "Caudex_Italic.ttf");
       //tl.addSplit("Prepared resources and fonts.");

       //Si prepara l'intent per il single hymn (to avoid null pointer exceptions at first invocation)
       SingleHymn_Activity.setupIntent();
       //tl.addSplit("Prepared intent.");

        //Managing preferences; if device language locale belongs to one of those available then automatically
        //select it, otherwise load xml defaults. This is the logic only one no user preferences are stored.
        SharedPreferences _prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!(_prefs.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false))) {
            //This branch gets executed only if preferences have been never set before (or user wiped app data)
            PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.prefs, false);
            String available_locales = TextUtils.join(";",myResources.getStringArray(R.array.pref_international_values));
            String default_language = Locale.getDefault().getLanguage();
            if (available_locales.contains(default_language)) {
                Log.i(MyConstants.LogTag_STR, "Device locale is included in the app: " + default_language);
                SharedPreferences.Editor e = _prefs.edit();
                e.putString(PrefsActivity.PREF_LANGUAGE_SELECTED, default_language).commit();
                mCurrentLanguageLocale = default_language;
            }
        }
        mCurrentLanguageLocale = _prefs.getString(
                PrefsActivity.PREF_LANGUAGE_SELECTED,
                myResources.getString(R.string.pref_international_defaultvalue));

       //Time logging continued within the helper class...
       HymnBooksHelper hymnBooksHelper = new HymnBooksHelper(getApplicationContext());
       HymnBooksHelper.me().caricaInnari(true);
       innari = HymnBooksHelper.me().innari;
       categoricalInnari = HymnBooksHelper.me().categoricalInnari;

       //Si imposta l'innario corrente al primo innario disponibile
       setCurrentInnario(innari.get(0));

       //Si crea il gestore dai cantici recenti
       recentsManager = new MRUManager();
       try {
          //Restoring saved preferences (recents)
          recentsManager.readFromPreferences(getApplicationContext());
       }
       catch (Exception e) {
          Log.e(MyConstants.LogTag_STR, "CATCHED SOMETHING WHILE RESTORING RECENT HYMNS...." + e.getMessage());
       }
       //tl.addSplit("Prepared recents manager with preferences.");

       //Si crea il gestore dei preferiti (starred)
       starManager = new StarManager();
       try {
          //Restoring saved preferences (starred)
          starManager.readFromPreferences(getApplicationContext());
       }
       catch (Exception e) {
          Log.e(MyConstants.LogTag_STR, "CATCHED SOMETHING WHILE RESTORING STARRED HYMNS...." + e.getMessage());
       }
       //tl.addSplit("Prepared star manager with preferences.");
       //tl.dumpToLog();

        //Log.i(MyConstants.LogTag_STR, HymnBooksHelper.normalizeAndLower("A Ti, Deus Trino, poderoso Deus,|br/|Que estás presente sempre junto aos teus|br/|A ministrar as bênçãos lá dos céus,"));
        //Log.i(MyConstants.LogTag_STR, HymnBooksHelper.normalizeAndLower("Santo Deus, vem inflamar|br/|Nossos débeis corações;|br/|Vem as trevas dissipar,|br/|Livra-nos de imperfeições."));
        //Log.i(MyConstants.LogTag_STR, HymnBooksHelper.normalizeAndLower("Queres a glória divina alcançar?|br/|Dá teu coração a Jesus."));
        Log.i(MyConstants.LogTag_STR, Locale.getDefault().getCountry() + " - "
                + Locale.getDefault().getDisplayCountry() + " - "
                + Locale.getDefault().getLanguage() + " - "         //This is something I can properly use.
                + Locale.getDefault().getISO3Language());

    }       //END onCreate()


    public static void setCurrentInnario(Innario _innario) {
      if (currentInnario == _innario) return;
      currentInnario = _innario;
      if (mOnCurrentInnarioChangedListener != null)
         mOnCurrentInnarioChangedListener.onCurrentInnarioChanged();
    }


    public static void setCurrentInnario(String _titolo) {
      setCurrentInnario(getInnarioByTitle(_titolo));
    }


    public static void setCurrentInnario(Inno.Categoria _categoria) {
      if (_categoria == Inno.Categoria.NESSUNA) setCurrentInnario(innari.get(0));
      else setCurrentInnario(categoricalInnari.get(_categoria));
    }


    public static Innario getCurrentInnario() {
      return currentInnario;
    }


    /*
    * Questo metodo restituisce l'oggetto Innario opportuno conoscendone il titolo.
    * Ritorna NULL se nessun innario corrisponde al criterio specificato.
    */
    public static Innario getInnarioByTitle(String _title) {
      for (Innario i: innari) {
         if (i.getTitolo().equals(_title)) return i;
      }
      return null;
    }


    /*
     * Questo metodo restituisce l'oggetto Innario opportuno conoscendone l'ID nel database.
     * Ritorna NULL se nessun innario corrisponde al criterio specificato.
     */
    public static Innario getInnarioByID(String prm_id) {
        for (Innario i: innari) {
            if (i.getId().equals(prm_id)) return i;
        }
        return null;
    }


    /*
     * This is a convenience method to get an ArrayList of titles for use with spinner's adapter.
     */
    public static ArrayList<String> getInnariTitles() {
      ArrayList<String> ret = new ArrayList<String>();
      for (Innario i: innari)
         ret.add(i.toString());
      return ret;
   }


    public static StarManager getStarManager() { return starManager; }


    public static MRUManager getRecentsManager() { return recentsManager; }


    //Helper class to clusterize clipboard related methods.
    public static class ClipboardHelper {

        /*
         * This method copies a string text content into clipboard.
         */
        static void CopyText(Context c, String prm_label, String prm_text) {
            if (Build.VERSION.SDK_INT < 11) {
                android.text.ClipboardManager _cm = (android.text.ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
                if (_cm != null) _cm.setText(prm_text);
            } else {
                android.content.ClipboardManager _cm = (android.content.ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
                if (_cm != null) {
                    ClipData _clip = ClipData.newPlainText(prm_label, prm_text);
                    _cm.setPrimaryClip(_clip);
                }
            }
        }

        /*
         * This method copies a strophe's text content into clipboard.
         */
        static void CopyStrophe(Context c, Strofa s) {
            CopyText(c,
                    s.getParent().getTitolo() + " [" + s.getLabel() + "]",
                    s.getContenuto());
        }

        /*
         * This method copies a hymn's full text content (including its title) into clipboard.
         */
        static void CopyHymn(Context c, Inno prm_hymn) {
            CopyText(c, prm_hymn.getTitolo(), prm_hymn.getFullText(true, true));
        }
    }       //END public static class ClipboardHelper

}
