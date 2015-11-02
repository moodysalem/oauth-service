define([ "backbone", "react", "react-dom", "model", "underscore", "rbs/components/mixins/Model", "rbs/components/controls/Tappable",
    "js/Nav", "util" ],
  function (Backbone, React, dom, m, _, model, tappable, nav, util) {
    "use strict";

    // component that re-renders on application model change as well as wraps the children in a tap-friendly listener
    var wrapper = util.rf({
      displayName: "wrapper",
      mixins: [ model ],
      render: function () {
        return tappable({}, this.props.children);
      }
    });

    var renderFile = function (file, properties) {
      require([ file ], function (comp) {
        dom.render(wrapper({ model: m }, comp(properties)), $("#app").get(0));
      });
    };

    dom.render(nav({ model: m }), $("#nav").get(0));

    return Backbone.Router.extend({
      routes: {
        "applications": "applications",
        "*splat": "home"
      },

      applications: function () {
        renderFile("js/views/Home");
        util.debug("Not yet implemented.");
      },

      home: function () {
        renderFile("js/views/Home");
      }
    });
  });