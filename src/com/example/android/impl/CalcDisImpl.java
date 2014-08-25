package com.example.android.impl;

public interface CalcDisImpl {
	/**
	 * An代表距离为1m时，RSSI值 
	 */
	public final static double A = 62.0;
	public final static double A0 = 70.0;
	public final static double A1 = 40.2;
	public final static double G = 5.34;
	/**
	 * Bn代表指数分母
	 */
	public final static double B = 45.0;
	public final static double B1 = 21.5; 
	/**
	 * RSSI过滤系数
	 */
	public final static double u = 0.8;
	/**
	 * 计算
	 * @param rssi
	 * @return 计算后的距离
	 */
	public String dis(Integer rssi);
	/**
	 * 过滤RSSI
	 * @param rssi
	 * @return 过滤后的RSSI
	 */
	public Integer gl(Integer rssi);
	/**
	 * 通过RX求解距离
	 * @param rx
	 * @return
	 */
	public String disO(Integer rx);
	/**
	 * 求解RX
	 * @param rssi
	 * @return
	 */
	public Integer rx(Integer rssi);
}
