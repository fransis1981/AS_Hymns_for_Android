package com.fransis1981.Android_Hymns;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class HymnsSuggestionsProvider extends ContentProvider {
    private static final String strURI = "content://com.fransis1981.hymns.provider/suggestions";
    public static final Uri PROVIDER_URI = Uri.parse(strURI);

    //The SUGGEST_COLUMN_INTENT_DATA contains the ID of the hymn related to the row
    public static final String[] SUGGESTIONS_COLUMNS = new String[] {BaseColumns._ID,
                                                                     SearchManager.SUGGEST_COLUMN_TEXT_1,
                                                                     SearchManager.SUGGEST_COLUMN_TEXT_2,
                                                                     SearchManager.SUGGEST_COLUMN_INTENT_DATA};

    private HymnBooksHelper mHh;
    private int mCapacity;

    public HymnsSuggestionsProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        mCapacity =  getContext().getResources().getInteger(R.integer.default_suggestions_capacity);
        return true;
    }

    /*
     * If query text is made up of 1 or 2 chars, then return at once with no suggestions.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor _cursor = new MatrixCursor(SUGGESTIONS_COLUMNS, mCapacity);
        String _query = uri.getLastPathSegment();
        if (_query.length() <= 2) return _cursor;

        //NOTE: doFullTextSearch returns the cursor already positioned on the first useful result.
        mHh = HymnBooksHelper.me();
        Cursor _c = mHh.doFullTextSearch(_query, mCapacity);
        int i = 0;
        do {
            _cursor.addRow(new Object[]{
                    i++,
                    _c.getString(_c.getColumnIndex(MyConstants.FIELD_INNI_TITOLO)),
                    //TODO: select a substring from the full text centered on the searched keywords.
                    _c.getString(_c.getColumnIndex(MyConstants.FIELD_STROFE_TESTO)).substring(1, 30),
                    _c.getInt(_c.getColumnIndex(MyConstants.FIELD_INNI_ID))
            });
        } while (_c.moveToNext());

        return _cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
