package com.zkc.Receiver;

import java.sql.Date;

import com.zkc.Service.CaptureService;
import com.zkc.Service.SerialPortClass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.renderscript.Element;
import android.util.Log;
import android.widget.Toast;

/**
 * 开机启动按键广播监听
 * 
 * @author zkc-soft2
 * 
 */
public class RemoteControlReceiver extends BroadcastReceiver {

	private static final String TAG = "RemoteControlReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// 广播接收
		String action = intent.getAction();
		Log.i(TAG, "System message " + action);
		if (action.equals("com.zkc.keycode")) {
			if (StartReceiver.times++ > 0) {
				StartReceiver.times = 0;
				int keyValue = intent.getIntExtra("keyvalue", 0);
				Log.i(TAG, "KEY VALUE:"+keyValue);
				if (keyValue == 136 || keyValue == 135 || keyValue == 131) {
					Log.i(TAG, "Scan key down.........");
					CaptureService.barCodeStr = "";
					CaptureService.scanGpio.openScan();
				}
			}
		} else if (action.equals("android.intent.action.SCREEN_ON")) {
			Log.i(TAG, "Power off,Close scan modules power.........");
			CaptureService.serialPort = new SerialPortClass(
					CaptureService.choosed_serial, CaptureService.choosed_buad);
			CaptureService.scanGpio.openPower();
		} else if (action.equals("android.intent.action.SCREEN_OFF")) {
			Log.i(TAG, "ACTION_SCREEN_OFF,Close scan modules power.........");
			CaptureService.serialPort.CloseDevice();
			CaptureService.serialPort = new SerialPortClass(
					CaptureService.choosed_serial, 115200);
			CaptureService.serialPort.CloseDevice();
			CaptureService.scanGpio.closePower();
			CaptureService.scanGpio.closeScan();
		} else if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
			Log.i(TAG, "ACTION_SCREEN_ON,Open scan modules power.........");
			CaptureService.serialPort.CloseDevice();
			CaptureService.serialPort = new SerialPortClass(
					CaptureService.choosed_serial, 115200);
			CaptureService.serialPort.CloseDevice();
			CaptureService.scanGpio.closePower();
			CaptureService.scanGpio.closeScan();
		}
	}
}