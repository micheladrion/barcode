package com.zkc.Service;

import android.os.Message;
import android.util.Log;

import com.zkc.pc700.helper.SerialPortHelper;

public class SerialPortClass extends SerialPortHelper{
	protected static final String TAG = "SerialPortClass";



	public SerialPortClass(String device, int baudrate) {
		if (OpenSerialPort(device, baudrate)) {
		}
	}
	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
		// TODO Auto-generated method stub
		byte[] data = new byte[size];
		System.arraycopy(buffer, 0, data, 0, size);
		CaptureService.handler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message m = new Message();
				m.arg1 = size;
				m.obj = buffer;
				CaptureService.handler.sendMessage(m);
				String recStr=bytesToString(buffer,size).toString();
				Log.i(TAG, recStr);
			}
		});
	}
	
	public void CloseDevice() {
		CloseSerialPort();
	}
	

	
	/**
	 * 将byte数组转换为字符串形式表示的十六进制数方便查看
	 */
	public static StringBuffer bytesToString(byte[] bytes,int size) {
		StringBuffer sBuffer = new StringBuffer();
		for (int i = 0; i < size; i++) {
			String s = Integer.toHexString(bytes[i] & 0xff);
			if (s.length() < 2)
				sBuffer.append('0');
			sBuffer.append(s + " ");
		}
		return sBuffer;
	}

}
