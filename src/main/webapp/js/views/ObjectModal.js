/**
 *
 */
define([ "react", "rbs", "rbs/components/layout/Modal", "rbs/components/controls/Button" ],
  function (React, rbs, modal, btn) {
    "use strict";
    var util = rbs.util;
    var d = React.DOM;

    return util.rf({
      render: function () {
        var title = this.props.title;
        return modal(_.extend({
          title: title,
          open: this.props.open,
          onClose: this.props.onClose
        }, this.props), []);
      }
    });
  });