var PLUGIN_NAME = 'CBLite'

var DataLayer = {
    echo: function (phrase, cb) {
        cordova.exec(cb, null, PLUGIN_NAME, 'echo', [phrase])
    },
    getURL: function () {
        cordova.exec(success, error, PLUGIN_NAME, "getURL", [])

        function success(url) {
            // cb(false, url)
            return Promise.resolve(url)
        }
        function error(error) {
            // cb(err)
            return Promise.reject(error)
        }
    },
    getSampleSets: function () {
        cordova.exec(success, error, PLUGIN_NAME, 'getSampleSets', [])

        function success(data) {
            // cb(data)
            return Promise.resolve(data)
        }
        function error(error) {
            // cb(error)
            return Promise.reject(error)
        }
    }
}

module.exports = DataLayer