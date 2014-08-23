package com.example.android.bluetoothlegatt;

import com.example.android.impl.CalcDisImpl;

/**
 * 通过获取得RSSI，计算蓝牙模块到移动终端的距离
 * @author schacker
 *
 */
public class CalcDis implements CalcDisImpl {

	@Override
	public double dis(Integer rssi) {
		if(null == rssi)
			return -1;
		return Math.pow(10, (Math.abs(rssi)-A)/B);
	}
}
