package jp.co.newphoria.watchdog.service;

import jp.co.newphoria.watchdog.R;
import jp.co.newphoria.watchdog.module.ProcessInfo;
import jp.co.newphoria.watchdog.util.Util;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

/**
 * 監視サービス
 *
 * @author Zhong Zhicong
 * @time 2015-7-17
 * ---------------------------------変更履歴---------------------------------
 * 日付 			変更者 			説明 
 * 2015-7-22	Zhong Zhicong	サービス起動方式、bind→startになる。callback方式は、broadcastで実現 
 * 2015-7-22	Zhong Zhicong	監視対象パッケージ名、監視かどうか情報をSharedPreferencedに保存
 * 2015-7-24	Zhong Zhicong	Callback用BroadcastReceiverはLocalBroadcastManager利用になる
 * 2015-7-30	Zhong Zhicong	機能追加：監視対象アプリ最前ではない時、アプリをプールアップ。例外：WatchDog本体の制御機能利用のため、WatchDogアプリが最前時、監視対象アプリをプールアップしない
 * 2015-7-30	Zhong Zhicong	スクリーン閉める/ロック状態を監視し、該当時にスクリーンをライトアップ/アンロックする
 * 2015-8-04	Zhong Zhicong	通知表示機能追加
 * 2015-8-04	Zhong Zhicong	自動的にロック有効/無効設定機能追加
 * 2015-8-04	Zhong Zhicong	WatchDogアプリが最前判定方式変更
 */
@SuppressWarnings("deprecation")
public class WatchService extends Service {
	// ログタッグ
	private final static String TAG = "WatchService";

	// Handlerメッセージ：メッセージ削除
	private final static int MSG_REMOVE = 1;
	// Handlerメッセージ：監視
	private final static int MSG_WATCH = 2;

	// 監視時間間隔
	private int mTimeInterval = 3;

	// アクティビティ管理器、パッケージ情報取得用
	private ActivityManager mActivityManager;

	// 情報読出し用
	private SharedPreferences mSharedPrefer;

	// BroadCastレジースト用マネジャー
	LocalBroadcastManager mLocalBroadcastManager;

	// 通知表示マネジャー対象
	private NotificationManager mNotificationManager;

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
		mSharedPrefer = getSharedPreferences(Util.DEFAULT_SHARE_NAME,
				Activity.MODE_PRIVATE);
		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Message msg = new Message();
		msg.what = MSG_WATCH;
		mWatchHandler.removeMessages(MSG_WATCH);
		mWatchHandler.sendMessageDelayed(msg, mTimeInterval * 1000);

		// 通知表示
		if (mSharedPrefer.getBoolean(Util.DEFAULT_SHARE_KEY_IS_NOTICE_ENABLE,
				false)
				&& mSharedPrefer.getBoolean(Util.DEFAULT_SHARE_KEY_IS_WATCHING,
						false)) {
			createNotification();
		}

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
				String pkg_name = mSharedPrefer.getString(
						Util.DEFAULT_SHARE_KEY_PKG_NAME, Util.PACKAGE_NAME);
				String cls_name = ProcessInfo.getClassNameByPkgName(
						getPackageManager(), pkg_name);

