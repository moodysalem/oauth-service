/**
 *
 */
define([ "react", "util" ], function (React, util) {
  "use strict";

  var d = React.DOM;

  return util.rf({
    displayName: "app",

    render: function () {
      var dn = (this.props.id === "create") ? "New Application" : "Loading...";

      return d.div({ className: "container" }, [
        d.h2({}, dn)
      ]);
    }
  });
});