window.require.config({
    baseUrl: ""
});
window.define(["rbs/RequireConfig"], function (rc) {
    window.require.config(rc);

    window.require(["react", "react-dom", "underscore", "backbone"], function (React, dom, _, Backbone) {

    });
});