package jp.co.newphoria.watchdog;

import jp.co.newphoria.watchdog.service.IWatchService;
import jp.co.newphoria.watchdog.service.WatchService;
import jp.co.newphoria.watchdog.service.WatchService.WatchServiceBinder;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_01_main, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mIntent = new Intent(getActivity(), WatchService.class);
		getActivity().bindService(mIntent, mServiceConnection,
				Service.BIND_AUTO_CREATE);

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
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mWatchServiceBinder == null) {
			return;
		}

		if (mWatchServiceBinder.getWatchingStatue()) {
			mTextStatus.setText("監視中");
		}

		if (mWatchServiceBinder.getPackageName() != null) {
			mEditTextPackage.setText(mWatchServiceBinder.getPackageName());
		}
	}

	@Override
	public void onDestroy() {
		// アプリ終了時、監視サービスを解く
		getActivity().unbindService(mServiceConnection);
		super.onDestroy();
	}

	OnClickListener mButtonStartWatchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 監視開始
			mWatchServiceBinder.startWatch();
		}
	};

	OnClickListener mButtonStoptWatchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 監視終了
			mWatchServiceBinder.stopWatch();
		}
	};

	// 監視サービスバンダー
	WatchServiceBinder mWatchServiceBinder;
	// 監視サービス
	Service mWatchService;
	// 監視サービス起動インテント
	Intent mIntent;
	// 監視サービス連結
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mWatchServiceBinder = (WatchServiceBinder) service;
			mWatchService = mWatchServiceBinder.getService();
			mWatchServiceBinder.setInterface(mIWatchService);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mWatchService = null;
			mWatchServiceBinder = null;
		}
	};

	// サービスcallback用インターフェース
	public IWatchService mIWatchService = new IWatchService() {
		// 監視アプリパッケージネーム取得
		@Override
		public String getTestPackageName() {
			return mEditTextPackage.getText().toString();
		}

		// 監視状態テキスト変更
		@Override
		public void setStatusText(String s) {
			mTextStatus.setText(s);
		}

		// 監視情報テキスト変更（結末追加）
		@Override
		public void updateLogText(String log) {
			mTextLog.append("\n" + log);
			android.util.Log.d(TAG, log);
			mHandlerLogText.post(new Runnable() {
				@Override
				public void run() {
					mScrollLog.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
		}
	};
}