				if (mSharedPrefer.getBoolean(
						Util.DEFAULT_SHARE_KEY_IS_WATCHING, false)) {

					if (mSharedPrefer.getBoolean(
							Util.DEFAULT_SHARE_KEY_IS_AUTO_UNLOCK, false)) {
						// スクリーン状態取得
						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
						boolean isScreenOn = pm.isScreenOn();
						android.util.Log.d(TAG, "isScreenOn = " + isScreenOn);

						KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
						boolean inputMode = km.inKeyguardRestrictedInputMode();
						android.util.Log.d(TAG, "inputMode = " + inputMode);

						// スクリーンライトアップ
						if (!isScreenOn) {
							android.util.Log.d(TAG, "light up screen");
							PowerManager.WakeLock wl = pm
									.newWakeLock(
											PowerManager.ACQUIRE_CAUSES_WAKEUP
													| PowerManager.SCREEN_DIM_WAKE_LOCK,
											"LightUp");
							wl.acquire();
							wl.release();
						}

						// スクリーンアンロック
						KeyguardLock kl = km.newKeyguardLock("Locker");
						android.util.Log.d(TAG, "unlock screen");
						kl.disableKeyguard();
					}

					// RunningAppProcessInfo対象取得
					RunningAppProcessInfo rainfo = ProcessInfo
							.getProcessInfoByPName(mActivityManager, pkg_name);

					// MOCK
					// if(Math.random()<0.95){
					if (rainfo != null) {
						// 監視されるアプリが働いている
						setLogText(pkg_name + " is running");
						android.util.Log.d(TAG, pkg_name + " is running");
						// Util.writeLog(pkg_name + " is running");

						if (!ProcessInfo.isTopProcess(mActivityManager, rainfo)) {
							// 案1 back up
							// // 監視対象アプリは最前ではない、WatchDog最前Activity取得
							// List<RunningTaskInfo> runningTasks =
							// mActivityManager
							// .getRunningTasks(10);
							// RunningTaskInfo rinfo = runningTasks.get(0);
							// ComponentName component = rinfo.topActivity;
							// android.util.Log.d(TAG,
							// "watchdog topActivity classname = "
							// + component.getClassName());
							//
							// if (!component.getClassName().equals(
							// Util.WATCH_DOG_LANCHER_NAME)) {
							// // WatchDogは最前ではない、監視対象アプリをプールアップ
							// android.util.Log.d(TAG, pkg_name
							// + " is running background");
							// // 該当アプリ再起動
							// if (pullUpApp(pkg_name, cls_name) < 0) {
							// // 起動失敗
							// setLogText(cls_name + " start failed");
							// android.util.Log.d(TAG, cls_name
							// + " start failed");
							// // Util.writeLog(cls_name +
							// // " start failed");
							// }
							// } else {
							// // WatchDogは最前で、監視対象アプリをプールアップしない
							// android.util.Log
							// .d(TAG,
							// pkg_name
							// +
							// " is running background, watchdog is running in front.");
							// }

							// 案2
							// 監視対象アプリは最前ではない、WatchDog最前Activity取得
							RunningAppProcessInfo rainfo_watchdog = ProcessInfo
									.getProcessInfoByPName(mActivityManager,
											Util.WATCH_DOG_PACKAGE_NAME);
							if (rainfo_watchdog != null) {
								if (!ProcessInfo.isTopProcess(mActivityManager,
										rainfo_watchdog)) {
									// WatchDogは最前ではない、監視対象アプリをプールアップ
									android.util.Log.d(TAG, pkg_name
											+ " is running background");
									// 該当アプリ再起動
									if (pullUpApp(pkg_name, cls_name) < 0) {
										// 起動失敗
										setLogText(cls_name + " start failed");
										android.util.Log.d(TAG, cls_name
												+ " start failed");
										// Util.writeLog(cls_name +
										// " start failed");
									}
								} else {
									// WatchDogは最前で、監視対象アプリをプールアップしない
									android.util.Log
											.d(TAG,
													pkg_name
															+ " is running background, watchdog is running in front.");
								}
							}
						} else {
							android.util.Log.d(TAG, pkg_name
									+ " is running in front");
						}
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

					}

					// 監視時間間隔後、監視ハンドラーに再送信
					Message m = new Message();
					m.what = MSG_WATCH;
					this.sendMessageDelayed(m, mTimeInterval * 1000);
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
		i.putExtra(Util.MSG_UPDATE_LOG, log);

		mLocalBroadcastManager.sendBroadcast(i);
	}

	// 通知表示
	private void createNotification() {
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Notification.Builder builder = new Notification.Builder(this);

		// 　タイトル指定
		builder.setContentTitle(Util.TXT_NOTIFICATION_TITLE);
		// 　アイコン指定
		builder.setSmallIcon(R.drawable.ic_launcher);
		// 　内容指定
		if (mSharedPrefer.getBoolean(Util.DEFAULT_SHARE_KEY_IS_WATCHING, false)) {
			builder.setContentText(Util.TXT_START_WATCHING);
		} else {
			builder.setContentText(Util.TXT_STOP_WATCHING);
		}
		// クリック動作指定
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		ComponentName com_name = new ComponentName(Util.WATCH_DOG_PACKAGE_NAME,
				Util.WATCH_DOG_LANCHER_NAME);
		intent.setComponent(com_name);
		PendingIntent p_intent = PendingIntent.getActivity(
				getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(p_intent);

		// 　通知作成
		Notification n = builder.build();
		n.flags = Notification.FLAG_ONGOING_EVENT;
		mNotificationManager.notify(0, n);
	}
}
