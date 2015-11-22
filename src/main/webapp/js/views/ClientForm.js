/**
 *
 */
define([ "react", "util", "rbs/components/model/Form", "js/Models", "rbs/components/mixins/Model",
    "rbs/components/controls/Button", "./MultiText", "./ViewablePassword" ],
  function (React, util, fm, mdls, model, btn, multitext, vp) {
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
        required: true,
        label: "Name",
        component: "text",
        tip: "The name as users should see this client."
      },
      {
        attribute: "tokenTtl",
        placeholder: "Access Token Time-To-Live",
        required: true,
        min: 0,
        label: "Token TTL",
        component: "number",
        tip: "Enter how long an access or client token should last before expiring in seconds from issue time."
      },
      {
        attribute: "refreshTokenTtl",
        placeholder: "Refresh Token Time-To-Live",
        label: "Refresh Token TTL",
        min: 0,
        component: "number",
        tip: "Enter how long a refresh token should last before expiring in seconds from issue time. Leave blank if this client should not receive refresh tokens."
      },
      {
        tip: "Type as defined by the OAuth2 specification. This controls whether client credentials are required for some of the token requests.",
        required: true,
        label: "Type",
        attribute: "type",
        searchOn: "name",
        valueAttribute: "name",
        component: "select",
        collection: mdls.ClientTypes,
        modelComponent: nameOpt
      },
      {
        tip: "These are the authorization flows that a client may use.",
        label: "Flows",
        attribute: "flows",
        component: "select",
        searchOn: "name",
        valueAttribute: "name",
        multiple: true,
        collection: mdls.ClientFlows,
        modelComponent: nameOpt
      },
      {
        tip: "The URIs that this client may use as redirect URIs.",
        label: "Redirect URIs",
        attribute: "uris",
        placeholder: "Redirect URIs",
        component: multitext
      }
    ];

    var moreAtt;
    moreAtt = [
      {
        attribute: "identifier",
        label: "Client ID",
        tip: "The client ID is considered public information, and is used to build login URLs, or included in Javascript source code on a page.",
        component: "text",
        readOnly: true
      },
      {
        attribute: "secret",
        label: "Secret",
        readOnly: true,
        tip: "The client secret must be kept confidential. If a deployed app cannot keep the secret confidential, such as Javascript or native apps, then the secret is not used.",
        component: vp
      }
    ].concat(att);


    return util.rf({
      displayName: "Client Form",

      propTypes: {
        allFields: rpt.bool
      },

      getDefaultProps: function () {
        return {
          allFields: false
        };
      },

      submit: function () {
        this.refs.fm.submit();
      },

      render: function () {
        return fm({
          ref: "fm",
          model: this.props.model,
          onSubmit: this.props.onSubmit,
          attributes: (this.props.allFields) ? moreAtt : att
        })
      }
    });
  });