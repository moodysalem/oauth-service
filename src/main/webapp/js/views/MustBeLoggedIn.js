/**
 * just an alert that shows a must be logged in message
 */
define([ "react", "util", "rbs/components/layout/Alert" ], function (React, util, alert) {
  "use strict";

  var d = React.DOM;

  return util.rf({
    render: function () {
      return d.div({
        className: "container"
      }, [
        d.h1({ key: "h1", className: "page-header" }, "Error"),
        alert({
          key: "al",
          strong: "",
          message: "You must be logged in to access this page.",
          level: "warning",
          icon: "exclamation-triangle"
        })
      ]);
    }
  });
});