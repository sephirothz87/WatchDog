package jp.co.newphoria.watchdog.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * グローバル定数とスタティック関数
 *
 * @author Zhong Zhicong
 * @time 2015-7-17
 * -----------------変更履歴-----------------
 * 日付			変更者				説明
 * 2015-7-22	Zhong Zhicong	Broadcastアクション名、SharedPreferences相関定数追加
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

	// ディフォルトパッケージ名
	public static final String PACKAGE_NAME = "jp.co.newphoria.signagedemo1";
	// ディフォルト起動クラス名
	public static final String CLASS_NAME = "jp.co.newphoria.signagedemo1.MainActivity";
	// 時間フォーマット
	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// 現在の時間取得、ログ表示用
	public static String getTime() {
		return DATE_FORMAT.format(new Date());
	}
}
