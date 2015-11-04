/**
 *
 */
define([ "react", "util", "form" ], function (React, util, form) {
  "use strict";

  var fa = [
    {
      attribute: "name",
      component: "text",
      label: "Name"
    }
  ];

  var d = React.DOM;

  return util.rf({
    render: function () {
      return d.div(_.extend({}, this.props), [
        form({
          ref: "f",
          attributes: fa,
          model: this.props.model
        }),
        d.div({ key: "d", className: "row" })
      ]);
    }
  });
});