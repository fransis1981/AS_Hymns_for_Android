package com.fransis1981.Android_Hymns;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by Fransis on 17/09/2014.
 * This is the custom cursor adapter for showing search results with proper hymnbooks names and other features.
 */
public class SearchResultsCursorAdapter extends SimpleCursorAdapter {
    static class ViewHolder {
        TextView hymnbook;
        TextView num;
        TextView hymn;
        TextView snippet;
    }

    LayoutInflater mLi;
    Cursor mC;
    Context mContext;
    int mLayoutResource;
    String mSearchedQuery;
    static SparseArray<String> hashMapHymnbooks;       //To cache hymnbooks names.

    //The additional parameter searched_query stores keywords the user searched, so to apply some bold effect somewhere.
    public SearchResultsCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, String searched_query) {
        super(context, layout, c, from, to);
        mContext = context;
        mC = c;
        mLayoutResource = layout;
        mLi = LayoutInflater.from(context);
        mSearchedQuery = searched_query;
        if (hashMapHymnbooks == null) {
            hashMapHymnbooks = new SparseArray<String>(HymnBooksHelper.me().innari.size());
            for (Innario _i : HymnBooksHelper.me().innari) {
                hashMapHymnbooks.put(Integer.parseInt(_i.getId()), String.format("[%s]", _i.getTitolo()));
            }
        }
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View _v = mLi.inflate(mLayoutResource, null);
        ViewHolder _vh = new ViewHolder();
        _vh.hymnbook = (TextView) _v.findViewById(R.id.searchresults_hymnbook_title);
        _vh.num = (TextView) _v.findViewById(R.id.searchresults_hymn_number);
        _vh.hymn = (TextView) _v.findViewById(R.id.searchresults_hymn_title);
        _vh.snippet = (TextView) _v.findViewById(R.id.searchresults_snippet);
        _v.setTag(_vh);
        return _v;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        Inno _inno;
        int _id = -1;
        try {
            ViewHolder vh = (ViewHolder) view.getTag();
            _id = cursor.getInt(cursor.getColumnIndexOrThrow(MyConstants.FTS_FIELD_INNI_ID));
            _inno = Inno.findInnoById(_id);
            vh.hymnbook.setText(hashMapHymnbooks.get(cursor.getInt(cursor.getColumnIndex(MyConstants.FIELD_INNI_ID_INNARIO))));
            vh.num.setText(String.valueOf(_inno.getNumero()));
            vh.hymn.setText(_inno.getTitolo());
            vh.snippet.setText(Html.fromHtml(
                    HymnBooksHelper.SearchTextUtils.addColorTagsForMatch(
                            HymnBooksHelper.SearchTextUtils.addBoldTagsForMatch(
                                    //HymnBooksHelper.SearchTextUtils.extractAndCenterSnippet(
                                            cursor.getString(cursor.getColumnIndex(MyConstants.FIELD_STROFE_TESTO)),
                                            //mSearchedQuery, 45),
                                    mSearchedQuery
                            ),
                            mSearchedQuery, HymnsApplication.myResources.getColor(android.R.color.white)
                    )));
        }
        catch (InnoNotFoundException infe) {
            Log.e(MyConstants.LogTag_STR, String.format("Hymn with id %d was not found; we might add here more detailed lookup for finding it into the DB.", _id));
        }
        catch (IllegalArgumentException iae) {
            Log.e(MyConstants.LogTag_STR, "CRITICAL: Table column with hymn ID was not found in the search results.");
        }
        catch (Exception e) {
            Log.e(MyConstants.LogTag_STR, "Maybe you're scrolling TOO FAST and THERE ARE TOO MANY ITEMS in the list.");
        }
    }

}
