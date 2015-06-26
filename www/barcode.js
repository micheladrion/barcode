/*global cordova, module*/

module.exports = {
    scan: function ( successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Barcode", "scan", [] );
    }
};
