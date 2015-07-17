package jp.co.newphoria.watchdog.service;

/**
 * サービスcallback用インターフェース
 *
 * @author	Zhong Zhicong
 * @time	2015-7-17
 */
public interface IWatchService {
	//監視状態テキスト変更
	public void setStatusText(String s);
	//監視情報テキスト変更（結末追加）
	public void updateLogText(String log);
	//監視情報テキスト変更（結末追加）
	public void pullUpApp();
}
