/**
 * just an alert that shows a must be logged in message
 */
define([ "react", "util", "rbs/components/layout/Alert", "js/OAuth2" ], function (React, util, alert, oauth2) {
  "use strict";

  var d = React.DOM;

  return util.rf({
    render: function () {
      return d.div({
        className: "container"
      }, [
        d.h1({ key: "h1", className: "page-header" }, "Uh oh..."),
        alert({
          key: "al",
          strong: "",
          message: [
            "You must be logged in to access this page. Click ",
            d.a({
              className: "alert-link",
              key: "link",
              href: "#",
              onClick: function () {
                oauth2.login();
              }
            }, "here"),
            " to log in"
          ],
          level: "warning",
          icon: "exclamation-triangle"
        })
      ]);
    }
  });
});