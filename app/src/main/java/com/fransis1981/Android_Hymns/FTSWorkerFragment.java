package com.fransis1981.Android_Hymns;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * Created by Fransis on 21/08/2014.
 * This is a UI-less fragment for running asynchronously the FTS builder algorithm.
 */
public class FTSWorkerFragment extends Fragment {

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    static interface TaskCallbacks {
        void onPreExecute();
        void onProgressUpdate(int val);
        void onCancelled();
        void onPostExecute();
    }

    private TaskCallbacks mCallbacks;
    private FTSTask mTask;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (TaskCallbacks) activity;
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        // Currently commented because: https://code.google.com/p/android/issues/detail?id=22564
        //setRetainInstance(true);

        // Create and execute the background task.
        mTask = new FTSTask();
        mTask.execute(HymnBooksHelper.me());
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        mTask.cancel(true);
        super.onDetach();
        mCallbacks = null;
    }

    /**
     * Note that we need to check if the callbacks are null in each
     * method in case they are invoked after the Activity's and
     * Fragment's onDestroy() method have been called.
     */
    private class FTSTask extends AsyncTask<HymnBooksHelper, Integer, Void> {

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected Void doInBackground(HymnBooksHelper... prm) {
            publishProgress(prm[0].FTS_Building_CurrentProgressValue);
            int _increment = (HymnBooksHelper.PROGRESSBAR_MAX_VALUE + HymnBooksHelper.FTS_BUILDING_STOPPED) / prm[0].getTotalNumberOfHymns();


            //TODO: Open the FTS builder cursor if it not yet.

            if (prm[0].FTS_Building_CurrentProgressValue > HymnBooksHelper.FTS_BUILDING_STOPPED) {
                //TODO: FTS build previously started; should fix cursor and progress value to one step back.
            }

            for (int i = prm[0].FTS_Building_CurrentProgressValue; !isCancelled() && i < 10000; i+=50) {
                SystemClock.sleep(30);
                prm[0].FTS_Building_CurrentProgressValue = i;
                publishProgress(i);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... val) {
            if (mCallbacks != null) {
                mCallbacks.onProgressUpdate(val[0]);
            }
        }

        @Override
        protected void onCancelled() {
            try {
                if (mCallbacks != null) {
                    mCallbacks.onCancelled();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "FTS worker:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPostExecute(Void ignore) {
            if (mCallbacks != null) {
                mCallbacks.onPostExecute();
            }
        }

    }       //END private class FTSTask

}
