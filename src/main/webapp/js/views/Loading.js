/**
 *
 */
define([ "react", "rbs", "rbs/components/controls/LoadingWrapper", "underscore" ],
  function (React, rbs, lw, _) {
    "use strict";

    var util = rbs.util;

    var d = React.DOM;
    return util.rf({
      render: function () {
        return lw(_.extend({
          icon: d.div({ className: "loader", style: { width: "30px", height: "30px" } })
        }, this.props), this.props.children);
      }
    });
  });