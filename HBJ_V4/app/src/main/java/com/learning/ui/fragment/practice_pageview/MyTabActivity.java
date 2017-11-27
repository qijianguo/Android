package com.learning.ui.fragment.practice_pageview;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.learning.R;

import java.util.ArrayList;
import java.util.List;

public class MyTabActivity extends Activity implements View.OnClickListener{

    private ViewPager viewPager;

    private PagerAdapter pagerAdapter;

    private List<View> viewList = new ArrayList<>();

    private LinearLayout weixin;
    private LinearLayout fre;
    private LinearLayout address;
    private LinearLayout setting;

    private ImageButton weixinImg;
    private ImageButton freImg;
    private ImageButton addressImg;
    private ImageButton settingImg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tab);
        
        initView();

        initEvents();
    }

    private void initEvents() {
        weixinImg.setOnClickListener(this);
        freImg.setOnClickListener(this);
        addressImg.setOnClickListener(this);
        settingImg.setOnClickListener(this);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int currentItem = viewPager.getCurrentItem();
                resetImg();
                switch (currentItem) {
                    case 0:
                        weixinImg.setImageResource(R.drawable.tab_weixin_pressed);
                        break;
                    case 1:
                        freImg.setImageResource(R.drawable.tab_find_frd_pressed);
                        break;
                    case 2:
                        addressImg.setImageResource(R.drawable.tab_address_pressed);
                        break;
                    case 3:
                        settingImg.setImageResource(R.drawable.tab_settings_pressed);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initView() {

        viewPager = findViewById(R.id.view_page);

        weixin = findViewById(R.id.weixin);
        fre = findViewById(R.id.fre);
        address = findViewById(R.id.address);
        setting = findViewById(R.id.setting);

        weixinImg = findViewById(R.id.weixin_img);
        freImg = findViewById(R.id.fre_img);
        addressImg = findViewById(R.id.address_img);
        settingImg = findViewById(R.id.setting_img);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View winxin = layoutInflater.inflate(R.layout.tab_weixin, null);
        View fre = layoutInflater.inflate(R.layout.tab_fre, null);
        View address = layoutInflater.inflate(R.layout.tab_address, null);
        View setting = layoutInflater.inflate(R.layout.tab_setting, null);

        viewList.add(winxin);
        viewList.add(fre);
        viewList.add(address);
        viewList.add(setting);

        pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                super.destroyItem(container, position, object);
                container.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = viewList.get(position);
                container.addView(view);
                return view;
            }
        };

        viewPager.setAdapter(pagerAdapter);


    }

    @Override
    public void onClick(View view) {

        resetImg();
        switch (view.getId()) {
            case R.id.weixin_img:
                viewPager.setCurrentItem(0);
                weixinImg.setImageResource(R.drawable.tab_weixin_pressed);
                break;
            case R.id.fre_img:
                viewPager.setCurrentItem(1);
                freImg.setImageResource(R.drawable.tab_find_frd_pressed);
                break;
            case R.id.address_img:
                viewPager.setCurrentItem(2);
                addressImg.setImageResource(R.drawable.tab_address_pressed);
                break;
            case R.id.setting_img:
                viewPager.setCurrentItem(3);
                settingImg.setImageResource(R.drawable.tab_settings_pressed);
                break;
        }
    }

    /**
     * 将所有的图片置为灰色
     */
    private void resetImg() {
        weixinImg.setImageResource(R.drawable.tab_weixin_normal);
        freImg.setImageResource(R.drawable.tab_find_frd_normal);
        addressImg.setImageResource(R.drawable.tab_address_normal);
        settingImg.setImageResource(R.drawable.tab_settings_normal);
    }
}
