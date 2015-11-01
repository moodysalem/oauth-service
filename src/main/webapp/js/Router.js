define(["backbone", "react", "react-dom", "model", "underscore"], function (Backbone, React, dom, m, _) {
    "use strict";

    var renderFile = function (file, properties) {
        require([file], function (comp) {
            dom.render(comp(properties), $("#app").get(0));
        });
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