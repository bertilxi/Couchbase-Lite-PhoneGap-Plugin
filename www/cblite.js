var PLUGIN_NAME = 'DataLayer';

var DataLayer = {
    echo: function (phrase, cb) {
        cordova.exec(cb, null, PLUGIN_NAME, 'echo', [phrase]);
    },
    getURL: function (callback) {
        cordova.exec(function (url) { callback(false, url); }, function (err) { callback(err); }, "CBLite", "getURL", []);
    },
    getSamples: function (view, cb) {
        cordova.exec(cb, null, PLUGIN_NAME, 'getSamples', [view]);
    }
};

module.exports = DataLayer;