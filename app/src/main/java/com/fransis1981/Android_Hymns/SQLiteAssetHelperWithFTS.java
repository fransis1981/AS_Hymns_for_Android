package com.fransis1981.Android_Hymns;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Fransis on 13/08/2014.
 */
public class SQLiteAssetHelperWithFTS extends SQLiteAssetHelper {

    SQLiteDatabase mDB;
    boolean mFTSAvailable;

    public SQLiteAssetHelperWithFTS(Context context, String name, String storageDirectory, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, storageDirectory, factory, version);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);

        //Erase FTS table if we're going under an upgrade. The getReadableDatabase() method
        //will manage its creation from scratch.
        try {
            if (newVersion != oldVersion)
                db.execSQL(MyConstants.QUERY_DROP_FTS_TABLE);
        }
        catch (Exception e) {
            Log.e(MyConstants.LogTag_STR, "onUpgrade(): error while dropping FTS table. Check DB R/W properties.");
        }
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        //Note that getReadableDatabase() returns a writeable db if possible (enough disk space...)
        mDB =  super.getReadableDatabase();

        //Store FTS availability status...
        //TODO...


        return mDB;
    }

    /*
     * Property method to check wether the FTS table is available or not.
     */
    public boolean isFTSAvailable() {
        return mFTSAvailable;
    }

    private boolean isTableExisting(String tableName) {
        boolean _wasClosed = mDB == null || !mDB.isOpen();
        if(_wasClosed) mDB = getReadableDatabase();

        String[] sss = new String[] {tableName};
        Cursor cursor = mDB.rawQuery(
                "SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = ?", sss
            );

        if(cursor != null) {
            if(cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }
}
