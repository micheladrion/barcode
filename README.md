# Cordova Hello World Plugin

## Using
Clone the plugin

    $ git clone https://github.com/micheladrion/barcode.git

    
Install the plugin

    1) download and install
    
	$ cordova plugin install https://github.com/micheladrion/barcode.git
	
    You need simple fixing in com.zkc.beep.ServiceBeepManager.java
	
	2) simple fixing
	
	import com.zkc.barcodescan.R;
	
	replace com.zkc.barcodescan with your bundle id.
    

You can set hadler anytime after calling `onDeviceReady`

	barcode.set_handler( function( str_code ){
		alert( "monitor: " + str_code);
	} );

You should call scan function when trying to scan

	barcode.scan();
	
Install iOS or Android platform

    cordova platform add ios
    cordova platform add android
    
Run the code

    cordova run 

## More Info

For more information on setting up Cordova see [the documentation](http://cordova.apache.org/docs/en/4.0.0/guide_cli_index.md.html#The%20Command-Line%20Interface)

For more info on plugins see the [Plugin Development Guide](http://cordova.apache.org/docs/en/4.0.0/guide_hybrid_plugins_index.md.html#Plugin%20Development%20Guide)
