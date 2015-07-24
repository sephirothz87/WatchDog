package jp.co.newphoria.watchdog.service;

import jp.co.newphoria.watchdog.module.ProcessInfo;
import jp.co.newphoria.watchdog.util.Util;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

/**
 * 監視サービス
 *
 * @author Zhong Zhicong
 * @time 2015-7-17
 * ---------------------------------変更履歴---------------------------------
 * 日付 			変更者 			説明 
 * 2015-7-22	Zhong Zhicong 	サービス起動方式、bind→startになる。callback方式は、broadcastで実現 
 * 2015-7-22	Zhong Zhicong 	監視対象パッケージ名、監視かどうか情報をSharedPreferencedに保存
 * 2015-7-24	Zhong Zhicong 	Callback用BroadcastReceiverはLocalBroadcastManager利用になる
 */
public class WatchService extends Service {
	// ログタッグ
	private final static String TAG = "WatchService";

	private final static int MSG_REMOVE = 1;
	private final static int MSG_WATCH = 2;

	// 監視時間間隔
	private int mTimeInterval = 3;

	// アクティビティ管理器、パッケージ情報取得用
	private ActivityManager mActivityManager;

	// 情報読出し用
	private SharedPreferences mSharedPrefer;

	// BroadCastレジースト用マネジャー
	LocalBroadcastManager mLocalBroadcastManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mSharedPrefer = getSharedPreferences("option", Activity.MODE_PRIVATE);
		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Message msg = new Message();
		msg.what = MSG_WATCH;
		mWatchHandler.removeMessages(MSG_WATCH);
		mWatchHandler.sendMessageDelayed(msg, mTimeInterval * 1000);

		// Util.writeLog("service start command");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
			switch (msg.what) {
			case MSG_REMOVE:
				android.util.Log.d(TAG, "remove msg");
				break;
			case MSG_WATCH:
				String pkg_name = mSharedPrefer.getString("PACKAGE_NAME",
						"jp.co.newphoria.signagedemo1");
				String cls_name = ProcessInfo
						.getClassNameByPkgName(getPackageManager(),
								mSharedPrefer.getString("PACKAGE_NAME",
										"jp.co.newphoria.signagedemo1"));
				if (mSharedPrefer.getBoolean(
						Util.DEFAULT_SHARE_KEY_IS_WATCHING, false)) {
					// プロセスID取得
					int pid = ProcessInfo.getPidByPName(mActivityManager,
							pkg_name);
					android.util.Log.d(TAG, "pid = " + pid);
					// Util.writeLog("pid = " + pid);
					// MOCK
					// if(Math.random()<0.95){
					if (pid != -1) {
						// 監視されるアプリが働いている
						setLogText(pkg_name + " is running");
						android.util.Log.d(TAG, pkg_name + " is running");
						// Util.writeLog(pkg_name + " is running");
						// 監視時間間隔後、監視ハンドラーに再送信
						Message m = new Message();
						m.what = MSG_WATCH;
						this.sendMessageDelayed(m, mTimeInterval * 1000);
					} else {
						// 監視されるアプリが働いてない
						setLogText(pkg_name + " is stop");
						android.util.Log.d(TAG, pkg_name + " is stop");
						// Util.writeLog(pkg_name + " is stop");

						// 該当アプリ再起動
						if (pullUpApp(pkg_name, cls_name) < 0) {
							// 起動失敗
							setLogText(cls_name + " start failed");
							android.util.Log.d(TAG, cls_name + " start failed");
							// Util.writeLog(cls_name + " start failed");
						}

						// 監視時間間隔後、監視ハンドラーに再送信
						Message m = new Message();
						m.what = MSG_WATCH;
						this.sendMessageDelayed(m, mTimeInterval * 1000);
					}
				} else {
					// 監視状態falseになって、監視終了
					setLogText("stop watch");
					android.util.Log.d(TAG, "is Watching = false, stop watch");
					// Util.writeLog("is Watching = false, stop watch");
				}
				break;
			default:
				break;
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
			setLogText("pull up app\npkgName = " + pkgName + "\nclsName = "
					+ clsName);
			android.util.Log.d(TAG, "pull up app\npkgName = " + pkgName
					+ "\nclsName = " + clsName);
			// Util.writeLog("pull up app\npkgName = " + pkgName +
			// "\nclsName = "
			// + clsName);
			ComponentName com_name = new ComponentName(pkgName, clsName);
			intent.setComponent(com_name);

			getApplication().startActivity(intent);
		} catch (Exception e) {
			setLogText("pullUpApp fail");
			android.util.Log.d(TAG, "pullUpApp fail");
			// Util.writeLog("pullUpApp fail");
			return -1;
		}

		return 0;
	}

	private void setLogText(String log) {
		Intent i = new Intent(Util.BROADCAST_ACTION_LOG);
		i.putExtra("msg", log);

		mLocalBroadcastManager.sendBroadcast(i);
	}
}
