/**
 *
 */
define([ "react", "util", "./ApplicationForm", "js/Models" ], function (React, util, af, mdls) {
  "use strict";

  var d = React.DOM;

  return util.rf({
    displayName: "app",

    getInitialState: function () {
      return {
        app: (new mdls.Application({ id: this.props.id === "create" ? null : this.props.id }))
      };
    },

    componentDidMount: function () {
      if (this.props.id !== "create") {
        this.state.app.fetch();
      }
    },

    render: function () {
      var dn = (this.props.id === "create") ? "New Application" : "Edit Application";

      return d.div({ className: "container" }, [
        d.h2({
          key: "h2",
          className: "page-header"
        }, dn),
        af({
          key: "af",
          model: this.state.app
        })
      ]);
    }
  });
});