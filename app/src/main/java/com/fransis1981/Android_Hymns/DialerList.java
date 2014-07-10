package com.fransis1981.Android_Hymns;

import android.util.Log;

/**
 * Created by Fransis on 06/03/14 17.07.
 * This recursive class is created is to support numbers obscuration on keypad.
 * To root instance is passed full original number (in either integer or string format);
 * to recurred instances it is passed the same number without the most significant digit
 * (infact users type numbers starting from most significant digit).
 * Each instance has an array of 10 elements of this same class.
 * NULL means that the number is not reached and could be oscured.
 */
public class DialerList {
   DialerList numberPresence[];
   DialerList mParent;           //This is the object itself for the root dialer, found in the Innario class definition.

   private void init(DialerList _parent) {
      //Upon construction, no digit is reachable (each array element is null).
      numberPresence = new DialerList[10];
      mParent = _parent;
   }

   public DialerList() {
      init(this);
   }
   public DialerList(DialerList _parent) {
      init(_parent);
   }

   public void addAvailableNumber(int _num) {
      try {
         addAvailableNumber(String.valueOf(_num));
      } catch (Exception e) {
         Log.e(MyConstants.LogTag_STR, "Il metodo DialerList.addAvailableNumber sul numero " +  _num + "ha generato: " + e.getMessage());
         e.printStackTrace();
      }
   }

   public void addAvailableNumber(String _numstr) throws Exception {
      if (_numstr.length() == 0) throw new Exception("Don't call addAvailableNumber(str) using an empty string.");
      int leftmostdigit = Integer.parseInt(_numstr.substring(0, 1));
      if (numberPresence[leftmostdigit] == null) numberPresence[leftmostdigit] = new DialerList(this);
      if (_numstr.length() == 1) return;     //Ci fermiamo, abbiamo finito le cifre da aggiungere.
      numberPresence[leftmostdigit].addAvailableNumber(_numstr.substring(1));
   }

   //This method actually returns the object itself if it's already the root.
   public DialerList getParentDialerList() {
      return mParent;
   }

   //Method to travel down the hierarchy of the DialerList; the relevant digit must be specified.
   public DialerList getSubDialerList(int digit) {
      return numberPresence[digit];
   }

   //Return an array of 10 booleans (true if that digit should be obscured at this level).
   public boolean[] getObscureList() {
      boolean[] ret = new boolean[10];
      for (int i = 0; i < 10; i++)
         ret[i] = (numberPresence[i] != null);
      return ret;
   }
}
