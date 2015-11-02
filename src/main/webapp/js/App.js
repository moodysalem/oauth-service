"use strict";
window.require.config({
  baseUrl: ""
});
window.define([ "rbs/RequireConfig" ], function (rc) {
  window.require.config(rc);

  window.require([ "js/OAuth2", "backbone", "jquery", "promise-polyfill", "util" ], function (oauth2, Backbone, $, pp, util) {
    var m = new Backbone.Model();
    window.define("model", m);

    var start = function () {
      require([ "js/Router" ], function (router) {
        var r = new router();
        window.define("router", r);

        $(function () {
          $(document).on("click", "[href]", function (e) {
            var href = $(e.target).closest("[href]").attr("href");
            if (typeof href !== "string") {
              return;
            }
            var isInternal = util.internalLink(href);
            if (isInternal || href === "#") {
              e.preventDefault();
            }
            if (isInternal) {
              r.navigate(href, { trigger: true });
            }
          });
        });

        Backbone.history.start({
          pushState: true
        });
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