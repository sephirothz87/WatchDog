package jp.co.newphoria.watchdog;

import jp.co.newphoria.watchdog.module.ProcessInfo;
import jp.co.newphoria.watchdog.service.WatchService;
import jp.co.newphoria.watchdog.util.Util;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * アプリの主fragment
 *
 * @author Zhong Zhicong
 * @time 2015-7-17
 * ---------------------------------変更履歴---------------------------------
 * 日付 			変更者 			説明 
 * 2015-7-22 	Zhong Zhicong	サービス起動方式、bind→startになる。callback方式は、broadcastで実現 
 * 2015-7-22	Zhong Zhicong	監視対象パッケージ名、監視かどうか情報をSharedPreferencedに保存
 * 2015-7-24	Zhong Zhicong	OS起動する時自動的に監視かどうか設定機能追加
 * 2015-7-24	Zhong Zhicong	Callback用BroadcastReceiverはLocalBroadcastManager利用になる
 * 2015-8-04	Zhong Zhicong	通知表示と有効/無効設定機能追加
 * 2015-8-04	Zhong Zhicong	自動的にロック有効/無効設定機能追加
 */
public class Fragment01 extends Fragment {
	// ログタッグ
	private final static String TAG = "Fragment01";

	// パッケージ名チェックステータステキスト
	private final String TXT_PKGNAME_CHECK = "パッケージなし、チェックし再入力してください";

	// 監視したいパッケージ入力枠
	private EditText mEditTextPackage;

	// OS起動自動的に監視開始設定チェックボックス
	private CheckBox mCheckBoxIsBootStart;
	// 自動的にロック解除設定チェックボックス
	private CheckBox mCheckBoxAutoUnlock;
	// 通知表示設定チェックボックス
	private CheckBox mCheckBoxNotice;

	// 監視状態表示テキスト
	private TextView mTextStatus;
	// 監視情報表示テキスト
	private TextView mTextLog;
	// 監視情報表示スクロール制御
	private ScrollView mScrollLog;

	// 監視開始ボタン
	private Button mButtonStartWatch;
	// 監視終了ボタン
	private Button mButtonStopWatch;

	// 監視情報更新用処理ハンドラ
	private Handler mHandlerLogText = new Handler();

	// 情報記録用
	private SharedPreferences mSharedPrefer;

	// BroadCastレジースト用マネジャー
	private LocalBroadcastManager mLocalBroadcastManager;
	// callback用MsgReceiver
	private MsgReceiver mMsgReceiver;
	// 通知表示マネジャー対象
	private NotificationManager mNotificationManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_01_main, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mEditTextPackage = (EditText) getActivity()
				.findViewById(R.id.edtxt_pkg);

		mCheckBoxIsBootStart = (CheckBox) getActivity().findViewById(
				R.id.check_box_boot_start);
		mCheckBoxIsBootStart
				.setOnCheckedChangeListener(mCheckBoxIsBootStartListener);

		mCheckBoxAutoUnlock = (CheckBox) getActivity().findViewById(
				R.id.check_box_auto_unlock);
		mCheckBoxAutoUnlock
				.setOnCheckedChangeListener(mCheckBoxAutoUnlockListener);

		mCheckBoxNotice = (CheckBox) getActivity().findViewById(
				R.id.check_box_notice);
		mCheckBoxNotice.setOnCheckedChangeListener(mCheckBoxNoticeListener);

		mTextStatus = (TextView) getActivity().findViewById(R.id.text);
		mTextLog = (TextView) getActivity().findViewById(R.id.log);

		mScrollLog = (ScrollView) getActivity().findViewById(R.id.scroll_log);

		mButtonStartWatch = (Button) getActivity().findViewById(
				R.id.button_01_start_watch);

		mButtonStopWatch = (Button) getActivity().findViewById(
				R.id.button_02_stop_watch);

		mButtonStartWatch.setOnClickListener(mButtonStartWatchListener);
		mButtonStopWatch.setOnClickListener(mButtonStoptWatchListener);

		mSharedPrefer = getActivity().getSharedPreferences(
				Util.DEFAULT_SHARE_NAME, Activity.MODE_PRIVATE);

		boolean is_first_install = mSharedPrefer.getBoolean(
				Util.DEFAULT_SHARE_KEY_FIRST_INSTALL, true);

		// 初期登録、ディフォルト値設定
		if (is_first_install) {
			android.util.Log.d(TAG, "first install");
			SharedPreferences.Editor edit = mSharedPrefer.edit();
			edit.putString(Util.DEFAULT_SHARE_KEY_PKG_NAME, Util.PACKAGE_NAME);
			edit.putBoolean(Util.DEFAULT_SHARE_KEY_IS_WATCHING, false);
			edit.putBoolean(Util.DEFAULT_SHARE_KEY_FIRST_INSTALL, false);
			edit.putBoolean(Util.DEFAULT_SHARE_KEY_IS_BOOT_START, false);
			edit.putBoolean(Util.DEFAULT_SHARE_KEY_IS_AUTO_UNLOCK, true);
			edit.putBoolean(Util.DEFAULT_SHARE_KEY_IS_NOTICE_ENABLE, true);
			edit.commit();
		}

		if (mSharedPrefer.getBoolean(Util.DEFAULT_SHARE_KEY_IS_BOOT_START,
				false)) {
			mCheckBoxIsBootStart.setChecked(true);
		} else {
			mCheckBoxIsBootStart.setChecked(false);
		}

		if (mSharedPrefer.getBoolean(Util.DEFAULT_SHARE_KEY_IS_AUTO_UNLOCK,
				false)) {
			mCheckBoxAutoUnlock.setChecked(true);
		} else {
			mCheckBoxAutoUnlock.setChecked(false);
		}

