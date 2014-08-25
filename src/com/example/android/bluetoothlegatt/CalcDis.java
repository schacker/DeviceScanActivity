package com.example.android.bluetoothlegatt;

import java.text.DecimalFormat;

import com.example.android.impl.CalcDisImpl;

/**
 * 通过获取得RSSI，计算蓝牙模块到移动终端的距离
 * @author schacker
 *
 */
public class CalcDis implements CalcDisImpl {
	
	/**
	 * 通过RSSI计算距离
	 */
	@Override
	public String dis(Integer rssi) {
		if(null == rssi)
			return "";
		DecimalFormat df = new DecimalFormat("0.00");
		return df.format(Math.pow(10, (Math.abs(rssi)-A)/B));
	}
	/**
	 * 过滤RSSI
	 */
	@Override
	public Integer gl(Integer rssi) {
		if(null == rssi)
			return 0;
		return (int) (u*rssi + (1-u)*A);
	}
	/**
	 * 通过RX求解距离
	 */
	@Override
	public String disO(Integer rx) {
		if(null == rx)
			return "";
		DecimalFormat df = new DecimalFormat("0.00");
		return df.format(Math.pow(10, (-A1-rx+G)/B1));
	}
	/**
	 * 求解RX
	 */
	@Override
	public Integer rx(Integer rssi) {
		if(rssi == null)
			return null;
		Integer rx = null;
		if(rssi > 0){
			rx = -40 + rssi;
		} else if(rssi == 0){
			rx = -45;
		} else if(rssi > -10 && rssi < 10){
			rx = -60 + rssi;
		} else {
			rx = rssi;
		}
		return rx;
	}
	
}
