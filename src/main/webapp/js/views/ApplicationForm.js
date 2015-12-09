/**
 *
 */
define([ "react", "rbs", "rbs/components/layout/Form", "rbs/components/model/GridRow", "./Loading" ],
  function (React, rbs, form, row, lw) {
    "use strict";
    var util = rbs.util;

    var d = React.DOM;

    var mainDetails = [
      {
        attribute: "id",
        label: "ID",
        tip: "This ID is used to identify your application. You need to verify this matches the token on the resource server when validating access tokens.",
        component: "text",
        readOnly: true,
        sm: 2
      },
      {
        attribute: "name",
        label: "Name",
        placeholder: "Name",
        tip: "Enter the name by which you'd like your application to be identified.",
        component: "text",
        sm: 5,
        required: true
      },
      {
        attribute: "supportEmail",
        label: "Support E-mail",
        placeholder: "Support E-mail",
        tip: "Enter the e-mail to which users we e-mail will be directed to reply.",
        component: "email",
        required: true,
        sm: 5
      }
    ];

    var logoUrl = [
      {
        attribute: "logoUrl",
        label: "Logo URL",
        tip: "Enter a URL for a logo that will appear in the public applications section.",
        placeholder: "Logo URL",
        component: "url",
        xs: 12
      }
    ];


    var description = [
      {
        attribute: "description",
        label: "Description",
        tip: "Enter a short description of your application.",
        placeholder: "Enter a short description of your application.",
        component: "textarea",
        xs: 12
      }
    ];


    var publicRegistrationCheckbox = [
      {
        attribute: "publicClientRegistration",
        label: "Public for Client Registration",
        tip: "Check this box to allow other users to register clients for this application. You can choose which scopes require your approval on the scopes page.",
        component: "checkbox",
        xs: 12
      }
    ];

    var stylesheetUrl = [
      {
        attribute: "stylesheetUrl",
        label: "Stylesheet URL",
        placeholder: "Stylesheet URL",
        tip: "Enter the URL of a CSS stylesheet that should be used as the theme of the login page.",
        component: "url",
        xs: 12
      }
    ];

    var legacyUrl = [
      {
        attribute: "legacyUrl",
        label: "Legacy URL",
        placeholder: "Legacy URL",
        tip: "Enter a URL to be treated as a webhook for legacy login account requests.",
        component: "url",
        xs: 12
      }
    ];

    var fbCredentials = [
      {
        attribute: "facebookAppId",
        label: "Facebook Application ID",
        placeholder: "Facebook Application ID",
        tip: "Enter the Facebook Application ID given to your application to enable login via Facebook.",
        component: "text",
        sm: 5
      },
      {
        attribute: "facebookAppSecret",
        label: "Facebook Application Secret",
        placeholder: "Facebook Application Secret",
        tip: "Enter the Facebook Application Secret given to your application to enable login via Facebook.",
        component: "password",
        sm: 7
      }
    ];

    var googleCredentials = [
      {
        attribute: "googleClientId",
        label: "Google Client ID",
        placeholder: "Google Client ID",
        tip: "Enter the Google Client ID given to your application to enable login via Google.",
        component: "text",
        sm: 5
      },
      {
        attribute: "googleClientSecret",
        label: "Google Client Secret",
        placeholder: "Google Client Secret",
        tip: "Enter the Google Client Secret given to your application to enable login via Google.",
        component: "password",
        sm: 7
      }
    ];

    var amazonCredentials = [
      {
        attribute: "amazonClientId",
        label: "Amazon Client ID",
        placeholder: "Amazon Client ID",
        tip: "Enter the Amazon Client ID given to your application to enable login via Amazon.",
        component: "text",
        sm: 5
      },
      {
        attribute: "amazonClientSecret",
        label: "Amazon Client Secret",
        placeholder: "Amazon Client Secret",
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
            d.div({ className: "card", key: "details" }, [
              d.h3({ key: "h41" }, "Application Details"),
              row({
                key: "nameEmail",
                model: this.props.model,
                attributes: mainDetails
              }),
              row({
                key: "logoUrl",
                model: this.props.model,
                attributes: logoUrl
              }),
              row({
                key: "description",
                model: this.props.model,
                attributes: description
              }),
              row({
                key: "public",
                model: this.props.model,
                attributes: publicRegistrationCheckbox
              })
            ]),
            d.div({ className: "card", key: "customize" }, [
              d.h3({ key: "h4" }, "Customize Appearance"),
              row({
                key: "style",
                model: this.props.model,
                attributes: stylesheetUrl
              })
            ]),
            d.div({ className: "card", key: "credentials" }, [
              d.h3({ key: "h4" }, "Login Methods"),
              row({
                key: "r2",
                model: this.props.model,
                attributes: legacyUrl
              }),
              row({
                key: "r3",
                model: this.props.model,
                attributes: fbCredentials
              }),
              row({
                key: "r4",
                model: this.props.model,
                attributes: googleCredentials
              }),
              row({
                key: "r5",
                model: this.props.model,
                attributes: amazonCredentials
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