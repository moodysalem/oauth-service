/**
 *
 */
define([ "react", "util", "rbs/components/model/Form", "js/Models", "rbs/components/mixins/Model" ],
  function (React, util, fm, mdls, model) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    var nameOpt = util.rf({
      mixins: [ model ],
      render: function () {
        return d.div({}, this.state.model.name);
      }
    });

    var att = [
      {
        attribute: "name",
        placeholder: "Name",
        label: "Name",
        component: "text",
        tip: "The name as users should see this client."
      },
      {
        attribute: "tokenTtl",
        placeholder: "Access Token Time-To-Live",
        label: "Token TTL",
        component: "number",
        tip: "Enter how long an access or client token should last before expiring in seconds from issue time."
      },
      {
        attribute: "refreshTokenTtl",
        placeholder: "Refresh Token Time-To-Live",
        label: "Refresh Token TTL",
        component: "number",
        tip: "Enter how long a refresh token should last before expiring in seconds from issue time. Leave blank if this client should not receive refresh tokens."
      },
      {
        tip: "Type as defined by the OAuth2 specification. This controls whether client credentials are required for some of the token requests.",
        label: "Type",
        attribute: "type",
        component: "select",
        collection: mdls.ClientTypes,
        modelComponent: nameOpt
      },
      {
        tip: "Flows that the client may access. Resource owner credentials and client credentials flows are not compatible with a .",
        label: "Flows",
        attribute: "flows",
        component: "select",
        multiple: true,
        collection: mdls.ClientFlows,
        modelComponent: nameOpt
      }
    ];

    return util.rf({
      displayName: "Client Form",

      submit: function () {
        this.refs.fm.submit();
      },

      render: function () {
        return fm({
          ref: "fm",
          model: this.props.model,
          onSubmit: this.props.onSubmit,
          attributes: att
        })
      }
    });
  });