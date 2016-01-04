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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangma.dbmanager.DBH;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity implements OnClickListener{
	private final static String TAG = DeviceControlActivity.class
			.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	// Layout Views
	//private ListView mConversationView;
	//private Button mSendButton;

	private TextView mConnectionState;
	//private TextView tempTextView;
	private TextView DevConStatusTextView;
	private TextView DevPowerTextView;
	private TextView HealthStatusTextView;
	private TextView TempTextView;
	private TextView PulseTextView;
	private TextView battery_status;//电池电量
	private ImageView LightOffButton;
	private ImageView SettingButton;
	private ImageView HistoryButton;
	private ImageView CallhelpButton;
	private String mDeviceName;
	private String mDeviceAddress;
	private BluetoothLeService mBluetoothLeService;//ble服务函数，用于处理得到的数据包
	private int mRecvDataCount;
	DBH db;
	private Calendar c;
	private String name;
	Date date;
	DateFormat df ;
	private int count=0;
	SmsManager smsManager;
	private String phone_one,phone_two,phone_three;
	
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
					 c = Calendar.getInstance();
					 int second = c.get(Calendar.SECOND);
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
								 if(second==10){
									 date=new Date();
									 df = new SimpleDateFormat("yyyy/MM/dd HH：mm：ss");
									 db.insert(name, Messages[1], Messages[0], df.format(date));
									 if(Float.parseFloat(Messages[1])>42||Float.parseFloat(Messages[1])<25){
										 
										 count++;
									 }
									 
									 if(count==10){
										String content="";
										 smsManager=SmsManager.getDefault();
										 smsManager.sendTextMessage(phone_one,null, content, null, null);
										 smsManager.sendTextMessage(phone_two,null, content, null, null);
										 smsManager.sendTextMessage(phone_three,null, content, null, null);
									 }
								 }
								
								
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
		setContentView(R.layout.activity_control);
		SharedPreferences sharedPreferencesVip = getSharedPreferences("VipInfo", Activity.MODE_PRIVATE);
		name=sharedPreferencesVip.getString("user_name", "");
		phone_one=sharedPreferencesVip.getString("user_connectone", "");
		phone_two=sharedPreferencesVip.getString("user_connecttwo", "");
		phone_three=sharedPreferencesVip.getString("user_connectthree", "");
		db=new DBH(this);
		 Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
		
		// Sets up UI references.
		TempTextView=(TextView)findViewById(R.id.temperature);
		PulseTextView=(TextView)findViewById(R.id.pulse);
		battery_status=(TextView)findViewById(R.id.battery_status);
		mConnectionState=(TextView)findViewById(R.id.status);
		CallhelpButton=(ImageView)findViewById(R.id.call_help);
		SettingButton=(ImageView)findViewById(R.id.setting);
		HistoryButton=(ImageView)findViewById(R.id.datalist);
		HistoryButton.setOnClickListener(this);
		CallhelpButton.setOnClickListener(this);
		SettingButton.setOnClickListener(this);
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
		LightOffButton = (ImageView) findViewById(R.id.light_off);
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
			
			mConnectionState.setText("已连接");
			
		} else {
			mConnectionState.setText("未连接");
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

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.datalist:
			
			Intent intent=new Intent();
			intent.setClass(this, DataCollectionActivity.class);
			startActivity(intent);
			break;
			
		case R.id.setting:

			Intent intent_set=new Intent();
			intent_set.setClass(this, MyCenterActivtiy.class);
			startActivity(intent_set);
			break;

		default:
			break;
		}
	}
}
