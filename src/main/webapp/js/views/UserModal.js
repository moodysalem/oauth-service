/**
 *
 */
define([ "react", "util", "underscore", "rbs/components/layout/Modal", "rbs/components/layout/Form", "rbs/components/model/GridRow",
    "rbs/components/controls/Button", "rbs/components/collection/Alerts", "rbs/components/mixins/Model" ],
  function (React, util, _, modal, form, row, btn, alerts, model) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    var r2 = [
      {
        attribute: "verified",
        label: "Verified",
        tip: "Whether the e-mail address has been verified.",
        component: "checkbox",
        xs: 12
      }
    ];

    var r3 = [
      {
        attribute: "firstName",
        label: "First Name",
        required: true,
        placeholder: "First Name",
        tip: "Enter the user's first name.",
        component: "text",
        sm: 6
      },
      {
        attribute: "lastName",
        label: "Last Name",
        placeholder: "Last Name",
        required: true,
        tip: "Enter the user's last name.",
        component: "text",
        sm: 6
      }
    ];

    return util.rf({
      displayName: "user modal",

      mixins: [ model ],

      propTypes: {
        onSave: rpt.func
      },

      render: function () {
        return modal(_.extend({}, this.props), [
          d.div({ key: "mb", className: "modal-body" }, [
            form({
              key: "mb",
              ref: "f",
              onSubmit: _.bind(function () {
                this.props.model.save({}, {
                  success: this.props.onSave
                });
              }, this)
            }, [
              row({
                key: "r1",
                model: this.props.model,
                attributes: [
                  {
                    attribute: "email",
                    label: "E-mail address",
                    tip: "Enter the e-mail address of the user.",
                    placeholder: "E-mail address",
                    component: "email",
                    required: true,
                    xs: 12,
                    readOnly: this.state.model.id > 0
                  }
                ]
              }),
              row({ key: "r2", model: this.props.model, attributes: r2 }),
              row({ key: "r3", model: this.props.model, attributes: r3 })
            ]),
            alerts({ key: "alts", watch: this.props.model, showSuccess: false })
          ]),
          d.div({ key: "mf", className: "modal-footer" }, [
            btn({
              key: "c",
              caption: "Cancel",
              onClick: this.props.onClose
            }),
            btn({
              key: "b",
              icon: "plus",
              type: "success",
              caption: "Save",
              onClick: _.bind(function () {
                this.refs.f.submit();
              }, this)
            })
          ])
        ]);
      }
    });
  });