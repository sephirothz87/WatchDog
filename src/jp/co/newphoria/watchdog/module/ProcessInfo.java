package jp.co.newphoria.watchdog.module;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

/**
 * プロセス情報管理
 *
 * @author Zhong Zhicong
 * @time 2015-7-17
 * -----------------変更履歴-----------------
 * 日付			変更者				説明
 * 2015-7-22	Zhong Zhicong	起動クラス名検察結果ないのログ追加
 * 2015-7-30	Zhong Zhicong	RunningAppProcessInfo対象取得メソッド追加
 * 2015-7-30	Zhong Zhicong	最前のプロセス名取得メソッド追加
 * 2015-7-30	Zhong Zhicong	最前のプロセスかどうか判定メソッド追加
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
	 * RunningAppProcessInfo対象取得
	 * 
	 * @param act_mgr
	 *            ActivityManagerオブジェクト
	 * @param p_name
	 *            パッケージ名，例「com.xxx.xxx」
	 * @return RunningAppProcessInfo対象
	 */
	public static RunningAppProcessInfo getProcessInfoByPName(
			ActivityManager act_mgr, String p_name) {
		List<ActivityManager.RunningAppProcessInfo> appProcessList = getAppProcessInfoList(act_mgr);
		for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
			if (appProcessInfo.processName.equals(p_name)) {
				return appProcessInfo;
			}
		}
		return null;
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

	/**
	 * 最前のプロセス名取得
	 * 
	 * @param act_mgr
	 *            ActivityManagerオブジェクト
	 * @return プロセス名
	 */
	public static String getTopProcess(ActivityManager act_mgr) {
		String res = "";
		List<ActivityManager.RunningAppProcessInfo> appProcessList = getAppProcessInfoList(act_mgr);
		for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
			if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				res = appProcessInfo.processName;
				return res;
			}
		}
		return res;
	}

	/**
	 * 最前のプロセスかどうか判定
	 * 
	 * @param act_mgr
	 *            ActivityManagerオブジェクト
	 * @param info
	 *            RunningAppProcessInfo対象
	 * @return true　最前プロセス　false　最前プロセスでばない
	 */
	public static boolean isTopProcess(ActivityManager act_mgr,
			RunningAppProcessInfo info) {
		if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 最前のプロセスかどうか判定
	 * 
	 * @param act_mgr
	 *            ActivityManagerオブジェクト
	 * @param p_name
	 *            パッケージ名，例「com.xxx.xxx」
	 * @return true　最前プロセス　false　最前プロセスでばない
	 */
	public static boolean isTopProcess(ActivityManager act_mgr, String p_name) {

		List<ActivityManager.RunningAppProcessInfo> appProcessList = getAppProcessInfoList(act_mgr);
		for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
			if (appProcessInfo.processName.equals(p_name)) {
				if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * 起動クラス名取得
	 * 
	 * @param pkgManager
	 *            パッケージ管理機（アクティビティ、サービス中getPackageManager()メソッドで取得）
	 * @param pkgName
	 *            パッケージ名
	 * @return　該当アプリの起動クラス名
	 */
	public static String getClassNameByPkgName(PackageManager pkgManager,
			String pkgName) {
		String clsName = null;

		PackageInfo packageinfo = null;
		try {
			packageinfo = pkgManager.getPackageInfo(pkgName, 0);
		} catch (NameNotFoundException e) {
			android.util.Log.d(TAG, "Name Not Found");
			e.printStackTrace();
		}

		if (packageinfo == null) {
			return null;
		}

		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageinfo.packageName);

		List<ResolveInfo> resolveinfoList = pkgManager.queryIntentActivities(
				resolveIntent, 0);

		ResolveInfo resolveinfo = resolveinfoList.iterator().next();

		if (resolveinfo != null) {
			clsName = resolveinfo.activityInfo.name;
		}

		return clsName;
	}
}