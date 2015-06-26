package com.micheladrion.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.zkc.Service.CaptureService;
import com.zkc.Service.CaptureService.CaptureServiceBinder;
import com.zkc.Service.CaptureService.CaptureServiceHandler;

public class Barcode extends CordovaPlugin implements CaptureServiceHandler {
	
	CallbackContext callback_context;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("scan")) {
        	callback_context = callbackContext;
        	StartCapture();
            return true;
        } else {
            return false;
        }
    }

    public void StartCapture(){
    	Context context = this.cordova.getActivity().getApplicationContext();
    	Intent newIntent = new Intent(context,
				CaptureService.class);
		newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.cordova.getActivity().startService(newIntent);
		this.cordova.getActivity().bindService(newIntent, myConnection, Context.BIND_AUTO_CREATE);
    }
    
    CaptureService myService;
    boolean isBound = false;

	private ServiceConnection myConnection = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        CaptureServiceBinder binder = (CaptureServiceBinder) service;
	        myService = binder.getService();
	        myService.registerHandler(Barcode.this);
	        isBound = true;
	    }
	    
	    public void onServiceDisconnected(ComponentName arg0) {
	        isBound = false;
	    }
	    
	   };

	@Override
	public void on_get_barcode(String barcode) {
		// TODO Auto-generated method stub
		callback_context.success(barcode);
	}
}
