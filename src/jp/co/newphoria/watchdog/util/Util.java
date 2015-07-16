package jp.co.newphoria.watchdog.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
	public static final String COMMON_TAG="WatchLogApp";
	public static final String PACKAGE_NAME="jp.co.newphoria.signagedemo1";
	public static final String CLASS_NAME="jp.co.newphoria.signagedemo1.MainActivity";
	public final static SimpleDateFormat DATE_FORMAT=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String getTime() {
		return DATE_FORMAT.format(new Date());
	}
}
