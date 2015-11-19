define([ "backbone", "react", "jquery", "react-dom", "model", "underscore", "rbs/components/mixins/Model", "rbs/components/controls/Tappable",
    "js/Nav", "util", "ga" ],
  function (Backbone, React, $, dom, m, _, model, tp, nav, util, ga) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    var pv = _.noop;
    if (typeof ga === "function") {
      util.debug("initializing google analytics...");
      ga("create", "UA-58623092-4", "auto");
      pv = function () {
        ga("send", "pageview", {
          page: window.location.pathname
        });
      };
    } else {
      util.debug("google analytics not started because GA is blocked...");
    }

    // component that re-renders on application model change as well as wraps the children in a tap-friendly listener
    var wrapper = util.rf({
      displayName: "wrapper",
      propTypes: {
        view: rpt.func.isRequired,
        props: rpt.object
      },
      mixins: [ model ],
      render: function () {
        util.debug("wrapper rendered.");
        return tp({}, this.props.view(this.props.props));
      }
    });

    var origTitle = document.title;

    var renderFile = function (file, properties, title) {
      // remove the page loading first element
      $("#app").find("#page-loading-first").remove();
      // show the page loading indicator
      $("#loading-js").css("display", "block");
      util.debug("getting file to render", file);
      require([ file ], function (comp) {
        util.debug("rendering file", file, properties);
        dom.render(wrapper({ model: m, view: comp, props: properties }), $("#app").get(0));
        pv();
        if (typeof title === "string") {
          document.title = util.concatWS(" | ", origTitle, title);
        } else {
          document.title = origTitle;
        }
        // remove the page loading indicator
        $("#loading-js").css("display", "");
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
        renderFile("js/views/Applications", {}, "Applications");
      },

      docs: function () {
        renderFile("js/views/Documentation", {}, "Documentation");
      },

      app: function (id) {
        renderFile("js/views/Application", { id: id }, "Application");
      },

      home: function () {
        renderFile("js/views/Home", {});
      },

      scopes: function (id) {
        renderFile("js/views/Scopes", { applicationId: id }, "Scopes");
      },

      clients: function (id) {
        renderFile("js/views/Clients", { applicationId: id }, "Clients");
      },

      users: function (id) {
        renderFile("js/views/Users", { applicationId: id }, "Users");
      },


      notFound: function () {
        renderFile("js/views/NotFound");
      }
    });
  });