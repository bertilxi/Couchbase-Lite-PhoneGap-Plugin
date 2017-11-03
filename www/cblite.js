var PLUGIN_NAME = 'CBLite'

var DataLayer = {
    echo: function (phrase, cb) {
        cordova.exec(cb, null, PLUGIN_NAME, 'echo', [phrase])
    },
    getURL: function () {
        return new Promise(function (resolve, reject) {
            cordova.exec(
                function success(url) {
                    resolve(url)
                },
                function error(error) {
                    reject(error)
                }, PLUGIN_NAME, "getURL", [])
        })
    },
    getSampleSets: function () {
        return new Promise(function (resolve, reject) {
            cordova.exec(
                function success(data) {
                    resolve(data)
                },
                function error(error) {
                    reject(error)
                }, PLUGIN_NAME, 'getSampleSets', [])
        })
    }
}

module.exports = DataLayer