/**
 *
 */
define([ "jquery", "react", "rbs", "underscore", "rbs/components/model/Form", "rbs/components/layout/Modal", "model",
    "rbs/components/controls/Button", "rbs/components/layout/Alert" ],
  function ($, React, rbs, _, form, modal, m, btn, alt) {
    "use strict";

    var util = rbs.util;
    var Backbone = rbs.backbone;

    var d = React.DOM;
    var rpt = React.PropTypes;

    var fa = [
      {
        attribute: "name",
        label: "Your Name",
        placeholder: "Your Name",
        tip: "Enter your name.",
        component: "text",
        required: true
      },
      {
        attribute: "email",
        label: "Your E-mail Address",
        placeholder: "Your E-mail Address",
        tip: "Enter your e-mail address if you would like to receive a reply.",
        component: "email",
        required: true
      },
      {
        attribute: "issue",
        label: "Inquiry",
        component: "textarea",
        placeholder: "Your inquiry...",
        tip: "Enter a description of your issue, or ask a question.",
        required: true
      }
    ];

    return util.rf({
      displayName: "contact modal",

      getInitialState: function () {
        return {
          md: new Backbone.Model(),
          error: null
        };
      },

      componentWillUpdate: function (nextProps, nextState) {
        if (nextProps.open && !this.props.open) {
          this.setState({
            error: null
          });
          this.state.md.clear();
          if (m.isLoggedIn()) {
            this.state.md.set({
              name: util.concatWS(" ", m.get("token.user_details.first_name"), m.get("token.user_details.last_name")),
              email: m.get("token.user_details.email")
            });
          }
        }
      },

      render: function () {
        return modal(_.extend({
          title: "Contact Us"
        }, this.props), [
          d.div({ key: "mb", className: "modal-body" }, [
            form({
              key: "F",
              ref: "f",
              attributes: fa,
              model: this.state.md,
              onSubmit: _.bind(function () {
                this.setState({
                  error: null
                });
                $.ajax({
                  url: util.path(API_URL, "support"),
                  method: "POST",
                  contentType: "application/json",
                  data: JSON.stringify(this.state.md.toJSON())
                }).then(_.bind(function () {
                  // success
                  this.props.onClose();
                }, this), _.bind(function () {
                  // fail
                  this.setState({
                    error: alt({
                      key: "alt",
                      level: "warning",
                      icon: "exclamation-triangle",
                      strong: "Error",
                      message: "Failed to send support message."
                    })
                  });
                }, this));
              }, this)
            }),
            this.state.error
          ]),
          d.div({ key: "mf", className: "modal-footer" }, [
            btn({
              ajax: true,
              key: "cancel",
              caption: "Cancel",
              onClick: this.props.onClose
            }),
            btn({
              ajax: true,
              key: "send",
              type: "success",
              caption: "Send",
              icon: "envelope",
              onClick: _.bind(function () {
                this.refs.f.submit();
              }, this)
            })
          ])
        ])
      }
    });
  });