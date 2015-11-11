/**
 *
 */
define([ "react", "util", "rbs/components/mixins/Model", "rbs/components/controls/Button" ],
  function (React, util, model, btn) {
    "use strict";


    var d = React.DOM;
    var rpt = React.PropTypes;

    return util.rf({
      displayName: "password with view",
      mixins: [ model ],
      getInitialState: function () {
        return {
          visible: false
        };
      },

      show: function () {
        this.setState({
          visible: true
        });
      },

      hide: function () {
        this.setState({
          visible: false
        });
      },

      render: function () {
        return d.div({
          className: "position-relative"
        }, [
          d.input(_.extend({ type: this.state.visible ? "text" : "password", key: "input" }, this.props), null),
          btn({
            key: "b",
            caption: (this.state.visible) ? "Hide" : "Show",
            icon: "key",
            onClick: (this.state.visible) ? this.hide : this.show,
            size: "xs",
            className: "right-absolute"
          })
        ]);
      }
    });

  });