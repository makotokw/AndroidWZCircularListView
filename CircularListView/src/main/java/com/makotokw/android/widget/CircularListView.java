package com.makotokw.android.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class CircularListView extends ListView implements AbsListView.OnScrollListener {

    private static final int REPEAT_COUNT = 3;
    private int mItemHeight = 0;

    private CircularListViewListener mCircularListViewListener;

    private InfiniteListAdapter mInfiniteListAdapter;

    private boolean mEnableInfiniteScrolling = true;

    private CircularListViewContentAlignment mCircularListViewContentAlignment = CircularListViewContentAlignment.Left;

    private double mRadius = -1;

    public CircularListView(Context context) {
        this(context, null);
    }

    public CircularListView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.listViewStyle);
    }

    public CircularListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setOnScrollListener(this);
        setClipChildren(false);
        setEnableInfiniteScrolling(true);
    }

    public void setAdapter(ListAdapter adapter) {
        mInfiniteListAdapter = new InfiniteListAdapter(adapter);
        mInfiniteListAdapter.setEnableInfiniteScrolling(mEnableInfiniteScrolling);
        super.setAdapter(mInfiniteListAdapter);
    }

    public CircularListViewListener getCircularListViewListener() {
        return mCircularListViewListener;
    }

    public void setCircularListViewListener(CircularListViewListener circularListViewListener) {
        this.mCircularListViewListener = circularListViewListener;
    }

    public void setEnableInfiniteScrolling(boolean enableInfiniteScrolling) {
        mEnableInfiniteScrolling = enableInfiniteScrolling;
        if (mInfiniteListAdapter != null) {
            mInfiniteListAdapter.setEnableInfiniteScrolling(enableInfiniteScrolling);
        }
        if (mEnableInfiniteScrolling) {
            setHorizontalScrollBarEnabled(false);
            setVerticalScrollBarEnabled(false);
        }
    }

    public CircularListViewContentAlignment getCircularListViewContentAlignment() {
        return mCircularListViewContentAlignment;
    }

    public void setCircularListViewContentAlignment(
            CircularListViewContentAlignment circularListViewContentAlignment) {
        mCircularListViewContentAlignment = circularListViewContentAlignment;
        requestLayout();
    }

    public double getRadius() {
        return mRadius;
    }

    public void setRadius(double radius) {
        this.mRadius = radius;
    }

    public void scrollFirstItemToCenter() {
        if (!mEnableInfiniteScrolling) {
            return;
        }

        View firstVisibleView = getChildAt(0);
        if (firstVisibleView != null) {
            int realTotalItemCount = mInfiniteListAdapter.getRealCount();
            this.setSelectionFromTop(realTotalItemCount, getHeight() / 2 - firstVisibleView.getHeight() / 2);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {

        if (!mEnableInfiniteScrolling) {
            return;
        }

        View itemView = this.getChildAt(0);
        if (itemView == null) {
            return;
        }

        int realTotalItemCount = mInfiniteListAdapter.getRealCount();
        if (realTotalItemCount == 0) {
            return;
        }

        if (mItemHeight == 0) {
            mItemHeight = itemView.getHeight();
        }

        if (firstVisibleItem == 0) {
            // scroll one unit
            this.setSelectionFromTop(realTotalItemCount, itemView.getTop());
        }

        if (totalItemCount == firstVisibleItem + visibleItemCount) {
            // back one unit
            this.setSelectionFromTop(firstVisibleItem - realTotalItemCount,
                    itemView.getTop());
        }

        if (mCircularListViewContentAlignment != CircularListViewContentAlignment.None) {

            double viewHalfHeight = view.getHeight() / 2.0f;

            double vRadius = view.getHeight();
            double hRadius = view.getWidth();

            double yRadius = (view.getHeight() + mItemHeight) / 2.0f;
            double xRadius = (vRadius < hRadius) ? vRadius : hRadius;
            if (mRadius > 0) {
                xRadius = mRadius;
            }

            for (int i = 0; i < visibleItemCount; i++) {
                itemView = this.getChildAt(i);
                if (itemView != null) {
                    double y = Math.abs(viewHalfHeight - (itemView.getTop() + (itemView.getHeight() / 2.0f)));
                    y = Math.min(y, yRadius);
                    double angle = Math.asin(y / yRadius);
                    double x = xRadius * Math.cos(angle);

                    if (mCircularListViewContentAlignment == CircularListViewContentAlignment.Left) {
                        x -= xRadius;
                    } else {
                        x = xRadius / 2 - x;
                    }
                    itemView.scrollTo((int) x, 0);
                }
            }
        } else {
            for (int i = 0; i < visibleItemCount; i++) {
                itemView = this.getChildAt(i);
                if (itemView != null) {
                    itemView.scrollTo(0, 0);
                }
            }
        }

        if (mCircularListViewListener != null) {
            mCircularListViewListener.onCircularLayoutFinished(this, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public int getCentralPosition() {
        double vCenterPos = getHeight() / 2.0f;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getTop() <= vCenterPos && child.getTop() + child.getHeight() > vCenterPos) {
                return getFirstVisiblePosition() + i;
            }
        }
        return -1;
    }

    public View getCentralChild() {
        int pos = getCentralPosition();
        if (pos != -1) {
            return getChildAt(pos - getFirstVisiblePosition());
        }
        return null;
    }

    class InfiniteListAdapter implements ListAdapter {

        private boolean mEnableInfiniteScrolling = true;

        private ListAdapter mCoreAdapter;

        public InfiniteListAdapter(ListAdapter coreAdapter) {
            mCoreAdapter = coreAdapter;
        }

        private void setEnableInfiniteScrolling(boolean enableInfiniteScrolling) {
            mEnableInfiniteScrolling = enableInfiniteScrolling;
        }

        public int getRealCount() {
            return mCoreAdapter.getCount();
        }

        public int positionToIndex(int position) {
            int count = mCoreAdapter.getCount();
            return (count == 0) ? 0 : position % count;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            mCoreAdapter.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            mCoreAdapter.unregisterDataSetObserver(observer);
        }

        @Override
        public int getCount() {
            int count = mCoreAdapter.getCount();
            return (mEnableInfiniteScrolling) ? count * REPEAT_COUNT : count;
        }

        @Override
        public Object getItem(int position) {
            return mCoreAdapter.getItem(this.positionToIndex(position));
        }

        @Override
        public long getItemId(int position) {
            return mCoreAdapter.getItemId(this.positionToIndex(position));
        }

        @Override
        public boolean hasStableIds() {
            return mCoreAdapter.hasStableIds();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCoreAdapter.getView(this.positionToIndex(position), convertView, parent);
        }

        @Override
        public int getItemViewType(int position) {
            return mCoreAdapter.getItemViewType(this.positionToIndex(position));
        }

        @Override
        public int getViewTypeCount() {
            return mCoreAdapter.getViewTypeCount();
        }

        @Override
        public boolean isEmpty() {
            return mCoreAdapter.isEmpty();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return mCoreAdapter.areAllItemsEnabled();
        }

        @Override
        public boolean isEnabled(int position) {
            return mCoreAdapter.isEnabled(this.positionToIndex(position));
        }
    }
}