		if (mSharedPrefer.getBoolean(Util.DEFAULT_SHARE_KEY_IS_NOTICE_ENABLE,
				false)) {
			mCheckBoxNotice.setChecked(true);
		} else {
			mCheckBoxNotice.setChecked(false);
		}

		mEditTextPackage.setText(mSharedPrefer.getString(
				Util.DEFAULT_SHARE_KEY_PKG_NAME, Util.PACKAGE_NAME));

		// 通知表示
		if (mSharedPrefer.getBoolean(Util.DEFAULT_SHARE_KEY_IS_NOTICE_ENABLE,
				false)) {
			createNotification();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();

		// receiverレジスト
		mLocalBroadcastManager = LocalBroadcastManager
				.getInstance(getActivity());
		mMsgReceiver = new MsgReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Util.BROADCAST_ACTION_LOG);
		mLocalBroadcastManager.registerReceiver(mMsgReceiver, intentFilter);

		if (mSharedPrefer.getBoolean(Util.DEFAULT_SHARE_KEY_IS_WATCHING, false)) {
			mTextStatus.setText(Util.TXT_START_WATCHING);

			// 監視開始
			Intent intent = new Intent(getActivity(), WatchService.class);
			getActivity().startService(intent);
		} else {
			mTextStatus.setText(Util.TXT_STOP_WATCHING);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mLocalBroadcastManager.unregisterReceiver(mMsgReceiver);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		// 監視停止状態WatchDog終了時、通知表示しない
		if ((mNotificationManager != null)
				&& (!mSharedPrefer.getBoolean(
						Util.DEFAULT_SHARE_KEY_IS_WATCHING, false))) {
			android.util.Log.d(TAG,
					"cancel notification when exit and not watching");
			mNotificationManager.cancelAll();
			mNotificationManager = null;
		}
		super.onDestroy();
	}

	OnClickListener mButtonStartWatchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// パッケージ存在判定
			String clsName = ProcessInfo
					.getClassNameByPkgName(getActivity().getPackageManager(),
							mEditTextPackage.getText().toString());

			android.util.Log.d(TAG, "clsName = " + clsName);

			if (clsName == null) {
				mTextStatus.setText(TXT_PKGNAME_CHECK);
				return;
			}

			// パッケージ名、監視かどうか情報セーブ
			SharedPreferences.Editor edit = mSharedPrefer.edit();
			edit.putString(Util.DEFAULT_SHARE_KEY_PKG_NAME, mEditTextPackage
					.getText().toString());
			edit.putBoolean(Util.DEFAULT_SHARE_KEY_IS_WATCHING, true);
			edit.commit();

			// android.util.Log.d(TAG,
			// "now share pkg_name = "+mSharedPrefer.getString(Util.DEFAULT_SHARE_KEY_PKG_NAME,
			// ""));
			// android.util.Log.d(TAG,
			// "now is_watching = "+mSharedPrefer.getBoolean(Util.DEFAULT_SHARE_KEY_IS_WATCHING,
			// false));

			// 監視開始
			Intent intent = new Intent(getActivity(), WatchService.class);
			getActivity().startService(intent);

			mTextStatus.setText(Util.TXT_START_WATCHING);
		}
	};

	OnClickListener mButtonStoptWatchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 監視無効情報セーブ
			SharedPreferences.Editor edit = mSharedPrefer.edit();
			edit.putBoolean(Util.DEFAULT_SHARE_KEY_IS_WATCHING, false);
			edit.commit();

			// 監視終了
			Intent intent = new Intent(getActivity(), WatchService.class);
			getActivity().stopService(intent);

			mTextStatus.setText(Util.TXT_STOP_WATCHING);

			if (mNotificationManager != null) {
				createNotification();
			}
		}
	};

	private OnCheckedChangeListener mCheckBoxIsBootStartListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			SharedPreferences.Editor edit = mSharedPrefer.edit();
			edit.putBoolean(Util.DEFAULT_SHARE_KEY_IS_BOOT_START, arg1);
			edit.commit();
		}
	};

	private OnCheckedChangeListener mCheckBoxAutoUnlockListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			SharedPreferences.Editor edit = mSharedPrefer.edit();
			edit.putBoolean(Util.DEFAULT_SHARE_KEY_IS_AUTO_UNLOCK, arg1);
			edit.commit();
		}
	};

	private OnCheckedChangeListener mCheckBoxNoticeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			SharedPreferences.Editor edit = mSharedPrefer.edit();
			edit.putBoolean(Util.DEFAULT_SHARE_KEY_IS_NOTICE_ENABLE, arg1);
			edit.commit();

			if (arg1) {
				// 通知表示
				createNotification();
			} else {
				// 　通知非表示
				android.util.Log.d(TAG, "cancel notification by checkbox");
				mNotificationManager.cancelAll();
				mNotificationManager = null;
			}
		}
	};

	// callback用MsgReceiver
	public class MsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 監視情報ログ更新
			String msg = intent.getStringExtra(Util.MSG_UPDATE_LOG);

			mTextLog.append("\n----------------------------------------");
			mTextLog.append("\n" + msg);
			mHandlerLogText.post(new Runnable() {
				@Override
				public void run() {
					mScrollLog.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
		}
	}

	// 通知表示
	private void createNotification() {
		mNotificationManager = (NotificationManager) getActivity()
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification.Builder builder = new Notification.Builder(getActivity());

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
				Util.WATCH_DOG_LAUNCHER_NAME);
		intent.setComponent(com_name);
		PendingIntent p_intent = PendingIntent.getActivity(getActivity()
				.getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(p_intent);

		// 　通知作成
		Notification n = builder.build();
		n.flags = Notification.FLAG_ONGOING_EVENT;
		mNotificationManager.notify(0, n);
	}
}
