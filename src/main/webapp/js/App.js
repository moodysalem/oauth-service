"use strict";
window.require.config({
    baseUrl: ""
});
window.define(["rbs/RequireConfig"], function (rc) {
    window.require.config(rc);

    window.require(["js/OAuth2", "backbone", "promise-polyfill"], function (oauth2, Backbone) {
        var m = new Backbone.Model();
        window.define("model", m);

        var start = function () {
            require(["js/Router"], function (router) {
                var r = new router();
                window.define("router", r);
                Backbone.history.start();
            });
        };

        oauth2.init({
            clientId: window.clientId,
            token: window.hashObject && window.hashObject.access_token,
            authorizeUrl: "/oauth/authorize",
            tokenInfoUrl: "/oauth/token/info"
        });

        oauth2.getLoginStatus().then(function (token) {
            m.set("token", token);
            start();
        }, function () {
            start();
        });

    });
});