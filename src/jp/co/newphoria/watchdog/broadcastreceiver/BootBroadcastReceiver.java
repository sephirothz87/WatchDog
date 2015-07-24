package jp.co.newphoria.watchdog.broadcastreceiver;

import jp.co.newphoria.watchdog.service.WatchService;
import jp.co.newphoria.watchdog.util.Util;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * OS起動監視用BroadcastReceiver
 *
 * @author Zhong Zhicong
 * @time 2015-7-24
 * ---------------------------------変更履歴---------------------------------
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
	private final String TAG = "BootBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		android.util.Log.d(TAG, "onReceive");
		Util.writeLog("onReceive");

		SharedPreferences share_pref = context.getSharedPreferences("option",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor edit = share_pref.edit();
		if (share_pref.getBoolean("IS_BOOT_START", false)) {
			android.util.Log.d(TAG, "boot start");
			Util.writeLog("boot start");
			// 監視かどうか情報セーブ
			edit.putBoolean("IS_WATCHING", true);
			edit.commit();
			Intent i = new Intent(context, WatchService.class);
			context.startService(i);
		} else {
			android.util.Log.d(TAG, "no need boot start");
			Util.writeLog("no need boot start");
			// 監視無効情報セーブ
			edit.putBoolean("IS_WATCHING", false);
			edit.commit();
		}
	}
}
