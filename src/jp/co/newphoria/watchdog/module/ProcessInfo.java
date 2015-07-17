package jp.co.newphoria.watchdog.module;

import java.util.List;

import android.app.ActivityManager;

public class ProcessInfo {
	private final static String TAG = "PackageInfo";

	/**
	 * 取得进程列表
	 * 
	 * @param act_mgr
	 *            ActivityManager对象，在Activity内获取：ActivityManager act_mgr =
	 *            (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	 * @return 进程列表
	 */
	public static List<ActivityManager.RunningAppProcessInfo> getAppProcessInfoList(
			ActivityManager act_mgr) {
		List<ActivityManager.RunningAppProcessInfo> appProcessList = act_mgr
				.getRunningAppProcesses();
		return appProcessList;
	}

	/**
	 * 打印进程名称和进程id列表
	 * 
	 * @param act_mgr
	 *            ActivityManager对象，在Activity内获取：ActivityManager act_mgr =
	 *            (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	 */
	public static void printProcessList(ActivityManager act_mgr) {
		List<ActivityManager.RunningAppProcessInfo> appProcessList = getAppProcessInfoList(act_mgr);
		for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
			android.util.Log.d(TAG, "pid = " + appProcessInfo.pid
					+ " p_name = " + appProcessInfo.processName);
		}
	}

	/**
	 * 根据指定进程名获得pid
	 * 
	 * @param act_mgr
	 *            ActivityManager对象，在Activity内获取：ActivityManager act_mgr =
	 *            (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	 * @param p_name
	 *            进程名，如"com.tencent.qqlive"
	 * @return 进程id,pid
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

	/**
	 * 根据指定进程名获得uid
	 * 
	 * @param act_mgr
	 *            ActivityManager对象，在Activity内获取：ActivityManager act_mgr =
	 *            (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	 * @param p_name
	 *            进程名，如"com.tencent.qqlive"
	 * @return 用户id，uid
	 */
	public static int getUidByPName(ActivityManager act_mgr, String p_name) {
		int uid = -1;
		List<ActivityManager.RunningAppProcessInfo> appProcessList = getAppProcessInfoList(act_mgr);
		for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
			if (appProcessInfo.processName.equals(p_name)) {
				uid = appProcessInfo.uid;
				break;
			}
		}
		return uid;
	}
}
