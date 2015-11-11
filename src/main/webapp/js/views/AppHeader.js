/**
 *
 */
define([ "react", "util", "rbs/components/mixins/Model", "rbs/components/controls/Button" ],
  function (React, util, model, btn) {
    "use strict";

    var d = React.DOM;

    return util.rf({
      displayName: "Scopes Header",
      mixins: [ model ],

      render: function () {
        var dn = this.state.model.name ? (" for " + this.state.model.name) : "";
        return d.div({}, [
          d.h2({ key: "h", className: "page-header" }, [
            btn({
              key: "btn",
              className: "pull-right",
              caption: "Create",
              icon: "plus",
              type: "success",
              onClick: this.props.onCreate
            }),
            this.props.title,
            d.small({ key: "s" }, dn)
          ])
        ]);
      }
    });
  });