package com.xiangma.bluetooth.le;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

/**
 * Description: <br/>
 * Program Name: Welcome displayµ{§Ç <br/>
 * Date: 2014-8-22
 * 
 * @author Shine.Hua hxylj4501@163.com
 * @version 2.15
 */

public class Welcome extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				startActivity(new Intent(Welcome.this, DeviceScanActivity.class));
				finish();
			}
		}, 2000);
	}
}
