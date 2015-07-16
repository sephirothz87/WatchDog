package jp.co.newphoria.watchdog;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;

public class MainActivityFrag extends FragmentActivity {
	private FragmentTabHost mTabHost;

	private LayoutInflater mLayoutInflater;

	private Class mFragments[] = { Fragment01.class, Fragment02.class,
			Fragment03.class, Fragment04.class };

	private int mNavImages[] = { R.drawable.btn_nav_01, R.drawable.btn_nav_02,
			R.drawable.btn_nav_02, R.drawable.btn_nav_02 };
	
	private String mTabTag[]={"home","extend2","extend3","extend4"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_ori_bak);

		mLayoutInflater = LayoutInflater.from(this);

		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.main_content);

		int count = mFragments.length;

		for (int i = 0; i < count; i++) {
			TabSpec tabSpec = mTabHost.newTabSpec(mTabTag[i]).setIndicator(
					getTabItemView(i));
			mTabHost.addTab(tabSpec, mFragments[i], null);
			
//			mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.nav_background);
		}
	}

	private View getTabItemView(int index) {
		View view = mLayoutInflater.inflate(R.layout.view_tab_item, null);

		ImageView im = (ImageView) view.findViewById(R.id.image_view);
		im.setImageResource(mNavImages[index]);

		return view;
	}
}
