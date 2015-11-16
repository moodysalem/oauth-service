define([ "backbone", "react", "react-dom", "model", "underscore", "rbs/components/mixins/Model", "rbs/components/controls/Tappable",
    "js/Nav", "util", "ga" ],
  function (Backbone, React, dom, m, _, model, tp, nav, util, ga) {
    "use strict";

    var pv = _.noop, lastPath = null;
    if (typeof ga === "function") {
      util.debug("initializing google analytics...");
      ga("create", "UA-58623092-4", "auto");
      pv = _.debounce(function () {
        if (ga && window.location.pathname !== lastPath) {
          ga("send", "pageview", {
            page: (lastPath = window.location.pathname)
          });
        }
      }, 100);
    } else {
      util.debug("google analytics not started because GA is blocked");
    }

    // component that re-renders on application model change as well as wraps the children in a tap-friendly listener
    var wrapper = util.rf({
      displayName: "wrapper",
      mixins: [ model ],
      render: function () {
        util.debug("wrapper rendered.");
        return tp({}, React.DOM.div({}, this.props.children));
      }
    });

    var renderFile = function (file, properties) {
      util.debug("getting file to render", file);
      require([ file ], function (comp) {
        util.debug("rendering file", file, properties);
        dom.render(wrapper({ model: m }, comp(properties)), $("#app").get(0));
        pv();
      });
    };

    util.debug("rendering nav");
    dom.render(nav({ model: m }), $("#nav").get(0));

    return Backbone.Router.extend({
      routes: {
        "applications": "applications",
        "docs": "docs",
        "applications/:id": "app",
        "applications/:id/scopes": "scopes",
        "applications/:id/clients": "clients",
        "applications/:id/users": "users",
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

      scopes: function (id) {
        renderFile("js/views/Scopes", { applicationId: id });
      },

      clients: function (id) {
        renderFile("js/views/Clients", { applicationId: id });
      },

      users: function (id) {
        renderFile("js/views/Users", { applicationId: id });
      },


      notFound: function () {
        renderFile("js/views/NotFound");
      }
    });
  });