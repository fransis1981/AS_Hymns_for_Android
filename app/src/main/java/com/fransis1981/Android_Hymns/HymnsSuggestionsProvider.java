package com.fransis1981.Android_Hymns;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/*
 * Supported Content URIs:
 * content://com.fransis1981.hymns.provider/suggestions
 *      This is used to provide search suggestions; it performs a lookup in the FTS table and returns a
 *      matrix cursor with proper suggestion columns as specified in the Android developers guide.
 *      The number of suggestions returned is limited.
 *
 * content://com.fransis1981.hymns.provider/hymns_from_fts
 *      This is used to provide search suggestions; it performs a lookup in the FTS table and returns a
 *      cursor with the same FTS table schema. There is no limitation in the number of results.
 */
public class HymnsSuggestionsProvider extends ContentProvider {
    private static final String strURIAuthority = "com.fransis1981.hymns.provider";
    private static final String URI_Suggestions_suffix = "search_suggest_query";
    private static final String URI_HymnsFTS_suffix = "hymns_from_fts";

    private static final int SUGGESTIONS = 0;
    private static final int HYMNSFTS = 1;
    public static final Uri PROVIDER_URI_SUGGESTIONS =
            Uri.parse("content://" + strURIAuthority + "/" + URI_Suggestions_suffix);
    public static final Uri PROVIDER_URI_HYMNSFTS =
            Uri.parse("content://" + strURIAuthority + "/" + URI_HymnsFTS_suffix);

    private static final UriMatcher mURI_MATCHER;
    static {
        mURI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        mURI_MATCHER.addURI(strURIAuthority, URI_Suggestions_suffix + "/*", SUGGESTIONS);
        mURI_MATCHER.addURI(strURIAuthority, URI_HymnsFTS_suffix + "/*", HYMNSFTS);
    }

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
        if (MyConstants.DEBUG) Log.i(MyConstants.LogTag_STR, "Provider queried with URI matching [" + mURI_MATCHER.match(uri) + "]:" + uri.toString() + "[" + uri.getLastPathSegment() + "]");
        Cursor _c;
        mHh = HymnBooksHelper.me();
        switch (mURI_MATCHER.match(uri)) {
            /* --------------------------- PROVIDER QUERIED FOR SUGGESTIONS ------------------------- */
            case SUGGESTIONS:
                MatrixCursor _cursor = new MatrixCursor(SUGGESTIONS_COLUMNS, mCapacity);
                String _query = uri.getLastPathSegment();
                if (_query.length() <= 2) return _cursor;

                //NOTE: doFullTextSearch returns the cursor already positioned on the first useful result
                //      or null if there are no results.
                _c = mHh.doFullTextSearch(_query, mCapacity);
                if (_c == null) return null;
                int i = 0;
                do {
                    _cursor.addRow(new Object[]{
                            i++,
                            _c.getString(_c.getColumnIndex(MyConstants.FIELD_INNI_TITOLO)),
                            HymnBooksHelper.SearchTextUtils.extractAndCenterSnippet(
                                    _c.getString(_c.getColumnIndex(MyConstants.FIELD_STROFE_TESTO)),
                                    _query,
                                    35),
                            _c.getInt(_c.getColumnIndex(MyConstants.FTS_FIELD_INNI_ID))
                    });
                } while ( _c.moveToNext());

                return _cursor;

            /* --------------------------- PROVIDER QUERIED FOR ACTUAL SEARCH ------------------------- */
            case HYMNSFTS:
                //NOTE: doFullTextSearch returns the cursor already positioned on the first useful result.
                _query = uri.getLastPathSegment();
                _c = mHh.doFullTextSearch(_query, 0);

                return _c;

            default: return null;
        }       //END switch

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
