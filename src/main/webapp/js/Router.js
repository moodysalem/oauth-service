define(["backbone", "react", "react-dom"], function (Backbone, React, dom) {
    "use strict";

    var renderFile = function (file, properties) {
        require([file], function (comp) {
            dom.render(comp(properties), $("#app").get(0));
        })
    };

    return Backbone.Router.extend({
        routes: {
            "(/)": "home"
        },

        home: function () {
            renderFile("js/views/Home");
        }
    });
});