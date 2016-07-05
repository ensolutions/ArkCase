'use strict';

/**
 * @ngdoc service
 * @name services:Acm.StoreService
 *
 * @description
 *
 * {@link https://gitlab.armedia.com/arkcase/ACM3/tree/develop/acm-standard-applications/acm-law-enforcement/src/main/webapp/resources/services/acm/acm-store.client.service.js services/acm/acm-store.client.service.js}
 *
 * This service package contains objects and functions for data storage
 */
angular.module('services').factory('Acm.StoreService', ['$rootScope', 'UtilService', '$window', 'Util.TimerService'
    , function ($rootScope, Util, $window, UtilTimerService
    ) {
        var Store = {
            /**
             * @ngdoc service
             * @name Acm.StoreService.Variable
             *
             * @description
             *
             * {@link https://gitlab.armedia.com/arkcase/ACM3/tree/develop/acm-standard-applications/acm-law-enforcement/src/main/webapp/resources/services/acm/acm-store.client.service.js services/acm/acm-store.client.service.js}
             *
             * Variable represents map like data structure. Data are saved in Angular $rootScope.
             */
            /**
             * @ngdoc method
             * @name Constructor
             * @methodOf Acm.StoreService.Variable
             *
             * @param {String} name Name
             * @param {Object} initValue Initial value
             *
             * @description
             * Create a reference object to a Variable.
             *
             * Example:
             *
             * var v = new Variable("MyData");
             *
             * var v2 = new Variable("MyData", "first");    //initialize value to "first"
             */
            Variable: function (name, initValue) {
                this.name = name;
                $rootScope._storeVariableMap = $rootScope._storeVariableMap || {};
                if (undefined != initValue) {
                    $rootScope._storeVariableMap[name] = initValue;
                }
            }

            /**
             * @ngdoc service
             * @name Acm.StoreService.SessionData
             *
             * @description
             *
             * {@link https://gitlab.armedia.com/arkcase/ACM3/tree/develop/acm-standard-applications/acm-law-enforcement/src/main/webapp/resources/services/acm/acm-store.client.service.js services/acm/acm-store.client.service.js}
             *
             * SessionData represent data saved in session. Each data are identified by a name. It persists through the entire login session.
             */
            /**
             * @ngdoc method
             * @name Constructor
             * @methodOf Acm.StoreService.SessionData
             *
             * @param {String} name (Optional)Name. If not provided, a random name is generated for use
             *
             * @description
             * Create a reference object to a SessionData.
             *
             * Example:
             *
             * var sd = new SessionData("MyData");
             */
            , SessionData: function (name) {
                this.name = name;
            }


            /**
             * @ngdoc service
             * @name Acm.StoreService.LocalData
             *
             * @description
             *
             * {@link https://gitlab.armedia.com/arkcase/ACM3/tree/develop/acm-standard-applications/acm-law-enforcement/src/main/webapp/resources/services/acm/acm-store.client.service.js services/acm/acm-store.client.service.js}
             *
             * LocalData represent data saved in local storage. Each data are identified by a name. It persists on user computer until deleted.
             */
            /**
             * @ngdoc method
             * @name Constructor
             * @methodOf Acm.StoreService.LocalData
             *
             * @param {String} name (Optional)Name. If not provided, a random name is generated for use
             *
             * @description
             * Create a reference object to a LocalData.
             *
             * Example:
             *
             * var ld = new LocalData("MyData");
             */
            , LocalData: function (name) {
                this.name = name;
            }


            /**
             * @ngdoc service
             * @name Acm.StoreService.CacheFifo
             *
             * @description
             *
             * {@link https://gitlab.armedia.com/arkcase/ACM3/tree/develop/acm-standard-applications/acm-law-enforcement/src/main/webapp/resources/services/acm/acm-store.client.service.js services/acm/acm-store.client.service.js}
             *
             * CacheFifo is cache using first in first out aging algorithm. Each cache is identified by a name.
             * Cache data persists in Angular $rootScope. Data is evicted after timeToLive limit.
             */
            /**
             * @ngdoc method
             * @name Constructor
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @param {Object} arg Argument. It can be an object or a name string. If a string, it is equivalent to {name: arg}
             * @param {String} arg.name (Optional)Name. If not provided, a random name is generated for use
             * @param {Number} arg.maxSize (Optional)Max size. If not provided, default size is 8
             * @param {Number} arg.timeToLive (Optional)Cache item time to live in milliseconds.
             *        If not provided, default value is 7200000 (2 hours); if -1, cache items live forever
             *
             * @description
             * Create a reference object to a CacheFifo.
             *
             * Example:
             *
             * var myCache = new CacheFifo("MyCache");
             * var myCache2 = new CacheFifo({name: "MyCache2", maxSize: 16});
             */
            , CacheFifo: function (arg) {
                if ("string" == typeof arg) {
                    arg = {name: arg};
                }
                this.name = Util.goodMapValue(arg, "name", "Cache" + Math.floor((Math.random() * 1000000000)));

                $rootScope._storeCacheMap = $rootScope._storeCacheMap || {};
                if (!$rootScope._storeCacheMap[this.name]) {
                    $rootScope._storeCacheMap[this.name] = {};

                    var thisCache = this._getThis();
                    thisCache.name = this.name;
                    thisCache.maxSize = Util.goodMapValue(arg, "maxSize", this.DEFAULT_MAX_CACHE_SIZE);
                    thisCache.timeToLive = Util.goodMapValue(arg, "timeToLive", this.DEFAULT_SHELF_LIFE);   //arg.timeToLive in milliseconds; -1 if live forever

                    this.reset();
                    this._evict(thisCache.name, thisCache.timeToLive);
                }
            }
        };
        Store.Variable.prototype = {
            /**
             * @ngdoc method
             * @name get
             * @methodOf Acm.StoreService.Variable
             *
             * @description
             * Get value of a Variable.
             *
             * Example:
             *
             * var dataVar = new Variable("MyData", "first");    //initialize value to "first"
             *
             * var data = dataVar.get();                         // returns "first"
             */
            get: function () {
                return $rootScope._storeVariableMap[this.name];
            }

            /**
             * @ngdoc method
             * @name set
             * @methodOf Acm.StoreService.Variable
             *
             * @param {Object} value Value to set
             *
             * @description
             * Set value of a Variable.
             *
             * Example:
             *
             * var dataVar = new Variable("MyData", "first");    //initialize value to "first"
             *
             * dataVar.set("last");                              // now it contains value "last"
             */
            , set: function (value) {
                $rootScope._storeVariableMap[this.name] = value;
            }
        };

        Store.SessionData.prototype = {
            /**
             * @ngdoc method
             * @name getName
             * @methodOf Acm.StoreService.SessionData
             *
             * @description
             * Get name of a SessionData reference object.
             */
            getName: function () {
                return this.name;
            }

            /**
             * @ngdoc method
             * @name get
             * @methodOf Acm.StoreService.SessionData
             *
             * @description
             * Get value of a SessionData reference object.
             *
             * Example:
             *
             * var dataCache = new SessionData("MyData");
             *
             * dataCache.set('{greeting: "Hello", who: "World"}');
             * var data = dataCache.get();                              // data contains value '{greeting: "Hello", who: "World"}'
             */
            , get: function () {
                var data = sessionStorage.getItem(this.name);
                var item = Util.goodJsonObj(data, null);
                //var item = ("null" === data) ? null : JSON.parse(data);
                return item;
            }

            /**
             * @ngdoc method
             * @name set
             * @methodOf Acm.StoreService.SessionData
             *
             * @param {Object} data Value to set
             *
             * @description
             * Set value of a SessionData.
             *
             * Example:
             *
             * var dataCache = new SessionData("MyData");
             *
             * dataCache.set('{greeting: "Hello", who: "World"}');
             *
             * var data = dataCache.get();                              // data contains value '{greeting: "Hello", who: "World"}'
             */
            , set: function (data) {
                var item = (Util.isEmpty(data)) ? null : JSON.stringify(data);
                sessionStorage.setItem(this.name, item);
            }
        };


        Store.LocalData.prototype = {
            /**
             * @ngdoc method
             * @name getName
             * @methodOf Acm.StoreService.LocalData
             *
             * @description
             * Get name of a v reference object.
             */
            getName: function () {
                return this.name;
            }

            /**
             * @ngdoc method
             * @name get
             * @methodOf Acm.StoreService.LocalData
             *
             * @description
             * Get value of a LocalData reference object.
             *
             * Example:
             *
             * var dataCache = new LocalData("MyData");
             *
             * dataCache.set('{greeting: "Hello", who: "World"}');
             *
             * var data = dataCache.get();                              // data contains value '{greeting: "Hello", who: "World"}'
             */
            , get: function () {
                var data = localStorage.getItem(this.name);
                var item = Util.goodJsonObj(data, null);
                //var item = ("null" === data) ? null : JSON.parse(data);
                return item;
            }

            /**
             * @ngdoc method
             * @name set
             * @methodOf Acm.StoreService.LocalData
             *
             * @param {Object} data Value to set
             *
             * @description
             * Set value of a LocalData.
             *
             * Example:
             *
             * var dataCache = new LocalData("MyData");
             *
             * dataCache.set('{greeting: "Hello", who: "World"}');
             *
             * var data = dataCache.get();                              // data contains value '{greeting: "Hello", who: "World"}'
             */
            , set: function (data) {
                var item = (Util.isEmpty(data)) ? null : JSON.stringify(data);
                localStorage.setItem(this.name, item);
            }
        };

        Store.CacheFifo.prototype = {
            DEFAULT_MAX_CACHE_SIZE: 8
            , DEFAULT_SHELF_LIFE: 7200000           //2 hours = 2 * 3600 * 1000 milliseconds

            , _getThis: function () {
                return $rootScope._storeCacheMap[this.name];
            }

            /**
             * @ngdoc method
             * @name get
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @param {Object} key Key to cache
             *
             * @description
             * Get value of a CacheFifo
             *
             * @returns {Object} Object stored in cache
             *
             * Example:
             *
             * var dataCache = new CacheFifo({name: "MyData", maxSize: 3});
             *
             * dataCache.put("k1", "v1");
             *
             * dataCache.put("k2", "v2");
             *
             * dataCache.put("k3", "v3");
             *
             * dataCache.put("k4", "v4");
             *
             * dataCache.put("k3", "v31");
             *
             * var v1 = dataCache.get("k1");     // null, because it is pushed out by "k4"
             *
             * var v2 = dataCache.get("k2");     // "v2"
             *
             * var v3 = dataCache.get("k3");     // "v31", first "v3" is replaced by second "v31"
             *
             * var v4 = dataCache.get("k4");     // "v4"
             *
             */
            , get: function (key) {
                var thisCache = this._getThis();
                for (var i = 0; i < thisCache.size; i++) {
                    if (thisCache.keys[i] == key) {
                        return thisCache.cache[key];
                    }
                }
                return null;
            }

            /**
             * @ngdoc method
             * @name put
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @param {Object} key Key to cache
             * @param {Object} item Value to set
             *
             * @description
             * Put value of into a CacheFifo. If a key already exists, put() updates the value, instead of creating a new one.
             *
             * Example:
             *
             * var dataCache = new CacheFifo({name: "MyData", maxSize: 3});
             *
             * dataCache.put("k1", "v1");
             *
             * dataCache.put("k2", "v2");
             *
             * dataCache.put("k3", "v3");
             *
             * dataCache.put("k4", "v4");
             *
             * dataCache.put("k3", "v31");
             *
             * var v1 = dataCache.get("k1");     // null, because it is pushed out by "k4"
             *
             * var v2 = dataCache.get("k2");     // "v2"
             *
             * var v3 = dataCache.get("k3");     // "v31", first "v3" is replaced by second "v31"
             *
             * var v4 = dataCache.get("k4");     // "v4"
             *
             */
            , put: function (key, item) {
                var thisCache = this._getThis();
                var putAt = -1;
                for (var i = 0; i < thisCache.size; i++) {
                    if (thisCache.keys[i] == key) {
                        putAt = i;
                        break;
                    }
                }

                if (0 > putAt) {
                    putAt = this._getNext();
                    this._advanceToNext();
                }

                thisCache.cache[key] = item;

                thisCache.timeStamp[key] = new Date().getTime();
                thisCache.keys[putAt] = key;
            }
            , _getNext: function () {
                return this._getNextN(0);
            }
            //Use n to keep track number of recursive call to _getNextN(), so that it will not exceed maxSize and into an infinite loop
            , _getNextN: function (n) {
                var thisCache = this._getThis();
                var next = thisCache.next;
                if (!this.isLock(thisCache.keys[next])) {
                    return next;
                }

                if (n > thisCache.maxSize) {     //when n == maxSize, _getNextN() is called maxSize times, pick the first one to avoid infinite loop
                    return next;
                }

                this._advanceToNext();
                return this._getNextN(n + 1);

            }
            , _advanceToNext: function () {
                var thisCache = this._getThis();
                thisCache.next = (thisCache.next + 1) % thisCache.maxSize;
                thisCache.size = (thisCache.maxSize > thisCache.size) ? (thisCache.size + 1) : thisCache.maxSize;
            }

            /**
             * @ngdoc method
             * @name remove
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @param {Object} key Key to cache
             *
             * @description
             * Remove an item from CacheFifo.
             *
             */
            , remove: function (key) {
                var thisCache = this._getThis();
                var delAt = -1;
                for (var i = 0; i < thisCache.size; i++) {
                    if (thisCache.keys[i] == key) {
                        delAt = i;
                        break;
                    }
                }

                if (0 <= delAt) {
                    var newKeys = [];
                    for (var i = 0; i < thisCache.maxSize; i++) {
                        newKeys.push(null);
                    }

                    if (thisCache.size == thisCache.maxSize) {
                        var n = 0;
                        for (var i = 0; i < thisCache.size; i++) {
                            if (i != delAt) {
                                newKeys[n] = thisCache.keys[(thisCache.next + i + thisCache.maxSize) % thisCache.maxSize];
                                n++;
                            }
                        }
                    } else {
                        var n = 0;
                        for (var i = 0; i < thisCache.size; i++) {
                            if (i != delAt) {
                                newKeys[n] = thisCache.keys[i];
                                n++;
                            }
                        }
                    }
                    thisCache.size--;
                    thisCache.next = thisCache.size;

                    thisCache.keys = newKeys;

                    delete thisCache.cache[key];

                    delete thisCache.timeStamp[key];
                } //end if (0 <= delAt) {
            }

            /**
             * @ngdoc method
             * @name reset
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @description
             * Reset CacheFifo.
             *
             */
            , reset: function () {
                var thisCache = this._getThis();

                thisCache.next = 0;
                thisCache.size = 0;
                thisCache.cache = {};

                thisCache.timeStamp = {};
                thisCache.keys = [];
                for (var i = 0; i < thisCache.maxSize; i++) {
                    thisCache.keys.push(null);
                }
                thisCache.locks = [];
            }

            /**
             * @ngdoc method
             * @name keys
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @description
             * Return keys in array of CacheFifo.
             *
             * @returns {Array} Keys in array
             */
            , keys: function () {
                var thisCache = this._getThis();
                return thisCache.keys;
            }

            , _evict: function (name, timeToLive) {
                if (0 < timeToLive) {
                    var that = this;
                    var thisCache = this._getThis();
                    var now = new Date().getTime();
                    UtilTimerService.useTimer(name
                        , 300000     //every 5 minutes = 5 * 60 * 1000 milliseconds
                        , function () {
                            var keys = thisCache.keys;
                            var len = keys.length;
                            for (var i = 0; i < thisCache.size; i++) {
                                var key = keys[i];
                                var ts = thisCache.timeStamp[key];
                                if (timeToLive < now - ts) {
                                    that.remove(key);
                                }
                            }
                            return true;
                        }
                    );
                }
            }

            /**
             * @ngdoc method
             * @name lock
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @param {Object} key Key to cache
             *
             * @description
             * Lock an item in CacheFifo, so that it has higher priority not to be aged first.
             *
             */
            , lock: function (key) {
                var thisCache = this._getThis();
                thisCache.locks.push(key);
            }

            /**
             * @ngdoc method
             * @name unlock
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @param {Object} key Key to cache
             *
             * @description
             * Unlock an item in CacheFifo.
             *
             */
            , unlock: function (key) {
                var thisCache = this._getThis();
                for (var i = 0; i < thisCache.locks.length; i++) {
                    if (thisCache.locks[i] == key) {
                        thisCache.locks.splice(i, 1);
                        return;
                    }
                }
            }

            /**
             * @ngdoc method
             * @name isLock
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @param {Object} key Key to cache
             *
             * @description
             * Return true if an item is locked in CacheFifo.
             *
             */
            , isLock: function (key) {
                var thisCache = this._getThis();
                for (var i = 0; i < thisCache.locks.length; i++) {
                    if (thisCache.locks[i] == key) {
                        return true;
                    }
                }
                return false;
            }

            /**
             * @ngdoc method
             * @name getMaxSize
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @description
             * Get maxSize of CacheFifo setting.
             *
             */
            , getMaxSize: function () {
                var thisCache = this._getThis();
                return thisCache.maxSize;
            }

            /**
             * @ngdoc method
             * @name setMaxSize
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @param {Number} maxSize Max size
             *
             * @description
             * Set maxSize of CacheFifo.
             *
             */
            , setMaxSize: function (maxSize) {
                var thisCache = this._getThis();
                thisCache.maxSize = maxSize;
            }

            /**
             * @ngdoc method
             * @name getTimeToLive
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @description
             * Get timeToLive of CacheFifo setting.
             *
             */
            , getTimeToLive: function () {
                var thisCache = this._getThis();
                return thisCache.timeToLive;
            }

            /**
             * @ngdoc method
             * @name setTimeToLive
             * @methodOf Acm.StoreService.CacheFifo
             *
             * @param {Number} timeToLive Expiration in milliseconds
             *
             * @description
             * Set timeToLive of CacheFifo.
             *
             */
            , setTimeToLive: function (timeToLive) {
                var thisCache = this._getThis();
                thisCache.timeToLive = timeToLive;
            }
        };


        //
        // IE11 has known issue with Local Storage. Following is a work-around until Microsoft provides a fix.
        // http://connect.microsoft.com/IE/feedbackdetail/view/812563/ie-11-local-storage-synchronization-issues#
        //
        var getIeVersion = function () {
            var sAgent = $window.navigator.userAgent;
            var Idx = sAgent.indexOf("MSIE");

            // If IE, return version number.
            if (Idx > 0)
                return parseInt(sAgent.substring(Idx+ 5, sAgent.indexOf(".", Idx)));

            // If IE 11 then look for Updated user agent string.
            else if (!!navigator.userAgent.match(/Trident\/7\./))
                return 11;

            else
                return 0; //It is not IE
        };
        if (11 == getIeVersion()) {
            $window.addEventListener("storage", function (e) {
                // Dummy
            }, false);
        }

        return Store;
    }
]);