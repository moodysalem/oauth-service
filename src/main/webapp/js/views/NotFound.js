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
        d.h3({
          key: "h1",
          className: "page-header"
        }, "404: Not Found"),
        d.p({
          key: "p",
          className: "lead"
        }, "The page you have requested is not found.")
      ])
    }
  });
});