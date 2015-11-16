/**
 *
 */
define([ "react", "util", "underscore", "./MustBeLoggedIn", "model", "rbs/components/combo/Table" ],
  function (React, util, _, mbli, m, table) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    return util.rf({
      displayName: "Users",

      getInitialState: function () {
        return {};
      },


      render: function () {
        if (!m.isLoggedIn()) {
          return mbli();
        }

        return d.div({
          className: "container"
        }, [
          d.div({ className: "alert alert-info" }, [
            "Coming soon..."
          ])
        ]);
      }
    });
  });