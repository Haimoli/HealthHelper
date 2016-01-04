package com.xiangma.bluetooth.le;

import java.util.Timer;
import java.util.TimerTask;

import com.xiangma.baseclass.BaseActivity;
import com.xiangma.dbmanager.DBH;
import com.xingma.setting.SettingActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * Description: <br/>
 * Program Name: Welcome display祘 <br/>
 * Date: 2014-8-22
 * 
 * @author Shine.Hua hxylj4501@163.com
 * @version 2.15
 */

public class Welcome extends BaseActivity {

	DBH dbh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		dbh = new DBH(this);
		dbh.dbhInit();// 初始化，创建数据库
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				SharedPreferences sharedPreferencesVip = getSharedPreferences("VipInfo", Activity.MODE_PRIVATE);
				String flag = sharedPreferencesVip.getString("flag", "");
				if (flag.equals("1")) {
					
					startActivity(new Intent(Welcome.this, DeviceScanActivity.class));
				} else {

					startActivity(new Intent(Welcome.this, SettingActivity.class));

				}
				finish();
			}
		}, 2000);
	}
}
