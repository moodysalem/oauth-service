/**
 *
 */
define([ "react", "rbs" ], function (React, rbs) {
  "use strict";
  var util = rbs.util;

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