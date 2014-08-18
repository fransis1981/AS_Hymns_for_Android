package com.fransis1981.Android_Hymns;

import android.database.Cursor;

/**
 * Created by Fransis on 24/02/14 0.42.
 */
public class Strofa {
   private Inno parentInno;
   private boolean isChorus;
   private String contenuto;
   private short indiceStrofa;      //Numero sequenziale che non distingue strofe normali dai cori
   private String label;            //Etichetta associata:
                                    // <numero progressivo> per le strofe normali
                                    // CORO per le strofe flaggate come cori


   public boolean IsChorus() { return isChorus; }
   public String getLabel() { return label; }
   public String getContenuto() { return contenuto; }
   public Inno getParent() { return parentInno; }

   /*
    * This constructior reads content from cursor passed by argument (which already points to the relevant strofa).
    * First time constructor is invoked for a given hymn, lastNumericLabel is set to 0;
    * if this is not a chorus, value is increased by 1 and its string representation is assigned to label.
    */
   public Strofa(Cursor cursor, Integer _lastNumericLabel, Inno _parent) {
       parentInno = _parent;
       isChorus = cursor.getInt(MyConstants.INDEX_STROFE_ISCHORUS) != 0;
       indiceStrofa = cursor.getShort(MyConstants.INDEX_STROFE_ID_NUM_STROFA);
       contenuto = cursor.getString(MyConstants.INDEX_STROFE_TESTO).replaceAll("<br>", "\n");
       label = isChorus?HymnsApplication.myResources.getString(R.string.coro_label):(++_lastNumericLabel).toString();
   }

}
