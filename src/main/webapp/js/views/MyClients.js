/**
 *
 */
define([ "rbs", "react", "underscore", "./MustBeLoggedIn", "model" ], function (rbs, React, _, mbli, m) {
  "use strict";

  var d = React.DOM;
  var rpt = React.PropTypes;
  var util = rbs.util;

  return util.rf({
    propTypes: {},
    getInitialState: function () {
      return {};
    },
    getDefaultProps: function () {
      return {};
    },
    componentDidMount: function () {
    },
    componentWillUpdate: function (nextProps, nextState) {
    },
    componentDidUpdate: function (prevProps, prevState) {
    },
    render: function () {
      if (!m.isLoggedIn()) {
        return mbli({});
      }

      return d.div({ className: "container" }, [
        d.h2({ key: "h2", className: "page-header" }, "My Clients")
      ]);
    }
  });
});