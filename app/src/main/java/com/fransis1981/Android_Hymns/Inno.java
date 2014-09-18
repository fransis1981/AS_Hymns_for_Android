package com.fransis1981.Android_Hymns;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Fransis on 23/02/14 21.29.
 */
public class Inno {
   public static enum Categoria {
      NESSUNA(0),
      BAMBINI(2),
      FUNERALE(4),
      MATRIMONIO(8),
      SANTACENA(16);

      private final int val;
      private Categoria(int v) {
         val = v;
      }

      @Override
      public String toString() {
         switch (val) {
            case 16: return "SANTA CENA";
            default: return super.toString();
         }
      }

      //Method for casting from integer to Categoria
      public static Categoria parseInt(int _x) {
         switch (_x) {
            case 2: return BAMBINI;
            case 4: return FUNERALE;
            case 8: return MATRIMONIO;
            case 16: return SANTACENA;
            default: return NESSUNA;
         }
      }
      public static ArrayList<String> getCategoriesStringList() {
         ArrayList<String> ret = new ArrayList<String>();
         for (Categoria val: values())
            ret.add(val.toString());
         return ret;
      }

      //Cannot simply use valueOf because of the SANTA CENA enum which cointains a white space
      public static Categoria parseString(String str) {
         if (str.equals(SANTACENA.toString())) return SANTACENA;
         return valueOf(str);
      }
   }

   public static class InnoComparator implements Comparator<Inno> {
      @Override
      public int compare(Inno lhs, Inno rhs) {
         return ((Integer) lhs.numero).compareTo(rhs.numero);
      }
   }

   private Innario parentInnario;
   private int numero;
   private int id_inno;
   private String titolo;
   private Integer numStrofe;              //Numero di strofe non definite come cori
   private int numCori;                    //Numero di strofe definite come cori
   private Categoria categoria;
   private ArrayList<Strofa> strofe_cori;      //Lista ordinata delle strofe e dei cori

   boolean mStarred;

    public String getTitolo() {
      return titolo;
   }

    public int getId() { return id_inno; }

    public int getNumero() { return numero; }

    /* ATTENZIONE: questo oggetto può essere null se l'Inno è usato solo a fini di elaborazione intermedia. */
   public Innario getParentInnario() { return parentInnario; }

   public Categoria getCategoria() { return categoria; }

   public int getNumTotaleStrofe() { return numStrofe + numCori; }


   public Inno() {
      numStrofe = 0 ;
      numCori = 0;
      strofe_cori = null;
   }

   public Inno(Cursor cursor, Innario _parent) {
      numStrofe = 0 ;
      numCori = 0;
      strofe_cori = null;

      parentInnario = _parent;
      id_inno = cursor.getInt(MyConstants.INDEX_INNI_ID);
      numero = cursor.getInt(MyConstants.INDEX_INNI_NUMERO);
      titolo = cursor.getString(MyConstants.INDEX_INNI_TITOLO);
      categoria = Categoria.parseInt(cursor.getInt(MyConstants.INDEX_INNI_CATEGORIA));
   }


    /*
     * This method checks if the requested hymn is already available at the application level, otherwise
     * it builds a new object with NULL parent and returns it.
     */
    public static Inno findInnoById(long prm_id_inno) throws InnoNotFoundException {
        Cursor c = HymnBooksHelper.me().mDB.query(MyConstants.TABLE_INNI, null,
                MyConstants.FIELD_INNI_ID + "=?", new String[]{String.valueOf(prm_id_inno)},
                null, null, null);
        if (c.getCount() == 0) throw new InnoNotFoundException();
        c.moveToNext();
        Innario _innario = HymnsApplication.getInnarioByID(c.getString(MyConstants.INDEX_INNI_ID_INNARIO));
        if (_innario != null) {
            return _innario.getInno(c.getInt(MyConstants.INDEX_INNI_NUMERO));
        }
        else {
            return new Inno(c, null);
        }
    }

   public ArrayList<Strofa> getListStrofe() {
      //Load strofe on demand by means of a cursor
      loadStrofeFromDB();
      return strofe_cori;
   }

   public Inno setStarred(boolean starred) {
      if (mStarred != starred) {
         if (starred) {
            HymnsApplication.getStarManager().addStarred(this);
         }
         else {
            HymnsApplication.getStarManager().removeStarred(this);
         }
         mStarred = starred;
      }
      return this;                //Implementing chaining
   }

   public boolean isStarred() { return mStarred; }

    /*
     * This method returns a single string containing the hymn full text.
     * New line characters are used at breaklines.
     * The parameter prm_withTitle controls wether the returned string contains the hymn title in the first line.
     */
    public String getFullText(boolean prm_withTitle, boolean prm_withLabels) {
        String _s = "";
        ArrayList<Strofa> _al = getListStrofe();

        if (prm_withTitle) _s = getTitolo() + "\n";
        for (Strofa strofa: _al)
            _s = _s +
                    ((prm_withLabels)? strofa.getLabel() + ". " : "") +
                    strofa.getContenuto() + "\n";
        return _s;
    }

   void loadStrofeFromDB() {
      if (strofe_cori == null) {
         strofe_cori = new ArrayList<Strofa>();
         Cursor c = HymnBooksHelper.me().mDB.query(MyConstants.TABLE_STROFE, null,
                               MyConstants.FIELD_STROFE_ID_INNO + "=?", new String[]{String.valueOf(id_inno)},
                               null, null, MyConstants.FIELD_STROFE_ID_NUM_STROFA);
         Strofa ss;
         while (c.moveToNext()) {
            ss = new Strofa(c, numStrofe, this);
            if (ss.IsChorus()) numCori++;
            else numStrofe++;
            strofe_cori.add(ss);
         }
         c.close();
      }
   }

}
