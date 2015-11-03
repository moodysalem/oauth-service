/**
 *
 */
define([ "react", "util" ], function (React, util) {
  "use strict";

  var d = React.DOM;

  return util.rf({
    render: function () {
      return d.div({
        className: "container"
      }, [
        d.h1({ className: "page-header", key: "p" }, "My Applications")
      ]);
    }
  });
});