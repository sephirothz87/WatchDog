package jp.co.newphoria.watchdog.util;

import android.annotation.SuppressLint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * グローバル定数とスタティック関数
 *
 * @author Zhong Zhicong
 * @time 2015-7-17
 * ---------------------------------変更履歴---------------------------------
 * 日付 			変更者 			説明 
 * 2015-7-22	Zhong Zhicong 	Broadcastアクション名、SharedPreferences相関定数追加
 * 2015-7-24	Zhong Zhicong 	OS起動する時自動的に監視かどうかSharedPreferencesケー追加
 */
public class Util {
	// 共通ログタッグ
	public static final String COMMON_TAG = "WatchLogApp";

	// Broadcastアクション
	public static final String BROADCAST_ACTION_LOG = "jp.co.newphoria.RECEIVER_LOG";

	// SharedPreferences名
	public static final String DEFAULT_SHARE_NAME = "option";
	// SharedPreferencesキー、新規登録フラッグ
	public static final String DEFAULT_SHARE_KEY_FIRST_INSTALL = "FIRST_INSTALL";
	// SharedPreferencesキー、監視対象パッケージ名
	public static final String DEFAULT_SHARE_KEY_PKG_NAME = "PACKAGE_NAME";
	// SharedPreferencesキー、監視かどうか
	public static final String DEFAULT_SHARE_KEY_IS_WATCHING = "IS_WATCHING";
	// SharedPreferencesキー、OS起動時に自動てきに監視するかどうか
	public static final String DEFAULT_SHARE_KEY_IS_BOOT_START = "IS_BOOT_START";

	// ディフォルトパッケージ名
	public static final String PACKAGE_NAME = "jp.co.newphoria.signagedemo1";
	// ディフォルト起動クラス名
	public static final String CLASS_NAME = "jp.co.newphoria.signagedemo1.MainActivity";
	// WatchDog起動クラス名
	public static final String WATCH_DOG_LANCHER_NAME = "jp.co.newphoria.watchdog.MainActivity";
	// 時間フォーマット
	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// 監視ログ更新用IntentExtra名
	public final static String MSG_UPDATE_LOG = "updateLog";

	// 現在の時間取得、ログ表示用
	public static String getTime() {
		return DATE_FORMAT.format(new Date());
	}

	// 　DEBUGフラッグ
	private static final boolean DEBUG = false;

	// DEBUG用、ローカルファイルにログ記入
	@SuppressLint("SdCardPath")
	public static void writeLog(String s) {
		if (DEBUG) {
			String path = "/mnt/sdcard/watchdog/log.txt";
			createNewFile(path);
			writeLine(path, "[" + Util.getTime() + "]" + s);
		}
	}

	public static File createNewFile(String filePath) {
		File f = new File(filePath);
		if (!f.exists()) {
			f.getParentFile().mkdirs();
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			return f;
		}
		return null;
	}

	public static boolean writeLine(String filePath, String str) {
		return writeLine(filePath, str, "utf-8");
	}

	public static boolean writeLine(String filePath, String str, String encode) {
		try {
			FileOutputStream fos = new FileOutputStream(filePath, true);
			String s = str + System.getProperty("line.separator");
			fos.write(s.getBytes(encode));
			fos.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
