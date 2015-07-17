package jp.co.newphoria.watchdog.service;

import jp.co.newphoria.watchdog.module.ProcessInfo;
import jp.co.newphoria.watchdog.util.Util;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

//TODO comment追加
public class WatchService extends Service {
	private final static String TAG = "WatchService";
	private boolean mIsWatching = false;

	public WatchServiceBinder mBinder = new WatchServiceBinder();

	private int mTimeInterval = 3;

	private ActivityManager mActivityManager;
	
	private String mPackageName;
	private String mClassName;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	}

	@Override
	public void onStart(Intent intent, int startId) {
	}

	@Override
	public void onDestroy() {
		mIsWatching = false;
		super.onDestroy();
	}

	public IWatchService mIWatchService;

	public class WatchServiceBinder extends Binder {
		public WatchService getService() {
			return WatchService.this;
		}

		public void setInterface(IWatchService i) {
			mIWatchService = i;
		}

		public boolean getWatchingStatue() {
			return mIsWatching;
		}

		public void startWatch() {
			if (!mIsWatching) {
				
				mPackageName=mIWatchService.getTestPackageName();
				mClassName=ProcessInfo.getClassNameByPkgName(getPackageManager(),mPackageName);
				
				if(mPackageName==null||mPackageName.length()==0){
					mIWatchService.updateLogText("package name is null");
					return;
				}
				
				if(mClassName==null){
					mIWatchService.updateLogText("package not found");
					return;
				}

				mIsWatching = true;
				mIWatchService.setStatusText("監視中");
				mIWatchService.updateLogText("start watching");
				mWatchHandler.sendEmptyMessage(0);
			}
		}

		public void stopWatch() {
			if (mIsWatching) {
				mIWatchService.setStatusText("監視中止");
				mIWatchService.updateLogText("stop watching");
				mIsWatching = false;
			}
		}
	}

	WatchHandler mWatchHandler = new WatchHandler();

	class WatchHandler extends Handler {
		public WatchHandler() {

		}

		public WatchHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mIsWatching) {
				if (mPackageName == null) {
					mPackageName = Util.PACKAGE_NAME;
				}

				int pid = ProcessInfo.getPidByPName(mActivityManager,
						mPackageName);
				android.util.Log.d(TAG, "pid = " + pid);
				// MOCK
				// if(Math.random()<0.95){
				if (pid != -1) {
					mIWatchService.updateLogText("[" + Util.getTime() + "]"
							+ mPackageName + " is running");
					this.sendEmptyMessageDelayed(0, mTimeInterval * 1000);
				} else {
					mIWatchService.updateLogText("[" + Util.getTime() + "]"
							+ mPackageName + " is stop");

					mIWatchService.updateLogText("[" + Util.getTime()
							+ "]pull up app \n package name = " + mPackageName
							+ "\n class name = " + mClassName);

//					if (mIWatchService.pullUpApp(mPackageName, mClassName) < 0) {
					if (pullUpApp(mPackageName, mClassName) < 0) {
						mIWatchService.updateLogText(mPackageName
								+ " start failed");
					}

					this.sendEmptyMessageDelayed(0, mTimeInterval * 1000);
				}
			} else {
				android.util.Log.d(TAG, "is Watching = false, stop watch");
			}
		}
	}

	public int pullUpApp(String pkgName, String clsName) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		try {
			ComponentName com_name = new ComponentName(pkgName, clsName);
			intent.setComponent(com_name);
			
			getApplication().startActivity(intent);
		} catch (Exception e) {
			android.util.Log.d(TAG, "pullUpApp fail");
			return -1;
		}
		
		return 0;
	}
}
