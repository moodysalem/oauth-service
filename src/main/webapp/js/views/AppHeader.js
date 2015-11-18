/**
 *
 */
define([ "react", "util", "rbs/components/mixins/Model", "rbs/components/controls/Button" ],
  function (React, util, model, btn) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    return util.rf({
      displayName: "Scopes Header",
      mixins: [ model ],

      propTypes: {
        title: rpt.string.isRequired,
        back: rpt.bool,
        onCreate: rpt.func
      },

      getDefaultProps: function () {
        return {
          back: true,
          onCreate: null
        };
      },

      getCreateButton: function () {
        if (this.props.onCreate !== null) {
          return btn({
            key: "btn",
            className: "pull-right",
            caption: "Create",
            icon: "plus",
            type: "success",
            onClick: this.props.onCreate
          });
        }
        return null;
      },

      getBackButton: function () {
        if (this.props.back) {
          return btn({
            key: "back",
            icon: "arrow-left",
            className: "pull-left",
            caption: "Back",
            type: "primary",
            href: "applications"
          });
        }
        return null;
      },

      render: function () {
        var dn = this.state.model.name ? (" for " + this.state.model.name) : "";
        return d.div({}, [
          d.h2({ key: "h", className: "page-header text-center" }, [
            this.getBackButton(),
            this.getCreateButton(),
            this.props.title,
            d.small({ key: "s" }, dn)
          ])
        ]);
      }
    });
  });