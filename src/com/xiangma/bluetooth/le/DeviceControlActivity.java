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

package com.xiangma.bluetooth.le;

import java.util.ArrayList;

import javax.security.auth.PrivateCredentialPermission;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
	private final static String TAG = DeviceControlActivity.class
			.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	// Layout Views
	//private ListView mConversationView;
	//private Button mSendButton;

	//private TextView mConnectionState;
	//private TextView tempTextView;
	private TextView DevConStatusTextView;
	private TextView DevPowerTextView;
	private TextView HealthStatusTextView;
	private TextView TempTextView;
	private TextView PulseTextView;
	private Button LightOffButton;
	private Button SettingButton;
	private Button HistoryButton;
	private Button CallhelpButton;
	private String mDeviceName;
	private String mDeviceAddress;
	private BluetoothLeService mBluetoothLeService;//ble服务函数，用于处理得到的数据包
	private int mRecvDataCount;
	
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;

	// Code to manage Service lifecycle.用于监控应用服务状态
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(mDeviceAddress);
			//System.out.println("ddddddddddddddddd");
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	//广播接收者
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				updateConnectionState(R.string.connected);
				invalidateOptionsMenu();
				mRecvDataCount = 0;
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				updateConnectionState(R.string.disconnected);
				invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				ArrayList<byte[]> arrayList = mBluetoothLeService
						.getRecvedData();//得到数据list集合
				
				//int i = 0;
				//while ((i++<10) && (arrayList.size() > 0)) {//之前的代码
				while ( arrayList.size() > 0) {
					byte[] data = arrayList.remove(0);//删除集合中第一个数据，即第一个存入的数据，并且将返回要删除的Byte[]。
					String mData=new String(data);
					if (data!= null) {
						final StringBuilder stringBuilder = new StringBuilder(
								data.length);
						for (byte byteChar : data)
							stringBuilder.append(String.format("%02X ",
									byteChar));
						
						String llString=new String(data);
						if(llString.length()>9)
						{
							try
							{
								
								String dateNewString=llString.substring(0, 8);
								String[] Messages=dateNewString.split(",");
								TempTextView.setText(Messages[1]);
								PulseTextView.setText(Messages[0]);
								System.out.println("数据集合"+new String(dateNewString));
								
							} catch (Exception e)
							{
								// TODO: handle exception
							}
							
						}
						
						//向listview中添加数据，去掉ListView
						//mConversationArrayAdapter.add(mDeviceName + ":  "
								//+ new String(data) + "\n");
								//+ stringBuilder.toString());
						mRecvDataCount += data.length;
					}
				}
//				Log.i(TAG, "Received Count: " + mRecvDataCount);
			}
		}
	};

	private void clearUI() {
		mConversationArrayAdapter.clear();
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	//发送数据，点击发送按钮，执行该方法
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mBluetoothLeService.getState() != BluetoothLeService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			// if(!message.contentEquals("$!CMDMODE!$"))
			// message += "\r\n";

			byte[] data = message.getBytes();
			mBluetoothLeService.write(data);

			final StringBuilder stringBuilder = new StringBuilder(data.length);
			for (byte byteChar : data)
				stringBuilder.append(String.format("%02X ", byteChar));
			mConversationArrayAdapter.add("Me:  " + new String(data) + "\n"
					+ stringBuilder.toString());

			// Reset out string buffer to zero and clear the edit text field
			// mOutStringBuffer.setLength(0);
			// mOutEditText.setText(mOutStringBuffer);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_layout);

		 Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
		
		// Sets up UI references.
		TempTextView=(TextView)findViewById(R.id.temperature);
		PulseTextView=(TextView)findViewById(R.id.pulse);
		// Initialize the array adapter for the conversation thread
		//mConversationArrayAdapter = new ArrayAdapter<String>(this,
				//R.layout.message);//修改
		//mConversationView = (ListView) findViewById(R.id.in);
		//mConversationView.setAdapter(mConversationArrayAdapter);

		//mConnectionState = (TextView) findViewById(R.id.connection_state);
		DevConStatusTextView = (TextView) findViewById(R.id.status);
		
		//tempTextView = (TextView) findViewById(R.id.);

		// Initialize the send button with a listener that for click events
		//mSendButton = (Button) findViewById(R.id.button_send);
		LightOffButton = (Button) findViewById(R.id.light_off);
		LightOffButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				//TextView view = (TextView) findViewById(R.id.edit_text_out);
				String message = "1";
                message = message.replaceAll("\n", "\r\n");
				sendMessage(message);
			}
		});

		//getActionBar().setTitle(mDeviceName);
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if ((mBluetoothLeService != null)
				&& (mBluetoothLeService.getState() == BluetoothLeService.STATE_CONNECTED)) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			mBluetoothLeService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
			mBluetoothLeService.disconnect();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				DevConStatusTextView.setText(resourceId);
			}
		});
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}
}
