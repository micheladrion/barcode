/*global cordova, module*/

module.exports = {
    scan: function ( successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Barcode", "scan", [] );
    },
	set_handler: function ( successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Barcode", "set_handler", [] );
    },
	init: function ( successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Barcode", "init", [] );
    },
};
cordova.exec( function(){}, function(){}, "Barcode", "init", [] );