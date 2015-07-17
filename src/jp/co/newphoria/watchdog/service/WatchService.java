package jp.co.newphoria.watchdog.service;

import jp.co.newphoria.watchdog.module.ProcessInfo;
import jp.co.newphoria.watchdog.util.Util;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

public class WatchService extends Service {
	private final static String TAG = "WatchService";
	private boolean mIsWatching = false;

	public WatchServiceBinder mBinder = new WatchServiceBinder();

	private int mTimeInterval = 3;

	private ActivityManager mActivityManager;

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
				int pid = ProcessInfo.getPidByPName(mActivityManager,
						Util.PACKAGE_NAME);
				android.util.Log.d(TAG, "pid = " + pid);
				// MOCK
				// if(Math.random()<0.95){
				if (pid != -1) {
					mIWatchService.updateLogText("[" + Util.getTime() + "]"
							+ Util.PACKAGE_NAME + " is running");
					this.sendEmptyMessageDelayed(0, mTimeInterval * 1000);
				} else {
					mIWatchService.updateLogText("[" + Util.getTime() + "]"
							+ Util.PACKAGE_NAME + " is stop");

					mIWatchService.pullUpApp();

					mIWatchService.updateLogText("[" + Util.getTime()
							+ "]pull up " + Util.PACKAGE_NAME);
					this.sendEmptyMessageDelayed(0, mTimeInterval * 1000);
				}
			} else {
				android.util.Log.d(TAG, "is Watching = false, stop watch");
			}
		}
	}
}
