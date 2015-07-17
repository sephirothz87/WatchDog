package jp.co.newphoria.watchdog.service;

public interface IWatchService {
	public void setStatusText(String s);

	public void updateLogText(String log);

	public void pullUpApp();
}
