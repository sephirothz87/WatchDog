package jp.co.newphoria.watchdog;

import jp.co.newphoria.watchdog.module.ProcessInfo;
import jp.co.newphoria.watchdog.service.WatchService;
import jp.co.newphoria.watchdog.util.Util;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * アプリの主fragment
 *
 * @author Zhong Zhicong
 * @time 2015-7-17
 * -----------------変更履歴-----------------
 * 日付			変更者				説明
 * 2015-7-22	Zhong Zhicong	サービス起動方式、bind→startになる。callback方式は、broadcastで実現
 * 2015-7-22	Zhong Zhicong	監視対象パッケージ名、監視かどうか情報をSharedPreferencedに保存
 */
public class Fragment01 extends Fragment {
	// ログタッグ
	private final static String TAG = "Fragment01";

	// 監視したいパッケージ入力枠
	private EditText mEditTextPackage;
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
				Util.DEFAULT_SHARE_KEY_FIRST_INSTALL, false);

		// 初期登録、ディフォルト値設定
		if (is_first_install) {
			SharedPreferences.Editor edit = mSharedPrefer.edit();
			edit.putString(Util.DEFAULT_SHARE_KEY_PKG_NAME, Util.PACKAGE_NAME);
			edit.putBoolean(Util.DEFAULT_SHARE_KEY_IS_WATCHING, false);
			edit.putBoolean(Util.DEFAULT_SHARE_KEY_FIRST_INSTALL, false);
			edit.commit();
		}

		mEditTextPackage.setText(mSharedPrefer.getString(
				Util.DEFAULT_SHARE_KEY_PKG_NAME, Util.PACKAGE_NAME));

		// receiverレジスト
		mMsgReceiver = new MsgReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Util.BROADCAST_ACTION_LOG);
		getActivity().registerReceiver(mMsgReceiver, intentFilter);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mSharedPrefer.getBoolean(Util.DEFAULT_SHARE_KEY_IS_WATCHING, false)) {
			mTextStatus.setText("監視中");
		} else {
			mTextStatus.setText("監視停止");
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		// アクティビティ終了
		getActivity().unregisterReceiver(mMsgReceiver);
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
				mTextStatus.setText("パッケージなし、チェックし再入力してください");
				return;
			}

			// パッケージ名、監視かどうか情報セーブ
			SharedPreferences.Editor edit = mSharedPrefer.edit();
			edit.putString(Util.DEFAULT_SHARE_KEY_PKG_NAME, mEditTextPackage
					.getText().toString());
			if (!mSharedPrefer.getBoolean(Util.DEFAULT_SHARE_KEY_IS_WATCHING,
					false)) {
				// android.util.Log.d(TAG,
				// "now is_watching = "+mSharedPrefer.getBoolean(Util.DEFAULT_SHARE_KEY_IS_WATCHING,
				// false));
				edit.putBoolean(Util.DEFAULT_SHARE_KEY_IS_WATCHING, true);
			}
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

			mTextStatus.setText("監視中");
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

			mTextStatus.setText("監視停止");
		}
	};

	// callback用MsgReceiver
	private MsgReceiver mMsgReceiver;

	public class MsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 監視情報ログ更新
			String msg = intent.getStringExtra("msg");

			mTextLog.append("\n" + msg);
			android.util.Log.d(TAG, msg);
			mHandlerLogText.post(new Runnable() {
				@Override
				public void run() {
					mScrollLog.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
		}
	}
}
