package com.meizu.widget.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.AbsListView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;

/**
 * 上拉加载
 * Created by maxueming on 2017/7/21.
 */

public class PullUpLoadMoreListView extends ListView implements AbsListView.OnScrollListener {

    private boolean isLoading = false;
    private View mFooterView;
    private int mFooterHeight;
    private OnLoadMoreListener mListener;
    private LayoutInflater inflater;

    public PullUpLoadMoreListView(Context context) {
        super(context);
        initView();
    }

    public PullUpLoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PullUpLoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public PullUpLoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView(){
        inflater = (LayoutInflater) SuperTestApplication.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initFooterView();
        setOnScrollListener(this);
    }


    /**
     * 初始化脚布局
     */
    private void initFooterView() {
        mFooterView = inflater.inflate(R.layout.refresh_load_more, null);
        mFooterView.measure(0, 0);
        mFooterHeight = mFooterView.getMeasuredHeight();
        mFooterView.setPadding(0, -mFooterHeight, 0, 0);
        this.addFooterView(mFooterView);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollstate) {
        if(this.getLastVisiblePosition() == this.getAdapter().getCount() - 1
                && !isLoading && (scrollstate == SCROLL_STATE_FLING
                || scrollstate == SCROLL_STATE_IDLE)){
            setLoadState(true);
            if(this.mListener != null){
                this.mListener.loadMore();
            }
        }
    }

    /**
     * 设置状态
     * @param b
     */
    public void setLoadState(boolean b) {
        this.isLoading = b;
        if(isLoading){ mFooterView.setPadding(0,0,0,0);
            this.setSelection(this.getAdapter().getCount() + 1);
        }else {
            mFooterView.setPadding(0,-mFooterHeight,0,0);
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {

    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener){
        this.mListener = listener;
    }

    public interface OnLoadMoreListener{
        void loadMore();
    }

}































