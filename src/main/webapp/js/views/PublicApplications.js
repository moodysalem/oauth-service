/**
 * list of applications that can be registered for
 */
define([ "react", "util", "underscore", "rbs/components/layout/Alert", "model" ],
  function (React, util, _, alt, m) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    return util.rf({
      render: function () {
        var li = m.isLoggedIn();

        return d.div({ className: "container" }, [
          d.h1({ key: "h1", className: "page-header" }, "Public Applications"),
          alt({
            key: "i",
            level: "info",
            icon: "info",
            strong: "Info",
            message: "Here you will be able to create clients for applications you do not own. This feature is coming soon."
          })
        ]);
      }
    });
  });