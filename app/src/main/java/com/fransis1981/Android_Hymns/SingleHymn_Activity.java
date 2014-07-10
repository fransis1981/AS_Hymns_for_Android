package com.fransis1981.Android_Hymns;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

/**
 * Created by Fransis on 12/04/14 21.19.
 */
public class SingleHymn_Activity extends FragmentActivity {
   static Intent single_hymn_intent;

   public static final String NUMERO_INNO_BUNDLEARG = "NumeroInno";
   public static final String INNARIO_BUNDLEARG = "InnarioScelto";

   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.singlehymn_activity);

      Bundle extras = getIntent().getExtras();
      int hymnNumber = extras.getInt(NUMERO_INNO_BUNDLEARG);
      String innarioTitle = extras.getString(INNARIO_BUNDLEARG);
      final Inno hymnToDisplay =  HymnsApplication.getInnarioByTitle(innarioTitle).getInno(hymnNumber);

      SingleHymn_Fragment frag = (SingleHymn_Fragment) getSupportFragmentManager().findFragmentById(R.id.singlehymn_fragment);
      frag.showHymn(hymnToDisplay);
   }

   static void setupIntent() {
      if (single_hymn_intent == null) single_hymn_intent = new Intent("com.fransis1981.action.SINGLEHYMNSHOW");
   }

   static void startIntentWithHymn(Context context, Inno hymn) {
      Bundle newextra = new Bundle();
      newextra.putInt(SingleHymn_Activity.NUMERO_INNO_BUNDLEARG, hymn.getNumero());
      newextra.putString(SingleHymn_Activity.INNARIO_BUNDLEARG, hymn.getParentInnario().getTitolo());
      SingleHymn_Activity.single_hymn_intent.replaceExtras(newextra);
      context.startActivity(SingleHymn_Activity.single_hymn_intent);
   }

}