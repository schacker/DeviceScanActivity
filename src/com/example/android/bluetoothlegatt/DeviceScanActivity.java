/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.ListActivity;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 负责扫描且列出可用的蓝牙设备,extends自ListActivity，自身不用setContentView,Android自身会构造出一个全屏的列表
 */
public class DeviceScanActivity extends ListActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter; //蓝牙适配器设备列表
    private BluetoothAdapter mBluetoothAdapter; //当前蓝牙适配器 
    private boolean mScanning; //是否处于扫描状态
    public HashMap<String,Integer> rssiMap;
    private CalcDis calcDis = new CalcDis();
    private static final int REQUEST_ENABLE_BT = 1;
    // 两秒钟后自动扫描，启动线程循环扫描
    private static final long SCAN_PERIOD = 2000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置title
        getActionBar().setTitle(R.string.title_devices); 
        // 判断设备是否支持低功耗蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // 获取蓝牙管理器
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // 判断是否支持蓝牙
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }
    /**
     * 初始化菜单调用，右上角菜单，ActionBar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	//设置菜单
        getMenuInflater().inflate(R.menu.main, menu);
        //设置菜单显示，是否处于扫描状态
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            //设置扫描时的进度条（旋转）
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }
    /**
     * 菜单被点击时调用，右上角菜单
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
			try {
				scanLeDevice(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
                break;
            case R.id.menu_stop:
			try {
				scanLeDevice(false);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * 判断主设备（手机）蓝牙是否开启或可用，如果不可用则提示用户，开启权限
         */
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        try {
			scanLeDevice(true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //设备进入休眠状态或跳转Activity
    @Override
    protected void onPause() {
        super.onPause();
        try {
			scanLeDevice(false);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        mLeDeviceListAdapter.clear();
    }
    //点击具体选项后，进入到DeviceControlActivity
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) 
        	return;
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        //将蓝牙设备名称、地址写入到DeviceControlActivity，有点像HashMap
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        //此时如果还在扫描，则停止扫描
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }
    /**
     * 扫描蓝牙设备
     * @param enable
     * @throws InterruptedException 
     */
    private void scanLeDevice(final boolean enable) throws InterruptedException {
        /*if (enable) {
        	rssiMap =  new HashMap<String, String>();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    System.out.println("---设备扫描中---");
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            //mScanning = true;
            //mBluetoothAdapter.startLeScan(mLeScanCallback);
        }*/ /*else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }*/
    	//用于存放不同蓝牙的RSSI值
    	final Handler handler = new Handler();
    	rssiMap = new HashMap<String, Integer>();
    	Runnable task = new Runnable() {
			@Override
			public void run() {
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
				handler.postDelayed(this, SCAN_PERIOD);
				Log.i("---延迟时间---", "延迟2秒执行线程");
				System.out.println("---设备扫描中---");
				mBluetoothAdapter.startLeScan(mLeScanCallback);
				//invalidateOptionsMenu();
			}
		};
		handler.post(task);
    	/*if(enable){
    		rssiMap =  new HashMap<String, Integer>();
    		handler.post(task);
    	}else{
    		mScanning = false;
    		if(null != task){
    			handler.removeCallbacks(task);
    			task.wait();
    		}
    		//invalidateOptionsMenu();
    	}*/
        invalidateOptionsMenu();
    }

    // 如果使用LIstView或者其子类，必须使用Adapter
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }
        /**
         * 清除
         */
        public void clear() {
            mLeDevices.clear();
        }
        
        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i); //获取到设备
            final String deviceName = device.getName(); //设备名字
            if (deviceName != null && deviceName.length() > 0){
            	viewHolder.deviceName.setText(deviceName);
            }
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            String address = device.getAddress();
            String dis = "暂无";
            if(!rssiMap.isEmpty()){
            	//dis = calcDis.dis(rssiMap.get(address));
            	Integer rx = calcDis.rx(rssiMap.get(address));
            	dis = calcDis.disO(rx);
            }
            viewHolder.deviceAddress.setText(address + " RSSI: "+ rssiMap.get(address) +" 距离："+ dis +"m");
            return view;
        }
    }

    // 设备扫描回调.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        	System.out.println("设备回调 DeviceScan RSSI:" + rssi);
        	rssiMap.put(device.getAddress(), (Integer)rssi);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}