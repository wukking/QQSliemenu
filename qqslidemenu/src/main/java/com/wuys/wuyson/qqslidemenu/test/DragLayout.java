package com.wuys.wuyson.qqslidemenu.test;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.view.ViewHelper;
import com.wuys.wuyson.qqslidemenu.ColorUtil;

/**
 * Created by Wuyson on 2016/12/1.
 */

public class DragLayout extends ViewGroup{
    private View redView;
    private View blueView;
    private ViewDragHelper viewDragHelper;
   // private Scroller scroller;

    public DragLayout(Context context) {
        super(context);
        init();
    }

    public DragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        viewDragHelper = ViewDragHelper.create(this,callback);
       // scroller = new Scroller(getContext());
    }

    /**
     * 当DragLayout的XML布局的结束标签被读完后执行该方法，此时会知道自己有几个子View
     * 一般用来初始化子View的引用
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        redView = getChildAt(0);
        blueView = getChildAt(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //1.测量子View
//        int size = getResources().getDimension(R.dimen.width);

        int measureSpec = MeasureSpec.makeMeasureSpec(redView.getLayoutParams().width,MeasureSpec.EXACTLY);
        redView.measure(measureSpec,measureSpec);
        blueView.measure(measureSpec,measureSpec);
        /*
        2.测量子控件的方法
        measureChild(redView,widthMeasureSpec,heightMeasureSpec);
        measureChild(blueView,widthMeasureSpec,heightMeasureSpec);
        */
        /**
         * 3.可以继承系统已有的布局，如FrameLayout
         */
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        int left = getPaddingLeft()/*+getMeasuredWidth()/2-redView.getMeasuredWidth()/2*/;
        int top = getPaddingTop();
        redView.layout(left,top,left+redView.getMeasuredHeight(),top+redView.getMeasuredHeight());
        blueView.layout(left,redView.getBottom(),
                left+blueView.getMeasuredWidth(),redView.getBottom()+blueView.getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //让viewDragHelper帮助我们判断是否应该拦截
        boolean result = viewDragHelper.shouldInterceptTouchEvent(ev);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将触摸事件交给viewDragHelper处理
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

        /**
         * 捕获View
         * @param child
         * @param pointerId
         * @return
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child==blueView||child==redView;
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        /**
         * 控制水平开关
         * @param child
         * @param left left = chile.getLeft()+dx
         * @param dx 本次child水平方向移动的值
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if(left<0){
                left=0;
            }else if(left>(getMeasuredWidth()-child.getMeasuredWidth())){
                left=getMeasuredWidth()-child.getMeasuredWidth();
            }
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if(top<0){
                top=0;
            }else if (top>(getMeasuredHeight()-child.getMeasuredHeight())){
                top=getMeasuredHeight()-child.getMeasuredHeight();
            }
            return top;
        }

        /**
         * 水平方向拖拽范围,目前不能限制边界
         *抬起手指的动画时间，最好不要返回0；
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return super.getViewHorizontalDragRange(child);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return super.getViewVerticalDragRange(child);
        }

        /**
         * 可以实现一个View跟随另一个View移动
         * @param changedView
         * @param left
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if(changedView == blueView){
                redView.layout(redView.getLeft()+dx,redView.getTop()+dy,
                        redView.getRight()+dx,redView.getBottom()+dy);
            }
            //计算移动百分比
            float fraction = changedView.getLeft()*1f/(getMeasuredWidth()-changedView.getMeasuredWidth());
            executeAnimation(fraction);
        }

        /**
         * 当手指抬起时执行该方法
         * @param releasedChild
         * @param xvel x方向移动的速度
         * @param yvel y方向移动的速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            int centerLeft = getMeasuredWidth() /2- releasedChild.getMeasuredWidth()/2;
            if(releasedChild.getLeft()<centerLeft){
              //  scroller.startScroll();
                //invalid();
                viewDragHelper.smoothSlideViewTo(releasedChild,0,releasedChild.getTop());
                ViewCompat.postInvalidateOnAnimation(DragLayout.this);
            }else {
                viewDragHelper.smoothSlideViewTo(releasedChild,getMeasuredWidth()-releasedChild.getMeasuredWidth(),releasedChild.getTop());
                ViewCompat.postInvalidateOnAnimation(DragLayout.this);
            }
        }
    };

    @Override
    public void computeScroll() {
        if(viewDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(DragLayout.this);
        }
    }

    /**
     * 实现伴随动画
     * @param franction 百分比
     */
    private void executeAnimation(float franction){

        //3.0之后可以用的redView.setScaleX(1+0.5f*franction);
        //使用ViewHelper兼容3.0之前的版本，需导包nineoldAndroids
        /*
        ViewHelper.setScaleX(redView,1+0.5f*franction);
        ViewHelper.setScaleY(redView,1+0.5f*franction);
        */
        ViewHelper.setRotation(redView,360*franction);
        ViewHelper.setRotationX(redView,360*franction);
        ViewHelper.setRotationY(redView,360*franction);
        ViewHelper.setRotationX(blueView,360*franction);

        //设置过渡颜色
        redView.setBackgroundColor((Integer) ColorUtil.evaluateColor(franction,
                Color.RED,Color.GREEN));
        /*setBackgroundColor((Integer) ColorUtil.evaluateColor(franction,
                Color.RED,Color.GREEN));*/
    }
}
