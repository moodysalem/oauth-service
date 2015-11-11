/**
 *
 */
define([ "react", "util", "rbs/components/model/Form" ], function (React, util, form) {
  "use strict";

  var d = React.DOM;
  var pt = React.PropTypes;

  var fa = [
    {
      attribute: "name",
      label: "Name",
      placeholder: "Name",
      tip: "Programmatic name for the scope. May only include alphanumeric characters and underscores.",
      component: "text",
      required: true
    },
    {
      attribute: "thumbnail",
      label: "Thumbnail",
      placeholder: "Thumbnail URL",
      tip: "Enter the URL for the thumbnail.",
      component: "text"
    },
    {
      attribute: "displayName",
      label: "Display Name",
      placeholder: "Display Name",
      tip: "How the scope should appear to users.",
      component: "text",
      required: true
    },
    {
      attribute: "description",
      label: "Description",
      placeholder: "Scope Description",
      tip: "Enter a short description of the API that this scope provides access to.",
      component: "textarea",
      required: true
    },
    {
      attribute: "requiresApprovalFromApplication",
      label: "Client Approval Required",
      tip: "Whether clients that register themselves can request this scope from the user without approval from the application owner.",
      component: "checkbox"
    }
  ];

  return util.rf({
    displayName: "Scope Form",

    propTypes: {
      model: pt.object.isRequired
    },

    submit: function () {
      this.refs.form.submit();
    },

    render: function () {
      return form({
        ref: "form",
        model: this.props.model,
        attributes: fa,
        onSubmit: this.props.onSubmit
      })
    }
  });
});