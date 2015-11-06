/**
 *
 */
define([ "react", "util", "rbs/components/controls/LoadingWrapper", "underscore" ], function (React, util, lw, _) {
  "use strict";

  var d = React.DOM;
  return util.rf({
    render: function () {
      return lw(_.extend({
        icon: d.div({ className: "loader", style: { width: "30px", height: "30px" } })
      }, this.props), this.props.children);
    }
  });
});