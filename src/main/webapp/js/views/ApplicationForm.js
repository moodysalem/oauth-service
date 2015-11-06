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
      },
      {
        attribute: "publicClientRegistration",
        label: "Public for Client Registration",
        tip: "Check this box to allow other users to register clients for this application. You can choose which scopes require your approval on the scopes page.",
        component: "checkbox",
        xs: 12
      }
    ];

    var secondRow = [
      {
        attribute: "legacyUrl",
        label: "Legacy URL",
        tip: "Enter a URL to be treated as a webhook for legacy login account requests.",
        component: "text",
        xs: 12
      }
    ];

    var thirdRow = [
      {
        attribute: "facebookAppId",
        label: "Facebook Application ID",
        tip: "Enter the Facebook Application ID given to your application to enable login via Facebook.",
        component: "text",
        sm: 5
      },
      {
        attribute: "facebookAppSecret",
        label: "Facebook Application Secret",
        tip: "Enter the Facebook Application Secret given to your application to enable login via Facebook.",
        component: "password",
        sm: 7
      }
    ];

    var fourthRow = [
      {
        attribute: "googleClientId",
        label: "Google Client ID",
        tip: "Enter the Google Client ID given to your application to enable login via Google.",
        component: "text",
        sm: 5
      },
      {
        attribute: "googleClientSecret",
        label: "Google Client Secret",
        tip: "Enter the Google Client Secret given to your application to enable login via Google.",
        component: "password",
        sm: 7
      }
    ];

    var fifthRow = [
      {
        attribute: "amazonClientId",
        label: "Amazon Client ID",
        tip: "Enter the Amazon Client ID given to your application to enable login via Amazon.",
        component: "text",
        sm: 5
      },
      {
        attribute: "amazonClientSecret",
        label: "Amazon Client Secret",
        tip: "Enter the Amazon Client Secret given to your application to enable login via Amazon.",
        component: "password",
        sm: 7
      }
    ];

    return util.rf({
      displayName: "appform",

      render: function () {

        var formProps = _.extend({}, this.props, { autoComplete: false, ref: "f" });

        return d.div(_.extend({}, this.props), [
          lw({
            key: "lw",
            watch: this.props.model
          }, form(formProps, [
            d.div({ className: "card", key: "d1" }, [
              d.h4({ key: "h41" }, "Application Details"),
              row({
                key: "r1",
                model: this.props.model,
                attributes: firstRow
              })
            ]),
            d.div({ className: "card", key: "d2" }, [
              d.h4({ key: "h4" }, "Login Methods"),
              row({
                key: "r2",
                model: this.props.model,
                attributes: secondRow
              }),
              row({
                key: "r3",
                model: this.props.model,
                attributes: thirdRow
              }),
              row({
                key: "r4",
                model: this.props.model,
                attributes: fourthRow
              }),
              row({
                key: "r5",
                model: this.props.model,
                attributes: fifthRow
              })
            ])
          ])),
          d.div({ key: "d", className: "row" })
        ]);
      },

      submit: function () {
        this.refs.f.submit();
      }

    });
  });