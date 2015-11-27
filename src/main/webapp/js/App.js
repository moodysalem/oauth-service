"use strict";

var lt = function (tx) {
  document.getElementById("loading-text").innerText = tx;
};

// load require configuration from base url
window.require.config({
  baseUrl: ""
});

lt("Loading Require...");
window.define([ "rbs/RequireConfig" ], function (rc) {
  // configure require with the paths for all the third party dependencies
  window.require.config(rc);

  // load other depencies now
  lt("Loading dependencies...");
  window.require([ "rbs", "js/OAuth2", "jquery" ],
    function (rbs, oauth2, $) {
      var bb = rbs.backbone;
      var util = rbs.util;

      util.debug("defining model...");
      var m = new (bb.Model.extend({
        isLoggedIn: function () {
          return this.has("token");
        }
      }))();
      window.define("model", m);

      util.debug("defining start function..");
      var start = function () {
        util.debug("start function called...");
        lt("Initializing application...");
        require([ "js/Router" ], function (router) {
          var r = new router();
          window.define("router", r);

          util.debug("router loaded...");
          $(function () {
            $(document).on("click", "[href]", function (e) {
              var href = $(e.target).closest("[href]").attr("href");
              if (typeof href !== "string") {
                return;
              }
              if (href.length > 0 && href[ 0 ] === "#") {
                e.preventDefault();
                if (href.length > 1) {
                  var els = $(href);
                  if (els.length == 1) {
                    var st = els.offset().top;
                    if (st > 0) {
                      $("html, body").animate({ scrollTop: st + "px" });
                    }
                  }
                }
                return;
              }

              if (e.metaKey || e.ctrlKey) {
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

          util.debug("starting router..");
          bb.history.start({ pushState: true });
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

      util.debug("initializing oauth2");
      oauth2.init({
        clientId: window.clientId,
        token: window.hashObject && window.hashObject.access_token,
        authorizeUrl: "/oauth/authorize",
        tokenInfoUrl: "/oauth/token/info"
      });

      util.debug("getting login status");
      oauth2.getLoginStatus().then(function (token) {
        util.debug("logged in");
        m.set("token", token);
        start();
      }, function (err) {
        util.debug("not logged in", err);
        start();
      });

    });
});