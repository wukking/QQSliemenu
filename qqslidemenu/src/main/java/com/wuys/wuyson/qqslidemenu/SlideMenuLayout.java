package com.wuys.wuyson.qqslidemenu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.FloatEvaluator;
import com.nineoldandroids.animation.IntEvaluator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Wuyson on 2016/12/1.
 */

public class SlideMenuLayout extends FrameLayout{
    private View menuView;
    private View mainView;
    private ViewDragHelper viewDragHelper;
    private int width;
    private float dragRange;
    private FloatEvaluator floatEvaluator;
    private IntEvaluator intEvaluator;

    public SlideMenuLayout(Context context) {
        super(context);
        init();
    }

    public SlideMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(getChildCount()!=2){
            throw new IllegalArgumentException("只能获取两个子VIew");
        }
        menuView = getChildAt(0);
        mainView = getChildAt(1);
    }
    //定义常量
    enum DragState{
        Open,Close
    }
    private DragState currentState = DragState.Close;//当前SlideMenu默认是关闭的

    private void init(){
        viewDragHelper = ViewDragHelper.create(this,callback);
        floatEvaluator = new FloatEvaluator();
        intEvaluator = new IntEvaluator();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * OnMeasure后，初始化自己和子View 的宽高
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getMeasuredWidth();
        dragRange = width*0.6f;
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == menuView||child == mainView;
        }
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        /**
         * 设置水平移动和边界等
         * @param child
         * @param left
         * @param dx
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if(child ==mainView){
                if(left<0) left=0;
                if (left>dragRange) left= (int) dragRange;
            }
            return left;
        }

        /**
         * 抬起手指后的动画时间
         * @param child
         * @return
         */
        @Override
        public int getViewVerticalDragRange(View child) {
            return (int) dragRange;
        }

        /**
         * 伴随动画设置
         * @param changedView
         * @param left
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (changedView ==menuView){
                menuView.layout(0,0,menuView.getMeasuredWidth(),menuView.getMeasuredHeight());
                int newLeft = mainView.getLeft()+dx;
                if(newLeft<0) left=0;
                if (newLeft>dragRange) newLeft= (int) dragRange;
                mainView.layout(newLeft,mainView.getTop()+dy,
                        newLeft+mainView.getMeasuredWidth(),mainView.getBottom()+dy);
            }
            //1.计算百分比
            float fraction = mainView.getLeft()/dragRange;
            //2.伴随动画2
            executeAnim(fraction);
            //3.更改状态，回调listener方法
            if(fraction==0 && currentState != DragState.Close) {
                //更改状态，回调关闭方法
                currentState = DragState.Close;
                if(listener!=null) listener.OnClose();
            }else if (fraction == 1f && currentState != DragState.Open){
                currentState=DragState.Open;
                if(listener!=null) listener.OnOpen();
            }
            //将drag的fraction暴露给外界
            if(listener!=null) {
                listener.OnDraging(fraction);
            }
        }




        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (mainView.getLeft()<dragRange/2){
                viewDragHelper.smoothSlideViewTo(mainView,0,mainView.getTop());
                ViewCompat.postInvalidateOnAnimation(SlideMenuLayout.this);
            }else {
                viewDragHelper.smoothSlideViewTo(mainView, (int) dragRange,mainView.getTop());
                ViewCompat.postInvalidateOnAnimation(SlideMenuLayout.this);
            }
        }

    };

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(viewDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(SlideMenuLayout.this);
        }
    }

    private void executeAnim(float fraction){
        //缩小mainView
        //float scaleValue = 0.8f+0.2f*(1-fraction);
        ViewHelper.setScaleX(mainView,floatEvaluator.evaluate(fraction,1f,0.8f));
        ViewHelper.setScaleY(mainView,floatEvaluator.evaluate(fraction,1f,0.8f));
        //移动menuView
        ViewHelper.setTranslationX(menuView,intEvaluator.evaluate(fraction,-(menuView.getMeasuredWidth()/2),0));
        //放大menuView
        ViewHelper.setScaleX(menuView,floatEvaluator.evaluate(fraction,0.5f,1f));
        ViewHelper.setScaleY(menuView,floatEvaluator.evaluate(fraction,0.5f,1f));
        //改变menuView的透明度
        ViewHelper.setAlpha(menuView,floatEvaluator.evaluate(fraction,0.3f,1f));

        getBackground().setColorFilter((Integer) ColorUtil.evaluateColor(fraction, Color.BLACK,Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    private OnDragStateChangeListener listener;
    public void setOnDragStateChangeListener(OnDragStateChangeListener listener){
        this.listener = listener;
    }

    public interface OnDragStateChangeListener{
        void OnOpen();
        void OnClose();
        void OnDraging(float fraction);
    }
}
