package com.zkc.Receiver;

import com.zkc.Service.CaptureService; 
//import com.zkc.barvodeScan.MainActivity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.renderscript.Element;
import android.util.Log;
import android.widget.Toast;

/**
 * 开机启动按键广播监听
 * @author zkc-soft2
 *
 */
public class RemoteControlReceiver extends BroadcastReceiver {
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	 
    	//广播接收
    	String action = intent.getAction();
    	if(action.equals("com.zkc.keycode"))
    	{
		    int keyValue=intent.getIntExtra("keyvalue",0);	
 
	    
			if (keyValue == 136 || keyValue == 135) {
			 
				/*if (MainActivity.isScanOpen == false) {
					MainActivity.isStartOpen =true;
					Intent newIntent = new Intent(context, CaptureService.class);			    	 
			    	newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startService(newIntent);
				
					MainActivity.isScanOpen =true;
				}*/ 			 
			}  
	        
    	}
    }
}