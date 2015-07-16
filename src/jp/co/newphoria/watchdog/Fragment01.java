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

	private TextView mTextStatus;
	private TextView mTextLog;
	private ScrollView mScrollLog;

	private Button mButtonStartWatch;
	private Button mButtonStopWatch;

	private Handler mHandlerLogText = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// comment
		return inflater.inflate(R.layout.fragment_01_main, null);
	}
}
