package com.micheladrion.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import com.zkc.Service.CaptureService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class Barcode extends CordovaPlugin {
	
	CallbackContext callback_context;
	private ScanBroadcastReceiver scanBroadcastReceiver;
	
	/**
     * Called after plugin construction and fields have been initialized.
     * Prefer to use pluginInitialize instead since there is no value in
     * having parameters on the initialize() function.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    	
    }
    
    /**
     * Called after plugin construction and fields have been initialized.
     */
    protected void pluginInitialize() {
		Context context = this.cordova.getActivity().getApplicationContext();
		Intent newIntent = new Intent(context, CaptureService.class);
		newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(newIntent);

		//getOverflowMenu();

		scanBroadcastReceiver = new ScanBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.zkc.scancode");
		context.registerReceiver(scanBroadcastReceiver, intentFilter);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("scan")) {
        	//callback_context = callbackContext;
        	StartCapture();
            return true;
        }else if(action.equals("init")){
        	return true;
        }else if(action.equals("set_handler")){
        	this.callback_context = callbackContext; 
        	return true;
        }else {
            return false;
        }
    }

    public void StartCapture(){
    	CaptureService.barCodeStr = "";
		CaptureService.scanGpio.openScan();
    }
    
	
	class ScanBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String text = intent.getExtras().getString("code");
			if( callback_context != null ){
				
				PluginResult result = new PluginResult(PluginResult.Status.OK, text );
				result.setKeepCallback(true);
				callback_context.sendPluginResult(result);
				
				//callback_context.success(text);				
			}
			
		}
	}
}
