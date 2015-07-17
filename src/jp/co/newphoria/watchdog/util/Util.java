package jp.co.newphoria.watchdog.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * グローバル定数とスタティック関数
 *
 * @author	Zhong Zhicong
 * @time	2015-7-17
 */
public class Util {
	//共通ログタッグ
	public static final String COMMON_TAG = "WatchLogApp";
	//ディフォルトパッケージ名
	public static final String PACKAGE_NAME = "jp.co.newphoria.signagedemo1";
	//ディフォルト起動クラス名
	public static final String CLASS_NAME = "jp.co.newphoria.signagedemo1.MainActivity";
	//時間フォーマット
	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	//現在の時間取得、ログ表示用
	public static String getTime() {
		return DATE_FORMAT.format(new Date());
	}
}
