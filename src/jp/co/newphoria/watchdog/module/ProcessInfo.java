package jp.co.newphoria.watchdog.module;

import java.util.List;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

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
	
	public static String getClassNameByPkgName(PackageManager pkgManager,String pkgName){
		String clsName=null;
		
		// 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等  
	    PackageInfo packageinfo = null;  
	    try {  
	        packageinfo = pkgManager.getPackageInfo(pkgName, 0);  
	    } catch (NameNotFoundException e) {  
	        e.printStackTrace();
	    }
	    
	    if (packageinfo == null) {  
	        return null;
	    }
	  
	    // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent  
	    Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);  
	    resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);  
	    resolveIntent.setPackage(packageinfo.packageName);
	  
	    // 通过getPackageManager()的queryIntentActivities方法遍历  
	    List<ResolveInfo> resolveinfoList = pkgManager.queryIntentActivities(resolveIntent, 0);  
	  
	    ResolveInfo resolveinfo = resolveinfoList.iterator().next();  
	    if (resolveinfo != null) {  
	        clsName = resolveinfo.activityInfo.name;
	    }  
		
		return clsName;
	}
}