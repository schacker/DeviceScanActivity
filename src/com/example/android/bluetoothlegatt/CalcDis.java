package com.example.android.bluetoothlegatt;

import com.example.android.impl.CalcDisImpl;

/**
 * ͨ����ȡ��RSSI����������ģ�鵽�ƶ��ն˵ľ���
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
