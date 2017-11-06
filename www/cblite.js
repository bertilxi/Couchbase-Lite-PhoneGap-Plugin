var PLUGIN_NAME = 'CBLite'

var DataLayer = {
  echo: function (phrase, cb) {
    cordova.exec(cb, null, PLUGIN_NAME, 'echo', [phrase])
  },
  getURL: function (callback) {
    cordova.exec(
            function (url) {
              callback(false, url)
            },
            function (err) {
              callback(err)
            }, PLUGIN_NAME, 'getURL', [])
  },
  getSampleSets: function (dbName, appName, locationId) {
    return new Promise(function (resolve, reject) {
      cordova.exec(
                function success (data) {
                  resolve(data)
                },
                function error (error) {
                  reject(error)
                }, PLUGIN_NAME, 'getSampleSets', [dbName, appName, locationId])
    })
  },
  getSamples: function (dbName, appName, locationId) {
    return new Promise(function (resolve, reject) {
      cordova.exec(
                function success (data) {
                  resolve(data)
                },
                function error (error) {
                  reject(error)
                }, PLUGIN_NAME, 'getSamples', [dbName, appName, locationId])
    })
  },
  createViews: function (dbName) {
    return new Promise(function (resolve, reject) {
      cordova.exec(
                function success (data) {
                  resolve(data)
                },
                function error (error) {
                  reject(error)
                }, PLUGIN_NAME, 'createViews', [dbName])
    })
  }
}

module.exports = DataLayer
