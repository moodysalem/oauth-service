define(["backbone", "react", "react-dom", "model", "underscore", "rbs/components/mixins/Model", "rbs/components/controls/Tappable",
        "js/Nav"],
    function (Backbone, React, dom, m, _, model, tappable, nav) {
        "use strict";

        // component that re-renders on application model change as well as wraps the children in a tap-friendly listener
        var wrapper = _.rf({
            displayName: "wrapper",
            mixins: [model],
            render: function () {
                return tappable({}, this.props.children);
            }
        });

        var renderFile = function (file, properties) {
            require([file], function (comp) {
                dom.render(wrapper({model: m}, comp(properties)), $("#app").get(0));
            });
        };

        dom.render(nav({model: m}), $("#nav").get(0));

        return Backbone.Router.extend({
            routes: {
                "(/)": "home"
            },

            home: function () {
                renderFile("js/views/Home");
            }
        });
    });