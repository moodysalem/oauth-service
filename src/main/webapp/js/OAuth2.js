/**
 * This script provides functions for checking the login status and visiting the login page
 */
define(["jquery", "underscore"], function ($, _) {
    "use strict";

    var callback = _.path(window.location.origin, "callback");
    var origin = window.location.origin;

    var loginParams = $.param({
        client_id: window.clientId,
        response_type: "token",
        redirect_uri: origin
    });

    var loginUrl = _.path(origin, "oauth", "authorize?" + loginParams);
    var tokenInfoPath = _.path(origin, "oauth", "token", "info");

    // for caching the token so we don't get a new one unnecessarily
    var TOKEN_KEY = "_login_token";
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
        var def = $.Deferred();

        if (typeof token === "string" && token.length > 0) {
            var d = $.param({
                client_id: window.clientId,
                token: token
            });
            $.ajax({
                url: tokenInfoPath,
                method: "POST",
                data: d,
                success: function (resp) {
                    def.resolve(resp);
                },
                error: function () {
                    def.reject();
                }
            });
        } else {
            def.reject();
        }

        return def.promise();
    };

    // checks if we're logged in to the oauth site already
    var checkLoggedIn = function () {
        var def = $.Deferred();
        var ifr = $("<iframe></iframe>").css("visibility", "hidden")
            .attr("src", loginUrl);
        ifr.on("load", function () {
            try {
                var url = ifr.get(0).contentWindow.location.href;
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
                getTokenInfo(obj.access_token).then(function (resp) {
                    def.resolve(resp);
                }, function () {
                    def.reject();
                });
            } catch (e) {
                def.reject();
            }
            ifr.remove();
        });
        $("body").append(ifr);
        return def.promise();
    };

    return {
        getLoginPath: function () {
            return loginUrl;
        },

        getLoginStatus: function () {
            var def = $.Deferred();
            var tkn = getCachedToken();
            if (typeof tkn === "string" && tkn.length > 0) {
                getTokenInfo(tkn).then(function (obj) {
                    def.resolve(obj);
                }, function () {
                    clearCachedToken();
                    checkLoggedIn().then(function (token) {
                        setCachedToken(token.access_token);
                        def.resolve(token);
                    }, function () {
                        def.reject();
                    });
                });
            } else {
                checkLoggedIn().then(function (token) {
                    setCachedToken(token.access_token);
                    def.resolve(token);
                }, function () {
                    def.reject();
                });
            }
            return def.promise();
        },

        login: function (callbackUrl, scope) {
            window.location.href = loginUrl;
        }
    };
});