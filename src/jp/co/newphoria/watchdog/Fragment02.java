package jp.co.newphoria.watchdog;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 機能拡張用fragment
 *
 * @author Zhong Zhicong
 * @time 2015-7-17
 */
public class Fragment02 extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_02, null);
	}

}
