package com.fransis1981.Android_Hymns;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class FTSIndexerSvc extends Service {
    public static final String FTS_INDEX_PROGRESSED = "FTS_INDEX_PROGRESSED";
    public static final String FTS_PROGRESS_EXTRA_VAL = "percent";
    public static final int FTS_PROGRESS_COMPLETE = 12345;

    private FTSTask mTask;
    private HymnBooksHelper mHH;       //Used to keep a reference and possibly avoid GC
    
    //This variable is used to decide wether to broadcast a progress update or not.
    private int mCurrentProgress;

    private String mCancelReason = "";


    public FTSIndexerSvc() {
        mCurrentProgress = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // Create and execute the background task.
        mTask = new FTSTask();
        mHH = HymnBooksHelper.me();
        mTask.execute();

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
            mHH = null;
        }
        catch (Exception e) {
            Log.w(MyConstants.LogTag_STR, "Terminating indexer service with exception: " + e.getMessage());
        }
        stopSelf();
    }

    //-------------------------------------------------------
    // Fields to support FTSTask progress.
    //-------------------------------------------------------
    public static final int FTS_BUILDING_STOPPED = -1;
    static Cursor mFTSCursor;
    static int mFTS_CurrentProgressValue = FTS_BUILDING_STOPPED;


    /*
     * Returns true if fields state shows that FTS table is still under building process.
     */
    public static boolean isBuildingFTS() {
        return  (mFTS_CurrentProgressValue > FTS_BUILDING_STOPPED)
                && (mFTSCursor != null);
    }


    class FTSTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... ignore) {
            //Wait for the DB to be available
            while (mHH == null || mHH.mDB == null || !mHH.mDB.isOpen()) {
                SystemClock.sleep(1);
            }

            int _increment = HymnBooksHelper.PROGRESSBAR_MAX_VALUE / mHH.getTotalNumberOfHymns();

            //Checking previous execution and progress status
            if (isBuildingFTS()) {
                //FTS build previously started; should fix cursor and progress value to one step back,
                //remembering to delete for safety current pointed hymn's ID.
                if (mFTSCursor.getPosition() > -1) {
                    int _tempid = mFTSCursor.getInt(MyConstants.INDEX_INNI_ID);
                    mHH.deleteHymnFromFTS_byID(_tempid);
                    mFTSCursor.moveToPrevious();
                }

                publishProgress(mFTS_CurrentProgressValue =
                        ((mFTSCursor.getPosition()+1)*_increment));
            }
            else {
                if (!mHH.createFTSTable()) {
                    mCancelReason = HymnsApplication.myResources.getString(R.string.fts_worker_cancel_reason1);
                    this.cancel(true);
                }
                mFTSCursor = mHH.mDB.query(
                        MyConstants.TABLE_INNI,
                        null,
                        null,
                        null,
                        null,
                        null,
                        MyConstants.FIELD_INNI_NUMERO);
                mFTS_CurrentProgressValue = 0;
            }

            ContentValues _cv = new ContentValues();
            String txt = "";
            int _successful_inserts = 0;
            while (mFTSCursor.moveToNext() && !isCancelled()) {
                _cv.clear();
                Inno _inno = new Inno(mFTSCursor, null);
                _cv.put(MyConstants.FIELD_INNARI_ID, mFTSCursor.getInt(MyConstants.INDEX_INNI_ID_INNARIO));
                _cv.put(MyConstants.FTS_FIELD_INNI_ID, mFTSCursor.getInt(MyConstants.INDEX_INNI_ID));
                _cv.put(MyConstants.FIELD_INNI_NUMERO, mFTSCursor.getInt(MyConstants.INDEX_INNI_NUMERO));
                _cv.put(MyConstants.FIELD_INNI_TITOLO, _inno.getTitolo());
                txt = HymnBooksHelper.SearchTextUtils.normalizeAndLower(_inno.getFullText(false, false));
                txt = HymnBooksHelper.SearchTextUtils.stripPunctuation(txt).trim();
                _cv.put(MyConstants.FIELD_STROFE_TESTO, txt);
                if (MyConstants.DEBUG && _successful_inserts <= 10) Log.i(MyConstants.LogTag_STR, "FTS: " + txt);
                if (mHH.mDB.insert(MyConstants.FTS_TABLE, null, _cv) == -1) {
                    Log.e(MyConstants.LogTag_STR, "Error while FTS indexing: " + _inno.getTitolo());
                }
                else {
                    _successful_inserts++;
                }

                publishProgress(mFTS_CurrentProgressValue += _increment);
            }
            //break everything and do not change any status-relevant fields if this task was cancelled.
            if (MyConstants.DEBUG)
                Log.i(MyConstants.LogTag_STR, String.format("Correctly inserted %d rows into FTS table.", _successful_inserts));
            if (isCancelled()) return null;

            mFTSCursor.close();
            mFTSCursor = null;
            mFTS_CurrentProgressValue = FTS_BUILDING_STOPPED;
            mHH.setFTSAvailable(true);
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
