package com.zkc.Service;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;

import com.zkc.Receiver.RemoteControlReceiver;
import com.zkc.beep.ServiceBeepManager;
import com.zkc.pc700.helper.ScanGpio;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class CaptureService extends Service {
	public static SharedPreferences sharedPreferences = null;
	public static String str_encoding = "UTF-8";

	static Context context;
	private static final String TAG = "CaptureService";
	public static ServiceBeepManager beepManager;// ɨ��ɹ���ʾ��

	// ��Ļ״̬�㲥
	RemoteControlReceiver screenStatusReceiver;

	/*
	 * һάͷ�ָ���������
	 */
	public static byte[] defaultSetting1D = new byte[] { 0x04, (byte) 0xC8,
			0x04, 0x00, (byte) 0xFF, 0x30 };// ��������

	/*
	 * һάͷ���ݸ�ʽ
	 */
	public static byte[] dataTypeFor1D = new byte[] { 0x07, (byte) 0xC6, 0x04,
			0x08, 0x00, (byte) 0xEB, 0x07, (byte) 0xFE, 0x35 };// ��ȡ���ݸ�ʽ

	/*
	 * ��άͷ�ָ���������
	 */
	public static byte[] defaultSetting2D = new byte[] { 0x16, 0x4D, 0x0D,
			0x25, 0x25, 0x25, 0x44, 0x45, 0x46, 0x2E };

	/*
	 * ��άͷ���ݸ�ʽ
	 */
	public static byte[] dataTypeFor2D = new byte[] { 0x16, 0x4D, 0x0D, 0x38,
			0x32, 0x30, 0x32, 0x44, 0x30, 0x31, 0x2E };

	public static SerialPortClass serialPort = null;

	public static String choosed_serial = "/dev/ttyMT0";// ���ں�
	public static int choosed_buad = 9600;// ������
	public static ScanGpio scanGpio = new ScanGpio();

	public static String barCodeStr = "";
	public static byte[] barCodeHex = new byte[4096];
	public static int barCodeLen = 0;

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "CaptureService onBind");
		return null;
	}

	/**
	 * ��ʾ��ϢHandle
	 */
	public static Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			byte[] getdata = (byte[]) msg.obj;
			int size = msg.arg1;
			if (size < 2) {
				return;
			}
			System.arraycopy(getdata, 0, barCodeHex, barCodeLen, size);
			barCodeLen += size;
			try {
				barCodeStr += new String(getdata, 0, size, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (getdata[size - 1] == 13 || getdata[size - 1] == 10) {
				try {
					// ����ɨ��ɹ���ʾ��
					if (beepManager != null) {
						beepManager.playBeepSoundAndVibrate();
					}

					byte[] btdata = new byte[barCodeLen];
					System.arraycopy(barCodeHex, 0, btdata, 0, btdata.length);

					barCodeStr = new String(btdata, 0, btdata.length, str_encoding);
					barCodeStr = barCodeStr.trim();

					// ���͹㲥��Ϣ
					Intent intentBroadcast = new Intent();
					intentBroadcast.setAction("com.zkc.scancode");
					intentBroadcast.putExtra("code", barCodeStr);
					context.sendBroadcast(intentBroadcast);

					barCodeStr = "";
					barCodeHex = new byte[4096];
					barCodeLen = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {

			}
		}

	};

	/**
	 * ��byte����ת��Ϊ�ַ�����ʽ��ʾ��ʮ������������鿴
	 */
	public static StringBuffer bytesToString(byte[] bytes, int size) {
		StringBuffer sBuffer = new StringBuffer();
		for (int i = 0; i < size; i++) {
			String s = Integer.toHexString(bytes[i] & 0xff);
			if (s.length() < 2)
				sBuffer.append('0');
			sBuffer.append(s + " ");
		}
		return sBuffer;
	}

	public static boolean RootCommand(String command) {
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			// process.waitFor();
		} catch (Exception e) {
			Log.d("*** DEBUG ***", "ROOT REE" + e.getMessage());
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				Thread.sleep(10);
				process.destroy();
			} catch (Exception e) {
			}
		}
		return true;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "CaptureService onCreate");

	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		context = this;
		Log.v(TAG, "CaptureService onStart");
		// ������ʾ��
		beepManager = new ServiceBeepManager(this);
		beepManager.updatePrefs();

		CaptureService.serialPort = new SerialPortClass(
				CaptureService.choosed_serial, CaptureService.choosed_buad);

		// ��ģ���Դ
		scanGpio.openPower();

		// ��Ļ״̬�㲥��ʼ��
		screenStatusReceiver = new RemoteControlReceiver();
		IntentFilter screenStatusIF = new IntentFilter();
		screenStatusIF.addAction(Intent.ACTION_SCREEN_ON);
		screenStatusIF.addAction(Intent.ACTION_SCREEN_OFF);
		screenStatusIF.addAction(Intent.ACTION_SHUTDOWN);
		// screenStatusIF.addAction("com.zkc.Receiver.RemoteControlReceiver");
		// ע��
		registerReceiver(screenStatusReceiver, screenStatusIF);

		sharedPreferences = getSharedPreferences("com.zkc.barcodescan",
				Activity.MODE_PRIVATE);
		if (sharedPreferences != null) {
			String str = CaptureService.sharedPreferences.getString("encoding",
					"");
			if (str != null && str != "") {
				CaptureService.str_encoding = str;
			}
		}
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "CaptureService onDestroy");
		scanGpio.closePower();
		if (serialPort != null) {
			serialPort.CloseDevice();
			serialPort = null;
		}
		unregisterReceiver(screenStatusReceiver);
		super.onDestroy();
	}

}
