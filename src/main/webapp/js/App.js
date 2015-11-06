"use strict";

var txt = function (tx) {
  document.getElementById("loading-text").innerText = tx;
};

window.require.config({
  baseUrl: ""
});
txt("Loading Require...");
window.define([ "rbs/RequireConfig" ], function (rc) {
  window.require.config(rc);

  txt("Loading dependencies...");
  window.require([ "js/OAuth2", "backbone", "jquery", "promise-polyfill", "util" ], function (oauth2, Backbone, $, pp, util) {
    var m = new Backbone.Model();
    window.define("model", m);

    var start = function () {
      txt("Initializing Application...");
      require([ "js/Router" ], function (router) {
        var r = new router();
        window.define("router", r);

        $(function () {
          $(document).on("click", "[href]", function (e) {
            var href = $(e.target).closest("[href]").attr("href");
            if (typeof href !== "string") {
              return;
            }
            if (href === "#") {
              e.preventDefault();
              return;
            }
            var isInternal = util.internalLink(href);
            if (isInternal) {
              e.preventDefault();
            }
            if (isInternal) {
              r.navigate(href, { trigger: true });
            }
          });
        });

        Backbone.history.start({ pushState: true });
      });
    };


    //configure ajax calls to the API to send our token in the authorization header whenever we are logged in
    $(document).ajaxSend(function (event, jqXhr, ajaxOptions) {
      var url = ajaxOptions.url;
      if (url.indexOf("api") === 0) {
        if (m.has("token.access_token")) {
          jqXhr.setRequestHeader("Authorization", "bearer " + m.get("token.access_token"));
        }
      }
    });

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