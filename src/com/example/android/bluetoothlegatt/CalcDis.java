package com.example.android.bluetoothlegatt;

import java.text.DecimalFormat;
import com.example.android.impl.CalcDisImpl;

/**
 * ͨ����ȡ��RSSI����������ģ�鵽�ƶ��ն˵ľ���
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
