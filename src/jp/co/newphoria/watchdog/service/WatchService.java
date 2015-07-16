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
		android.util.Log.d(TAG, "onBind");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		android.util.Log.d(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		android.util.Log.d(TAG, "onCreate");

		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		android.util.Log.d(TAG, "onStart");
	}

	@Override
	public void onDestroy() {
		mIsWatching = false;
		android.util.Log.d(TAG, "onDestroy");

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
				android.util.Log.d(TAG, "start watching");
				mIsWatching=true;
				mIWatchService.setStatusText("監視中");
				mWatchHandler.sendEmptyMessage(0);
			}
		}
		
		public void stopWatch(){
			if (mIsWatching) {
				mIWatchService.setStatusText("監視中止");
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
				
//				int pid = ProcessInfo.getPidByPName(mIWatchService.getActivityManager(),
//						Util.PACKAGE_NAME);
				int pid = ProcessInfo.getPidByPName(mActivityManager,
						Util.PACKAGE_NAME);
				android.util.Log.d(TAG, "pid = " + pid);
				// MOCK
				// if(Math.random()<0.95){
				if (pid != -1) {
					// TODO 写入日志，XXX程序运行中
					android.util.Log.d(TAG, "XXX is running");
					mIWatchService.updateLogText("[" + Util.getTime() + "]XXX is running");
					this.sendEmptyMessageDelayed(0, mTimeInterval * 1000);
				} else {
					// TODO 写入日志，XXX程序已停止
					android.util.Log.d(TAG, "XXX is stop");
					mIWatchService.updateLogText("[" + Util.getTime() + "]XXX is stop");
					// TODO 拉起程序
					android.util.Log.d(TAG, "pull up XXX");

					mIWatchService.pullUpApp();
					
					mIWatchService.updateLogText("[" + Util.getTime() + "]pull up XXX");
					this.sendEmptyMessageDelayed(0, mTimeInterval * 1000);
				}
			} else {
				android.util.Log.d(TAG,
						"is Watching = false, stop watch");
			}
		}
	}
}
