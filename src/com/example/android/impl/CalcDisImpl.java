package com.example.android.impl;

public interface CalcDisImpl {
	/**
	 * An�������Ϊ1mʱ��RSSIֵ 
	 */
	public final static double A = 62.0;
	public final static double A0 = 70.0;
	public final static double A1 = 40.2;
	public final static double G = 5.34;
	/**
	 * Bn����ָ����ĸ
	 */
	public final static double B = 45.0;
	public final static double B1 = 21.5; 
	/**
	 * RSSI����ϵ��
	 */
	public final static double u = 0.8;
	/**
	 * ����
	 * @param rssi
	 * @return �����ľ���
	 */
	public String dis(Integer rssi);
	/**
	 * ����RSSI
	 * @param rssi
	 * @return ���˺��RSSI
	 */
	public Integer gl(Integer rssi);
	/**
	 * ͨ��RX������
	 * @param rx
	 * @return
	 */
	public String disO(Integer rx);
	/**
	 * ���RX
	 * @param rssi
	 * @return
	 */
	public Integer rx(Integer rssi);
}
