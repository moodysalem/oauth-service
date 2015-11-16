/**
 *
 */
define([ "react", "util", "jquery", "underscore" ], function (React, util, $, _) {
  "use strict";

  var d = React.DOM;
  var rpt = React.PropTypes;

  return util.rf({
    displayName: "Fix to top",

    propTypes: {
      buffer: rpt.number
    },

    getDefaultProps: function () {
      return {
        buffer: 50
      };
    },

    getInitialState: function () {
      return {
        origStyle: {},
        copyStyle: {
          display: "none"
        }
      };
    },

    componentDidMount: function () {
      this.calculateStyle();
      this.boundStyle = _.bind(this.calculateStyle, this);
      $(window).on("resize scroll", this.boundStyle);
    },

    componentWillUnmount: function () {
      $(window).off("resize scroll", this.boundStyle);
    },

    calculateStyle: function () {
      if (!this.isMounted()) {
        return;
      }
      var origStyle = {};
      var copyStyle = {};

      var w = $(window);
      var wst = w.scrollTop();
      var wht = w.height();
      var orig = $(this.refs.orig);
      var of = orig.offset();

      if ((wst > of.top - this.props.buffer) && (orig.outerHeight() + (this.props.buffer * 2) < wht)) {
        origStyle.visibility = "hidden";
        copyStyle.position = "fixed";
        copyStyle.left = of.left;
        copyStyle.top = this.props.buffer;
      } else {
        copyStyle.display = "none";
      }

      this.setState({
        origStyle: origStyle,
        copyStyle: copyStyle
      });
    },

    render: function () {
      var c = React.Children.only(this.props.children);

      var orig = React.cloneElement(c, { style: this.state.origStyle, key: "orig", ref: "orig" });
      var copy = React.cloneElement(c, { style: this.state.copyStyle, key: "copy", ref: "copy" });
      return d.span({}, [
        orig, copy
      ]);
    }
  })
    ;
});