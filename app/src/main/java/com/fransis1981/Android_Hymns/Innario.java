package com.fransis1981.Android_Hymns;

import android.database.Cursor;
import android.util.SparseArray;

import java.util.ArrayList;

/**
 * Created by Fransis on 23/02/14 20.07.
 */
public class Innario {
   private String titolo;
   private String id;
   private int numeroInni;
   private SparseArray<Inno> inni;
   private DialerList mDialerList;
   private Cursor hymnsCursor;

   private void init(int _numeroinni, String _titolo, String _id) {
      mDialerList = new DialerList();
      numeroInni = _numeroinni;
      titolo = _titolo;
      id = _id;
      inni = new SparseArray<Inno>(_numeroinni);

      //Populating dialer list only (not hymns objects)
      //Log.i(MyConstants.LogTag_STR, "Stato del DB nwl costruttore dell'Innario:" + HymnBooksHelper.me().db.isOpen());
      hymnsCursor = HymnBooksHelper.me().mDB.query(MyConstants.TABLE_INNI,
                           null,
                           MyConstants.FIELD_INNI_ID_INNARIO + "=?", new String[] {String.valueOf(id)},
                           null, null, MyConstants.FIELD_INNI_NUMERO);
      while (hymnsCursor.moveToNext()) {
         Inno iii = new Inno(hymnsCursor, this);
         int num = iii.getNumero();
         inni.append(num, iii);
         mDialerList.addAvailableNumber(num);

         //Se l'inno appartiene ad una categoria, lo si sistema nell'opportuna struttura dati
         if (iii.getCategoria() != Inno.Categoria.NESSUNA) {
            HymnBooksHelper.me().addCategoricalInno(iii);
         }
      }

      hymnsCursor.moveToFirst();
   }

   //This constructor is used if you want to extract fields from the cursor from an external class.
   public Innario(int numeroinni, String titolo, String id) {
      init(numeroinni, titolo, id);
   }

   //This constructor prepares an empty innario (populate using addInno() method).
   public Innario(String titolo) {
      init(0, titolo, titolo);
   }

   /*
    * This chainable method allows to add an already existing inno object to a given innario.
    * That inno (being already created) has the parent pointer correctly populated with its actual container.
    * This method is useful for building category innario preserving proper inno/innario relationship.
    */
   public Innario addInno(Inno _inno) {
      inni.append(_inno.getNumero(), _inno);
      mDialerList.addAvailableNumber(_inno.getNumero());
      numeroInni++;
      return this;
   }

   public String getTitolo() {
      return titolo;
   }
   public Innario setTitolo(String _titolo) {
      titolo = _titolo;
      return this;
   }

   public String getId() { return id; }

   public int getNumeroInni() {
      return numeroInni;
   }

   public Inno getInno(int number) {
      return inni.get(number);
   }

   public DialerList getDialerList() {
      return mDialerList;
   }

   public Cursor getHymnsCursor() {
      return hymnsCursor;
   }

   public boolean hasHymn(int number) {
      return inni.indexOfKey(number) >= 0;
   }

   //toString returns the title
   @Override
   public String toString() {
      return getTitolo();
   }

   public ArrayList<Inno> hymnsToArrayList() {
      int ss = inni.size();
      ArrayList<Inno> ret = new ArrayList<Inno>(ss);
      for (int i = 0; i < ss; i++)
         ret.add(inni.valueAt(i));
      return ret;
   }

   public void dispose() {
      hymnsCursor.close();
   }
}
