/**
 * This script provides functions for checking the login status and visiting the login page
 */
(function () {
    "use strict";

    var AUTHORIZE_URL = "https://oauth2cloud.com/oauth/authorize";
    var TOKEN_INFO_URL = "https://oauth2cloud.com/oauth/token/info";

    var clientId;

    var getLoginUrl = function (callback, logout) {
        return AUTHORIZE_URL + "?" +
            "client_id=" + encodeURIComponent(clientId) +
            "&response_type=token" +
            "&redirect_uri=" + ((typeof callback === "string") ? encodeURIComponent(callback) : encodeURIComponent(window.location.origin)) +
            ((logout) ? "&logout=true" : "")
    };

    // for caching the token so we don't get a new one unnecessarily
    var TOKEN_KEY = "_oauth2cloud_login_token";
    var getCachedToken = function () {
        return window.localStorage.getItem(TOKEN_KEY);
    };

    var setCachedToken = function (token) {
        window.localStorage.setItem(TOKEN_KEY, token);
    };

    var clearCachedToken = function () {
        window.localStorage.removeItem(TOKEN_KEY);
    };

    // gets info for a token
    var getTokenInfo = function (token) {
        return new Promise(function (resolve, reject) {
            if (typeof clientId !== "string" && clientId.length === 0) {
                console.error("Must initialize the oauth2 library before calling getTokenInfo");
                reject();
                return;
            }
            if (typeof token === "string" && token.length > 0) {
                var d = "client_id=" + encodeURIComponent(clientId) + "&token=" + encodeURIComponent(token);
                $.ajax({
                    url: TOKEN_INFO_URL,
                    method: "POST",
                    data: d,
                    success: function (resp) {
                        resolve(resp);
                    },
                    error: function () {
                        reject();
                    }
                });
            } else {
                reject();
            }
        });
    };

    // checks if we're logged in to the oauth site already
    var checkAlreadyLoggedIn = function () {
        return new Promise(function (resolve, reject) {
            if (typeof clientId !== "string" || clientId === null) {
                reject();
                return;
            }

            var ifr = document.createElement('iframe');
            ifr.style.visibility = "hidden";
            ifr.src = getLoginUrl();
            document.body.appendChild(ifr);
            ifr.onload = function () {
                try {
                    var url = ifr.contentWindow.location.href;
                    var hash = url.split("#")[1];
                    if (typeof hash !== "string" || hash.length === 0) {
                        return;
                    }
                    var pcs = hash.split("&");
                    var i;
                    var obj = {};
                    for (i = 0; i < pcs.length; i++) {
                        var pp = pcs[i].split("=");
                        var n = pp[0], v = pp[1];

                        obj[decodeURIComponent(n)] = decodeURIComponent(v);
                    }
                    resolve(getTokenInfo(obj.access_token));
                } catch (e) {
                    reject();
                }
                ifr.parentNode.removeChild(ifr);
            };
        });
    };

    var getLoginStatus = function () {
        return new Promise(function (resolve, reject) {
            var tkn = getCachedToken();
            if (typeof tkn === "string" && tkn.length > 0) {
                getTokenInfo(tkn).then(function (obj) {
                    resolve(obj);
                }, function () {
                    clearCachedToken();
                    checkAlreadyLoggedIn().then(function (token) {
                        setCachedToken(token.access_token);
                        resolve(token);
                    }, function () {
                        reject();
                    });
                });
            } else {
                checkAlreadyLoggedIn().then(function (token) {
                    setCachedToken(token.access_token);
                    resolve(token);
                }, function () {
                    reject();
                });
            }
        });
    };

    var init = function (object) {
        if (typeof object.clientId === "string") {
            clientId = object.clientId;
        }
        if (typeof object.token === "string") {
            setCachedToken(object.token);
        }
        if (typeof object.authorizeUrl === "string") {
            AUTHORIZE_URL = object.authorizeUrl;
        }
        if (typeof object.tokenInfoUrl === "string") {
            TOKEN_INFO_URL = object.tokenInfoUrl;
        }
    };

    var logout = function () {
        clearCachedToken();
        var ifr = document.createElement('iframe');
        ifr.style.visibility = "hidden";
        ifr.src = getLoginUrl(null, true);
        document.body.appendChild(ifr);
        ifr.onload = function () {
            ifr.parentNode.removeChild(ifr);
        };
    };

    var toReturn = {
        init: init,
        getLoginStatus: getLoginStatus,
        logout: logout
    };

    if (typeof window.define === "function") {
        window.define(toReturn);
    }
})();