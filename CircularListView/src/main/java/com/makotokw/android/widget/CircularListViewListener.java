package com.makotokw.android.widget;

public interface CircularListViewListener {
    void onCircularLayoutFinished(CircularListView circularListView,
                                  int firstVisibleItem,
                                  int visibleItemCount,
                                  int totalItemCount);
}
