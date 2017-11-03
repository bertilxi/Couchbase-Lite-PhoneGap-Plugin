var PLUGIN_NAME = 'CBLite'

var DataLayer = {
    echo: function (phrase, cb) {
        cordova.exec(cb, null, PLUGIN_NAME, 'echo', [phrase])
    },
    getURL: function (callback) {
        cordova.exec(
            function (url) {
                callback(false, url);
            },
            function (err) {
                callback(err);
            }, PLUGIN_NAME, "getURL", []);
    },
    getSampleSets: function () {
        return new Promise(function (resolve, reject) {
            cordova.exec(
                function success(data) {
                    resolve(data)
                },
                function error(error) {
                    if (error) reject(error)
                }, PLUGIN_NAME, 'getSampleSets', [])
        })
    }
}

module.exports = DataLayer