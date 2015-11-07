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
        "docs": "docs",
        "applications/:id": "app",
        "(/)": "home",
        "*splat": "notFound"
      },

      applications: function () {
        renderFile("js/views/Applications");
      },

      docs: function () {
        renderFile("js/views/Documentation");
      },

      app: function (id) {
        renderFile("js/views/Application", { id: id });
      },

      home: function () {
        renderFile("js/views/Home");
      },

      notFound: function () {
        renderFile("js/views/NotFound");
      }
    });
  });