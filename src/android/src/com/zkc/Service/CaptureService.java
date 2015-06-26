package com.zkc.Service;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import com.zkc.beep.ServiceBeepManager;
import com.zkc.io.EmGpio;
import com.zkc.pc700.helper.ScanGpio;
import com.zkc.pc700.helper.SerialPort;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class CaptureService extends Service {

	private static final String TAG = "CaptureService";
	private ServiceBeepManager beepManager;// ɨ��ɹ���ʾ��
	private byte[] leaveSetting = new byte[] { 0x04, (byte) 0xC8, 0x04, 0x00,
			(byte) 0xFF, 0x30 };// ��������

	private byte[] packedData = new byte[] { 0x07, (byte) 0xC6, 0x04, 0x08,
			0x00, (byte) 0xEB, 0x07, (byte) 0xFE, 0x35 };// ��ȡ���ݸ�ʽ
	public static SerialPort serialPort;
	private InputStream mInputStream;// ��ȡ��Ϣ��
	private String choosed_serial = "/dev/ttyMT0";// ���ں�
	private int choosed_buad = 9600;// ������
	private ReadThread readThread;// ��ȡ��Ϣ�����߳�
	private static byte[] getbuffer = new byte[1024];// ��������Ϣ
	private static int getsize = 0;// ������Ϣ����
	public static ScanGpio scanGpio = new ScanGpio();
	
	private final IBinder myBinder = new CaptureServiceBinder();
	private ArrayList<CaptureServiceHandler> handlers = new ArrayList<CaptureService.CaptureServiceHandler>();
	
	public interface CaptureServiceHandler {
		public void on_get_barcode( String barcode );
	}
	
	public void registerHandler( CaptureServiceHandler handler ){
		handlers.add(handler);
	}
	
	public void unregisterHandler( CaptureServiceHandler handler ){
		handlers.remove(handler);
	}

	public class CaptureServiceBinder extends Binder {
		public CaptureService getService() {
			return CaptureService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "CaptureService onBind");
		return myBinder;
	}
	
	/**
	 * ��ȡ��Ϣ����
	 * 
	 * @author zkc-soft2
	 * 
	 */
	class ReadThread extends Thread {

		@Override
		public void run() {

			while (!interrupted()) {
				try {

					int size;
					byte[] buffer = new byte[1024];

					if (mInputStream == null)
						return;
					// ��ȡ������Ϣ
					size = mInputStream.read(buffer);
					Log.v(TAG, size + "");
					// �����ȡ������Ϣ
					for (int i = 0; i < size; i++) {
						getbuffer[getsize + i] = buffer[i];
					}
					// ����������Ϣ����
					getsize += size;
					handler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Message m = new Message();
							m.arg1 = getsize;
							m.obj = getbuffer;
							handler.sendMessage(m);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();

					return;
				}
			}

		}
	}

	/**
	 * ��ʾ��ϢHandle
	 */
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			byte[] getdata = (byte[]) msg.obj;

			int sizs = msg.arg1;
			byte[] setData = new byte[sizs];
			boolean isfull = false;// �Ƿ�ȡ����������
			boolean isLuanMa = false;// �״��Ƿ�����������ݡ�4, -48, 0, 0, -1,
										// 44��

			for (int i = 0; i < sizs; i++) {
				setData[i] = getdata[i];
				if (getdata[i] == 13) {
					isfull = true;
				}
				if (getdata[i] == -1) {
					isLuanMa = true;
				}
			}

			if (isfull) {
				// ����ɨ��ɹ���ʾ��
				//beepManager.playBeepSoundAndVibrate();
				// MainActivity.isScanOpen = false;
				try {
					String getStringPort;
					// ת��ɨ����ϢΪ�ַ�������ʽUTF-8
					if (isLuanMa) {
						setData = new byte[sizs];
						for (int j = 6; j < sizs; j++) {
							setData[j - 6] = getdata[j];
						}
						getStringPort = new String(setData, 0, sizs, "UTF-8");
					} else {
						getStringPort = new String(setData, 0, sizs, "UTF-8");
					}
					Log.d(TAG, getStringPort);

					// MainActivity.et_code.setText(getStringPort);
					beepManager.playBeepSoundAndVibrate();
					CaptureServiceHandler iter_handler;
					for( int i = 0; i<handlers.size(); i++ ){
						iter_handler = handlers.get(i);
						iter_handler.on_get_barcode(getStringPort);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	};

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "CaptureService onCreate");

		// ������ʾ��
		beepManager = new ServiceBeepManager(this);
		beepManager.updatePrefs();
		try {
			// �򿪵�Դ
			scanGpio.openPower();
			// ���Ӵ���
			serialPort = new SerialPort(choosed_serial, choosed_buad, 0);
			mInputStream = serialPort.getInputStream();

			// ���ý��ո�ʽ
			serialPort.send_Instruct(packedData);
			// ������ȡ��Ϣ����
			readThread = new ReadThread();
			readThread.start();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public static void clerkMessage() {
		getsize = 0;// ɨ����Ϣ������0
		getbuffer = new byte[1024];// ���ɨ����Ϣ
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.v(TAG, "CaptureService onStart");
		super.onStart(intent, startId);

		// if (MainActivity.isStartOpen) {

		// MainActivity.isStartOpen = false;
		// ����ı���
		// if (MainActivity.et_code != null) {
		// MainActivity.et_code.setText("");
		// }
		// ������ݣ���ɨ��
		CaptureService.clerkMessage();
		CaptureService.scanGpio.openScan();

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// MainActivity.isScanOpen = false;
			}
		}).start();

		// }
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.v(TAG, "CaptureService onDestroy");

		if (readThread != null) {
			readThread.interrupt();
			serialPort = null;
		}
		super.onDestroy();
	}

}
