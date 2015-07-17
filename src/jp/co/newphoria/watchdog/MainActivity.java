package jp.co.newphoria.watchdog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;

public class MainActivity extends FragmentActivity {

	private FragmentTabHost mTabHost;

	private LayoutInflater mLayoutInflater;

	@SuppressWarnings("rawtypes")
	private Class mFragments[] = { Fragment01.class, Fragment02.class,
			Fragment03.class, Fragment04.class };

	private int mNavImages[] = { R.drawable.btn_nav_01, R.drawable.btn_nav_02,
			R.drawable.btn_nav_02, R.drawable.btn_nav_02 };

	private String mTabTag[] = { "home", "extend2", "extend3", "extend4" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mLayoutInflater = LayoutInflater.from(this);

		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.main_content);

		int count = mFragments.length;

		for (int i = 0; i < count; i++) {
			TabSpec tabSpec = mTabHost.newTabSpec(mTabTag[i]).setIndicator(
					getTabItemView(i));
			mTabHost.addTab(tabSpec, mFragments[i], null);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			showExitDialog();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	protected void showExitDialog() {
		AlertDialog.Builder builder = new Builder(MainActivity.this);
		builder.setMessage("アプリ停止し、監視終了がいいですか。");
		builder.setTitle("注意");
		builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
			}
		});
		builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private View getTabItemView(int index) {
		View view = mLayoutInflater.inflate(R.layout.view_tab_item, null);

		ImageView im = (ImageView) view.findViewById(R.id.image_view);
		im.setImageResource(mNavImages[index]);

		return view;
	}
}
