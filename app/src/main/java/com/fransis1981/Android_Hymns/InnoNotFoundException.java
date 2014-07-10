package com.fransis1981.Android_Hymns;

/**
 * Created by Fransis on 10/03/14 15.29.
 */
public class InnoNotFoundException extends Exception {
   public InnoNotFoundException() {
      super();
   }

   public InnoNotFoundException(int number) {
      super("L'inno " + number + " non è stato trovato nell'innario specificato (probabilmente quello corrente).");
   }

   public InnoNotFoundException(int number, Innario innario) {
      super("L'inno " + number + " non è stato trovato nell'innario [" + innario.toString() + "].");
   }
}
