package jp.co.newphoria.watchdog;

import jp.co.newphoria.watchdog.service.IWatchService;
import jp.co.newphoria.watchdog.service.WatchService;
import jp.co.newphoria.watchdog.service.WatchService.WatchServiceBinder;
import jp.co.newphoria.watchdog.util.Util;
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
import android.widget.ScrollView;
import android.widget.TextView;

public class Fragment01 extends Fragment {

	private final static String TAG = "Fragment01";

	private TextView mTextStatus;
	private TextView mTextLog;
	private ScrollView mScrollLog;

	private Button mButtonStartWatch;
	private Button mButtonStopWatch;

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
	public void onDestroyView() {
		getActivity().unbindService(mServiceConnection);
		super.onDestroyView();
	}

	OnClickListener mButtonStartWatchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mWatchServiceBinder.startWatch();
		}
	};

	OnClickListener mButtonStoptWatchListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mWatchServiceBinder.stopWatch();
		}
	};

	WatchServiceBinder mWatchServiceBinder;
	Service mWatchService;

	Intent mIntent;
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

	public IWatchService mIWatchService = new IWatchService() {
		@Override
		public void setStatusText(String s) {
			mTextStatus.setText(s);
		}

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

		@Override
		public void pullUpApp() {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			ComponentName com_name = new ComponentName(Util.PACKAGE_NAME,
					Util.CLASS_NAME);
			intent.setComponent(com_name);
			startActivity(intent);
		}
	};
}
