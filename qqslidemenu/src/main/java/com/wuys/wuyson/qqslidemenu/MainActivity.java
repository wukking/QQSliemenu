package com.wuys.wuyson.qqslidemenu;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ListView menu_listView,main_listView;
    private SlideMenuLayout slideMenuLayout;
    private ImageView iv_head;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slideMenuLayout = (SlideMenuLayout) findViewById(R.id.SlideMenu);
        iv_head = (ImageView) findViewById(R.id.iv_head);

        menu_listView = (ListView) findViewById(R.id.menu_list);
        menu_listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                Constant.sCheeseStrings){
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.WHITE);
                return textView;
            }
        });

        main_listView = (ListView) findViewById(R.id.main_listview);
        main_listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,
                Constant.NAMES){
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView==null?super.getView(position, convertView, parent):convertView;
                //textView.setScaleX(0.5f);
                ViewHelper.setScaleX(view,0.5f);
                ViewHelper.setScaleY(view,0.5f);
                //属性动画放大
                ViewPropertyAnimator.animate(view).scaleX(1).setDuration(350).start();
                ViewPropertyAnimator.animate(view).scaleY(1).setDuration(350).start();
                return view;
            }
        });

        slideMenuLayout.setOnDragStateChangeListener(new SlideMenuLayout.OnDragStateChangeListener() {
            @Override
            public void OnOpen() {
                Log.e("TAG","OnOpen");
                menu_listView.smoothScrollToPosition(new Random().nextInt(menu_listView.getCount()));
            }

            @Override
            public void OnClose() {
                Log.e("TAG","OnClose");
                ViewPropertyAnimator.animate(iv_head).translationX(15).
                        setInterpolator(new CycleInterpolator(4)).setDuration(500).start();
            }

            @Override
            public void OnDraging(float fraction) {
                Log.e("TAG","OnDraging"+fraction);
                ViewHelper.setAlpha(iv_head,1-fraction);
            }
        });

    }
}
