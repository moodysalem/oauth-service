/**
 *
 */
define([ "react", "util" ], function (React, util) {
  "use strict";

  var d = React.DOM;

  return util.rf({
    getInitialState: function () {
      return {};
    },

    render: function () {
      return d.div({
        className: "container"
      }, [
        d.h2({ className: "page-header", key: "p" }, "My Applications")
      ]);
    }
  });
});