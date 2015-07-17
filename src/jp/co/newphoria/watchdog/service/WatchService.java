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

/**
 * 監視サービス
 *
 * @author Zhong Zhicong
 * @time 2015-7-17
 */
public class WatchService extends Service {
	// ログタッグ
	private final static String TAG = "WatchService";
	// 監視状態
	private boolean mIsWatching = false;

	// ディフォルトバンダー
	public WatchServiceBinder mBinder = new WatchServiceBinder();

	// 監視時間間隔
	private int mTimeInterval = 3;

	// アクティビティ管理器、パッケージ情報取得用
	private ActivityManager mActivityManager;

	// パッケージ名
	private String mPackageName;
	// 起動クラス名
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

	// サービスcallback用インターフェース
	private IWatchService mIWatchService;

	// アクティビティ呼ぶ用インターフェース
	public class WatchServiceBinder extends Binder {
		public WatchService getService() {
			return WatchService.this;
		}

		// callback用インターフェース指定
		public void setInterface(IWatchService i) {
			mIWatchService = i;
		}

		// 監視状態取得
		public boolean getWatchingStatue() {
			return mIsWatching;
		}

		// 監視開始
		public void startWatch() {
			if (!mIsWatching) {

				mPackageName = mIWatchService.getTestPackageName();
				mClassName = ProcessInfo.getClassNameByPkgName(
						getPackageManager(), mPackageName);

				// パッケージ名入力チェック
				if (mPackageName == null || mPackageName.length() == 0) {
					mIWatchService.updateLogText("package name is null");
					return;
				}

				// 入力したアプリ存在チェック
				if (mClassName == null) {
					mIWatchService.updateLogText("package not found");
					return;
				}

				mIsWatching = true;
				mIWatchService.setStatusText("監視中");
				mIWatchService.updateLogText("start watching");
				// 監視ハンドラー送信、監視開始
				mWatchHandler.sendEmptyMessage(0);
			}
		}

		// 監視終了
		public void stopWatch() {
			if (mIsWatching) {
				mIWatchService.setStatusText("監視中止");
				mIWatchService.updateLogText("stop watching");
				mIsWatching = false;
			}
		}

		// 監視中パッケージ名取得
		public String getPackageName() {
			return mPackageName;
		}
	}

	// 監視用ハンドラー
	WatchHandler mWatchHandler = new WatchHandler();

	// 監視用ハンドラー
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

				// プロセスID取得
				int pid = ProcessInfo.getPidByPName(mActivityManager,
						mPackageName);
				android.util.Log.d(TAG, "pid = " + pid);
				// MOCK
				// if(Math.random()<0.95){
				if (pid != -1) {
					// 監視されるアプリが働いている
					mIWatchService.updateLogText("[" + Util.getTime() + "]"
							+ mPackageName + " is running");
					// 監視時間間隔後、監視ハンドラーに再送信
					this.sendEmptyMessageDelayed(0, mTimeInterval * 1000);
				} else {
					// 監視されるアプリが働いてない
					mIWatchService.updateLogText("[" + Util.getTime() + "]"
							+ mPackageName + " is stop");

					mIWatchService.updateLogText("[" + Util.getTime()
							+ "]pull up app \n package name = " + mPackageName
							+ "\n class name = " + mClassName);

					// 該当アプリ再起動
					if (pullUpApp(mPackageName, mClassName) < 0) {
						mIWatchService.updateLogText(mPackageName
								+ " start failed");
					}

					// 監視時間間隔後、監視ハンドラーに再送信
					this.sendEmptyMessageDelayed(0, mTimeInterval * 1000);
				}
			} else {
				// 監視状態falseになって、監視終了
				android.util.Log.d(TAG, "is Watching = false, stop watch");
			}
		}
	}

	/**
	 * 該当アプリを起動
	 * 
	 * @param pkgName
	 *            　パッケージ名
	 * @param clsName
	 *            　起動クラス名
	 * @return　0:起動成功　-1:失敗
	 */
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
