package com.fransis1981.Android_Hymns;

import android.app.Activity;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;

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
        void onCancelled(String reason);
        void onPostExecute();
    }

    private TaskCallbacks mCallbacks;
    FTSTask mTask;

    private String mCancelReason = "";

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
    class FTSTask extends AsyncTask<HymnBooksHelper, Integer, Void> {

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
            //Wait for the DB to be available
            while (prm[0] == null || prm[0].db == null || !prm[0].db.isOpen()) {
                SystemClock.sleep(2);
            }

            int _increment = HymnBooksHelper.PROGRESSBAR_MAX_VALUE / prm[0].getTotalNumberOfHymns();

            //Checking previous execution and progress status
            if (prm[0].isBuildingFTS()) {
                //FTS build previously started; should fix cursor and progress value to one step back,
                //remembering to delete for safety current pointed hymn's ID.
                if (prm[0].FTS_Building_Cursor.getPosition() > -1) {
                    int _tempid = prm[0].FTS_Building_Cursor.getInt(MyConstants.INDEX_INNI_ID);
                    prm[0].deleteHymnFromFTS_byID(_tempid);
                    prm[0].FTS_Building_Cursor.moveToPrevious();
                }

                publishProgress(prm[0].FTS_Building_CurrentProgressValue =
                        ((prm[0].FTS_Building_Cursor.getPosition()+1)*_increment));
            }
            else {
                if (!prm[0].createFTSTable()) {
                    mCancelReason = HymnsApplication.myResources.getString(R.string.fts_worker_cancel_reason1);
                    this.cancel(true);
                }
                prm[0].FTS_Building_Cursor = prm[0].db.query(MyConstants.TABLE_INNI, null, null, null, null, null, null);
                prm[0].FTS_Building_CurrentProgressValue = 0;
            }

            ContentValues _cv = new ContentValues();
            int _successful_inserts = 0;
            while (prm[0].FTS_Building_Cursor.moveToNext() && !isCancelled()) {
                _cv.clear();
                Inno _inno = new Inno(prm[0].FTS_Building_Cursor, null);
                _cv.put(MyConstants.FIELD_INNARI_ID, prm[0].FTS_Building_Cursor.getInt(MyConstants.INDEX_INNARI_ID));
                _cv.put(MyConstants.FIELD_INNI_ID, prm[0].FTS_Building_Cursor.getInt(MyConstants.INDEX_INNI_ID));
                _cv.put(MyConstants.FIELD_INNI_TITOLO, _inno.getTitolo());
                _cv.put(MyConstants.FIELD_STROFE_TESTO,
                            HymnBooksHelper.normalizeAndLower(_inno.getFullText(false)));
                if (prm[0].db.insert(MyConstants.FTS_TABLE, null, _cv) == -1) {
                    Log.e(MyConstants.LogTag_STR, "Error while FTS indexing: " + _inno.getTitolo());
                }
                else {
                    _successful_inserts++;
                }

                publishProgress(prm[0].FTS_Building_CurrentProgressValue += _increment);
            }
            //break everything and do not change any status-relevant fields if this task was cancelled.
            if (MyConstants.DEBUG)
                Log.i(MyConstants.LogTag_STR, String.format("Correctly inserted %d rows into FTS table.", _successful_inserts));
            if (isCancelled()) return null;

            prm[0].FTS_Building_Cursor.close();
            prm[0].FTS_Building_Cursor = null;
            prm[0].FTS_Building_CurrentProgressValue = HymnBooksHelper.FTS_BUILDING_STOPPED;
            prm[0].setFTSAvailable(true);
            if (MyConstants.DEBUG)
                Log.i(MyConstants.LogTag_STR, "Completed FTS table generation.");
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
                    mCallbacks.onCancelled(mCancelReason);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
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
