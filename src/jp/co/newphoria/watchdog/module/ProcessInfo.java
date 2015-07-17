package jp.co.newphoria.watchdog.module;

import java.util.List;

import android.app.ActivityManager;

/**
 * プロセス情報管理
 *
 * @author	Zhong Zhicong
 * @time	2015-7-17
 */
public class ProcessInfo {
	private final static String TAG = "PackageInfo";

	/**
	 * プロセスリスト取得
	 * 
	 * @param act_mgr
	 *            ActivityManagerオブジェクト
	 * @return プロセスリスト
	 */
	public static List<ActivityManager.RunningAppProcessInfo> getAppProcessInfoList(
			ActivityManager act_mgr) {
		List<ActivityManager.RunningAppProcessInfo> appProcessList = act_mgr
				.getRunningAppProcesses();
		return appProcessList;
	}

	/**
	 * プロセスリスト出力
	 * 
	 * @param act_mgr
	 *            ActivityManagerオブジェクト
	 */
	public static void printProcessList(ActivityManager act_mgr) {
		List<ActivityManager.RunningAppProcessInfo> appProcessList = getAppProcessInfoList(act_mgr);
		for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
			android.util.Log.d(TAG, "pid = " + appProcessInfo.pid
					+ " p_name = " + appProcessInfo.processName);
		}
	}

	/**
	 * プロセスID取得
	 * 
	 * @param act_mgr
	 *            ActivityManagerオブジェクト
	 * @param p_name
	 *            パッケージ名，例「com.xxx.xxx」
	 * @return プロセスID
	 */
	public static int getPidByPName(ActivityManager act_mgr, String p_name) {
		int pid = -1;
		List<ActivityManager.RunningAppProcessInfo> appProcessList = getAppProcessInfoList(act_mgr);
		for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
			if (appProcessInfo.processName.equals(p_name)) {
				pid = appProcessInfo.pid;
				break;
			}
		}
		return pid;
	}
}