/**
 * view scopes for an application
 */
define([ "react", "util", "rbs/components/layout/Alert" ], function (React, util, alert) {
  "use strict";

  var rpt = React.PropTypes;
  var d = React.DOM;

  return util.rf({
    propTypes: {
      applicationId: rpt.string
    },

    render: function () {
      return d.div({ className: "container" }, [
        d.h1({ key: "h1", className: "page-header" }, "Scopes"),
        alert({
          key: "alt",
          icon: "exclamation-triangle",
          strong: "Please come back later.",
          message: "This page is not yet implemented.",
          level: "info"
        })
      ]);
    }
  });
});