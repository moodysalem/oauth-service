/**
 *
 */
define([ "react", "util", "rbs/components/model/Form" ], function (React, util, fm) {
  "use strict";

  var d = React.DOM;
  var rpt = React.PropTypes;

  var att = [
    {
      attribute: "name",
      label: "Name",
      component: "text"
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