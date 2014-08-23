package com.example.android.bluetoothlegatt;

import java.text.DecimalFormat;
import com.example.android.impl.CalcDisImpl;

/**
 * 通过获取得RSSI，计算蓝牙模块到移动终端的距离
 * @author schacker
 *
 */
public class CalcDis implements CalcDisImpl {

	@Override
	public String dis(Integer rssi) {
		if(null == rssi)
			return "";
		DecimalFormat df = new DecimalFormat("0.00");
		return df.format(Math.pow(10, (Math.abs(rssi)-A)/B));
		//return Math.pow(10, (Math.abs(rssi)-A)/B);
	}
}
