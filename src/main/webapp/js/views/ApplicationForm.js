/**
 *
 */
define([ "react", "util", "rbs/components/layout/Form", "rbs/components/model/GridRow", "rbs/components/controls/LoadingWrapper" ],
  function (React, util, form, row, lw) {
    "use strict";

    var d = React.DOM;

    var firstRow = [
      {
        attribute: "name",
        label: "Name",
        tip: "Enter the name by which you'd like your application to be identified.",
        component: "text",
        sm: 6,
        required: true
      },
      {
        attribute: "supportEmail",
        label: "Support E-mail",
        tip: "Enter the e-mail to which users we e-mail will be directed to reply.",
        component: "email",
        required: true,
        sm: 6
      }
    ];

    var secondRow = [];

    return util.rf({
      displayName: "appform",

      render: function () {
        return d.div(_.extend({}, this.props), [
          lw({
            key: "lw",
            watch: this.props.model
          }, form({
            ref: "f"
          }, [
            row({
              key: "r1",
              model: this.props.model,
              attributes: firstRow
            }),
            row({
              key: "r2",
              model: this.props.model,
              attributes: secondRow
            })
          ])),
          d.div({ key: "d", className: "row" })
        ]);
      }
    });
  });