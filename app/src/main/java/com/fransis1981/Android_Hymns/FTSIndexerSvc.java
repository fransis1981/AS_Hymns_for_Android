package com.fransis1981.Android_Hymns;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class FTSIndexerSvc extends Service {
    public static final String FTS_INDEX_PROGRESSED = "FTS_INDEX_PROGRESSED";
    public static final String FTS_PROGRESS_EXTRA_VAL = "percent";
    public static final int FTS_PROGRESS_COMPLETE = 12345;

    private FTSTask mTask;
    
    //This variable is used to decide wether to broadcast a progress update or not.
    private int mCurrentProgress;

    private String mCancelReason = "";


    public FTSIndexerSvc() {
        mCurrentProgress = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //if ((flags & START_FLAG_RETRY) == 1) {
        //    //TODO - NOTE: intent is null if service is restarted after run-time termination
        //}

        // Create and execute the background task.
        mTask = new FTSTask();
        mTask.execute(HymnBooksHelper.me());

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /*
     * This method is used to broadcast progress only if a 1% progress has abeen actually achieved.
     * Parameter prm_progress is a value in the range [0 .. 9999]
     */
    private void broadcastProgress(int prm_progress) {
        int _percent = Math.round(((float)prm_progress) / 9999 * 100);
        if (_percent > mCurrentProgress || prm_progress == FTS_PROGRESS_COMPLETE) {
            Intent _i = new Intent(FTS_INDEX_PROGRESSED);
            mCurrentProgress = _percent;
            _i.putExtra(FTS_PROGRESS_EXTRA_VAL, prm_progress);
            sendOrderedBroadcast(_i, null);
        }
    }       //END void broadcastProgress()

    /*
     * Call this method when FTS building is complete to terminate the service and broadcast the
     * termination value (so that main activity may hide progress bat and show the search menu item).
     */
    private void terminate(boolean prm_finished) {
        if (prm_finished) broadcastProgress(FTS_PROGRESS_COMPLETE);
        try {
            mTask.cancel(true);
            mTask = null;
        }
        catch (Exception e) {
            Log.w(MyConstants.LogTag_STR, e.getMessage());
        }
        stopSelf();
    }

    class FTSTask extends AsyncTask<HymnBooksHelper, Integer, Void> {

        @Override
        protected void onPreExecute() {
        }

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
            if (MyConstants.DEBUG) Log.i(MyConstants.LogTag_STR, "Completed FTS table generation.");
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... val) {
            broadcastProgress(val[0]);
        }

        /*
        @Override
        protected void onCancelled() {
            try {
                }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        */

        @Override
        protected void onPostExecute(Void ignore) {
           terminate(true);
        }

    }       //END private class FTSTask


}
