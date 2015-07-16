package jp.co.newphoria.watchdog;

import jp.co.newphoria.watchdog.service.IWatchService;
import jp.co.newphoria.watchdog.service.WatchService;
import jp.co.newphoria.watchdog.service.WatchService.WatchServiceBinder;
import jp.co.newphoria.watchdog.util.Util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class MainActivity extends FragmentActivity {

	private TextView mTextStatus;
	private TextView mTextLog;
	private ScrollView mScrollLog;

	private Button mButtonStartWatch;
	private Button mButtonStopWatch;

	private Handler mHandlerLogText = new Handler();
	
	private FragmentTabHost mTabHost;

	private LayoutInflater mLayoutInflater;

	private Class mFragments[] = { Fragment01.class, Fragment02.class,
			Fragment03.class, Fragment04.class };

	private int mNavImages[] = { R.drawable.btn_nav_01, R.drawable.btn_nav_02,
			R.drawable.btn_nav_02, R.drawable.btn_nav_02 };
	
	private String mTabTag[]={"home","extend2","extend3","extend4"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// test
		android.util.Log.d(Util.COMMON_TAG, "build no.0005");

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
			
//			mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.nav_background);
		}

	    mIntent = new Intent(MainActivity.this, WatchService.class);
	    bindService(mIntent, mServiceConnection, Service.BIND_AUTO_CREATE);

		mTextStatus = (TextView) findViewById(R.id.text);
		mTextLog = (TextView) findViewById(R.id.log);

		mScrollLog = (ScrollView) findViewById(R.id.scroll_log);

		mButtonStartWatch = (Button) findViewById(R.id.button_01_start_watch);

		mButtonStopWatch = (Button) findViewById(R.id.button_02_stop_watch);

//		mButtonStartWatch.setOnClickListener(mButtonStartWatchListener);
//		mButtonStopWatch.setOnClickListener(mButtonStoptWatchListener);
	}

	@Override
	protected void onDestroy() {
		// test
		android.util.Log.d(Util.COMMON_TAG, "on destroy");
		unbindService(mServiceConnection);
		super.onDestroy();
	}

	OnClickListener mButtonStartWatchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// test
			android.util.Log.d(Util.COMMON_TAG,
					"mButtonStartWatchListener funced");
			// mTextStatus.setText("mButtonStartWatchListener funced");

			// android.util.Log.d(Util.COMMON_TAG,
			// "mButtonStartWatchListener funced");
			// PackageInfo.printProcessList(mActivityManager);
			//
			// int pid = PackageInfo.getPidByPName(mActivityManager,
			// Util.PACKAGE_NAME);
			// android.util.Log.d(Util.COMMON_TAG, Util.PACKAGE_NAME +
			// "'s PID = "
			// + pid);
			// doStartApplicationWithPackageName(Util.PACKAGE_NAME);
			
//		    mIntent = new Intent(MainActivity.this, WatchService.class);
//		    bindService(mIntent, mServiceConnection, Service.BIND_AUTO_CREATE);

			mWatchServiceBinder.startWatch();
		}
	};
	
	OnClickListener mButtonStoptWatchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// test
			android.util.Log.d(Util.COMMON_TAG,
					"mButtonStopWatchListener funced");
			// mTextStatus.setText("mButtonStoptWatchListener funced");
//			unbindService(mServiceConnection);

			mWatchServiceBinder.stopWatch();
		}
	};
	
	public IWatchService mIWatchService = new IWatchService() {
		@Override
		public void setStatusText(String s) {
			mTextStatus.setText(s);
		}

		@Override
		public void updateLogText(String log) {
			mTextLog.append("\n" + log);
			mHandlerLogText.post(new Runnable() {
				@Override
				public void run() {
					mScrollLog.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
		}

		@Override
		public void pullUpApp() {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			ComponentName com_name = new ComponentName(
					Util.PACKAGE_NAME, Util.CLASS_NAME);
			intent.setComponent(com_name);
			startActivity(intent);
		}
	};

	WatchServiceBinder mWatchServiceBinder;
	Service mWatchService;

	Intent mIntent;
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			android.util.Log.d(Util.COMMON_TAG, "onServiceConnected is called");
			mWatchServiceBinder = (WatchServiceBinder) service;
			mWatchService = mWatchServiceBinder.getService();
			mWatchServiceBinder.setInterface(mIWatchService);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			android.util.Log.d(Util.COMMON_TAG,
					"onServiceDisconnected is called");
			mWatchService = null;
			mWatchServiceBinder = null;
		}
	};
   
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			android.util.Log.d(Util.COMMON_TAG, "back clicked");
			showExitDialog();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	protected void showExitDialog(){
		AlertDialog.Builder builder=new Builder(MainActivity.this);
		builder.setMessage("アプリ停止し、監視終了がいいですか。");
		builder.setTitle("注意");
		builder.setPositiveButton("はい", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
			}
		});
		builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener(){
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
