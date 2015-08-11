/* 
 * Created by Cory Teng  email:tengrenfu@163.com
 * Aug 8, 2015 
 */  
package com.displayjson.asyncload;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;  
import android.widget.ProgressBar;
import android.widget.TextView;

public class EnhancedListView extends ListView implements OnScrollListener {
	private Context mContext;
    private int mMaxYOverScrollDistance;
    private int mFirstVisibleItemPosition;
    private final int PULL_DOWN_UPDATE = 0;  
    private final int RELEASE_UPDATE = 1; 
    private final int UPDATING = 2; 
    private int mCurrentState = PULL_DOWN_UPDATE;
    private int mDownY;  
    private Animation mUpAnimation;
    private Animation mDownAnimation;
    private ImageView mImageViewArrow;  
    private ProgressBar mProgressBar; 
    private TextView mTextViewState;  
    private boolean mIsScrollToBottom;  
    private int mHeaderViewHeight;  
    private View mHeaderView; 
    private View mFooterView;  
    private int mFooterViewHeight;  
    private boolean mIsLoadingMore;
    private boolean mIsAllLoaded;
    private OnUpdateListener mOnUpdateListener;  
    
    
	public EnhancedListView(Context context) {
		super(context);
		mContext = context;
		initEnhanvedListView();
		initHeaderView();
		initFooterView();
		setOnScrollListener(this);
	}
	
	public EnhancedListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initEnhanvedListView();
		initHeaderView();
		initFooterView();
		setOnScrollListener(this);
	}

	public EnhancedListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initEnhanvedListView();
		initHeaderView();
		initFooterView();
		setOnScrollListener(this);
	}

	/**
	 * calculate mMaxYOverScrollDistance
	 */
	private void initEnhanvedListView() {
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		mMaxYOverScrollDistance = (int)(metrics.density * Constants.MAX_Y_OVERSCROLL_DISTANCE);
	}

	/**
	 * initialize the header view and add on the list view
	 */
    private void initHeaderView() {  
        mHeaderView = View.inflate(getContext(), R.layout.header_view, null);
        mImageViewArrow = (ImageView)mHeaderView.findViewById(R.id.listview_header_arrow);
        mProgressBar = (ProgressBar)mHeaderView.findViewById(R.id.listview_header_progressbar);
        mTextViewState = (TextView)mHeaderView.findViewById(R.id.listview_header_state);  
        mHeaderView.measure(0, 0);
        mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
        addHeaderView(mHeaderView);
        initAnimation();
    }
    
    /**
     * initialize the footer view and add on the list view
     */ 
    private void initFooterView() {
        mFooterView = View.inflate(getContext(), R.layout.footer_view, null);
        mFooterView.measure(0, 0);
        mFooterViewHeight = mFooterView.getMeasuredHeight();
        mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);  
        addFooterView(mFooterView);
    }

    /**
     * animate the icon on the header view
     */
    private void initAnimation() {  
        mUpAnimation = new RotateAnimation(0f, -180f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);  
        mUpAnimation.setDuration(500);
        mUpAnimation.setFillAfter(true);

        mDownAnimation = new RotateAnimation(-180f, -360f,  
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);  
        mDownAnimation.setDuration(500);
        mDownAnimation.setFillAfter(true);  
    }

    /**
     * handle the header view with the three process
     * 'pull down to refresh', 'Release to update', 'updating' 
     */
    @Override  
    public boolean onTouchEvent(MotionEvent event) {    	
        switch (event.getAction()) {        
            case MotionEvent.ACTION_DOWN:  
                mDownY = (int) event.getY();
                break;
                
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) event.getY();
                int diff = (moveY - mDownY) / 2;
                int paddingTop = diff - mHeaderViewHeight;
                if (mFirstVisibleItemPosition == 0
                        && -mHeaderViewHeight < paddingTop) {
                    if (paddingTop > 0 && mCurrentState == PULL_DOWN_UPDATE) {
                        mCurrentState = RELEASE_UPDATE;
                        updateHeaderView();
                    } else if (paddingTop < 0
                            && mCurrentState == RELEASE_UPDATE) {
                        mCurrentState = PULL_DOWN_UPDATE;
                        updateHeaderView();
                    }
                    mHeaderView.setPadding(0, paddingTop, 0, 0);
                    return true;
                }
                break;
                
            case MotionEvent.ACTION_UP:
                if (mCurrentState == RELEASE_UPDATE) {
                    mHeaderView.setPadding(0, 0, 0, 0);
                    mCurrentState = UPDATING;
                    updateHeaderView();
                    if (mOnUpdateListener != null) {
                    	mOnUpdateListener.onPullToUpdate();
                    }
                } else if (mCurrentState == PULL_DOWN_UPDATE) {
                    mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
                }
                break;
                
            default:
                break;
        }
        return super.onTouchEvent(event);
    }  
  
    private void updateHeaderView() {  
        switch (mCurrentState) {
            case PULL_DOWN_UPDATE:  
                mTextViewState.setText("Pull to refresh...");
                mImageViewArrow.startAnimation(mDownAnimation);
                break;
                
            case RELEASE_UPDATE:
                mTextViewState.setText("Release to update...");
                mImageViewArrow.startAnimation(mUpAnimation);
                break;
                
            case UPDATING:
                mImageViewArrow.clearAnimation();
                mImageViewArrow.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mTextViewState.setText("Updating...");
                break;
                
            default:
                break;
        }
    }
    
    /**
     * set json data is loaded or not
     * @param over  json data is loaded over or not
     */
    public void setAllLoaded(boolean over) {
    	mIsAllLoaded = over;
    }
    
    /**
     * scroll to the bottom of the list view or not
     */
	@Override  
	public void onScroll(AbsListView view, int firstVisibleItem, 
			                      int visibleItemCount, int totalItemCount) {
        mFirstVisibleItemPosition = firstVisibleItem;
        if (getLastVisiblePosition() == totalItemCount - 1) {
            mIsScrollToBottom = true;  
        } else {
            mIsScrollToBottom = false;
        }
	}  

	/**
	 * if the scroll bar is at the bottom and isn't on loading
	 * and json data is not loaded, display the footer view
	 */
	@Override  
	public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_FLING) {
            if (mIsScrollToBottom && !mIsLoadingMore && !mIsAllLoaded) {
                mIsLoadingMore = true;
                mFooterView.setPadding(0, 0, 0, 0);
                setSelection(getCount());
                if (mOnUpdateListener != null) {
                    mOnUpdateListener.onLoadingMore();
                }
            }
        }
	}

    public void setOnRefreshListener(OnUpdateListener listener) {  
        mOnUpdateListener = listener;  
    }  
  
    /**
     * hide the header view when new request are sent
     * it's called after 'onDownPullRefresh' 
     */
    public void hideHeaderView() {  
        mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
        mImageViewArrow.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mTextViewState.setText("Pull to refresh...");
        mCurrentState = PULL_DOWN_UPDATE;
    }  
  
    /**
     * hide the footer view when the new contents have been loaded
     * it's called in 'onLoadingMore'
     */
    public void hideFooterView() {
        mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);
        mIsLoadingMore = false;
    }  

    /**
     * override this method to implement the scroll bar
     * with damping effect, using mMaxYOverScrollDistance
     * to replace maxOverScrollY
     */
	@Override  
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, 
    		int scrollY, int scrollRangeX, int scrollRangeY, 
    		int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, 
				scrollRangeX, scrollRangeY, maxOverScrollX, 
				mMaxYOverScrollDistance, isTouchEvent);
	}
	
}
