"use strict";
window.require.config({
    baseUrl: ""
});
window.define(["rbs/RequireConfig"], function (rc) {
    window.require.config(rc);

    window.require(["js/Router"], function (router) {
        var r = new router();
        window.define("router", r);

        var start = function () {
            Backbone.history.start();
        };

        start();
    });
});