/**
 *
 */
define([ "react", "rbs", "rbs/components/layout/Modal", "rbs/components/layout/Alert", "rbs/components/controls/Button" ],
  function (React, rbs, modal, alert, btn) {
    "use strict";

    var util = rbs.util;

    var d = React.DOM;
    var rpt = React.PropTypes;

    return util.rf({
      displayName: "delete modal",

      propTypes: {
        deleteMessage: rpt.string.isRequired,
        onDelete: rpt.func.isRequired
      },

      render: function () {
        return modal(_.extend({
          title: "Confirm Delete"
        }, this.props), [
          d.div({
            key: "mb",
            className: "modal-body"
          }, [
            alert({
              key: "warning",
              strong: "Warning",
              message: this.props.deleteMessage,
              level: "danger",
              icon: "exclamation-triangle"
            })
          ]),
          d.div({
            key: "mf",
            className: "modal-footer"
          }, [
            btn({
              key: "cancel",
              ajax: true,
              icon: "cancel",
              onClick: this.props.onClose,
              caption: "Cancel"
            }),
            btn({
              key: "del",
              icon: "trash",
              ajax: true,
              type: "danger",
              onClick: this.props.onDelete,
              caption: "Delete"
            })
          ])
        ]);
      }
    });
  });