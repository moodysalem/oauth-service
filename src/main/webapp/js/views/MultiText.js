/**
 *
 */
define([ "react", "util", "underscore", "rbs/components/controls/Button" ],
  function (React, util, _, btn) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    var KEY_ENTER = 13;

    return util.rf({
      displayName: "multitext",

      removeValue: function (ix) {
        var val = _.clone(this.props.value);
        val.splice(ix, 1);
        this.props.onChange(val);
      },

      getInitialState: function () {
        return {
          newValue: ""
        };
      },

      setNewValue: function (e) {
        var val = e.target.value;
        this.setState({
          newValue: val
        });
      },

      addValue: function (focus, e) {
        var val = this.state.newValue;
        if (typeof val !== "string" || val.length === 0) {
          return;
        }
        this.setState({
          newValue: ""
        }, function () {
          this.props.onChange(this.props.value.concat([ val ]));
          if (focus) {
            this.refs.newValue.focus();
          }
        });
      },

      handleEnter: function (e) {
        if (e.keyCode === KEY_ENTER) {
          this.addValue(true);
        }
      },

      render: function () {
        var i = 0;
        var children = _.map(this.props.value, function (val) {
          return d.div({ key: "val-" + val, className: "position-relative" }, [
            d.input({ key: "in", type: "text", className: this.props.className, value: val, readOnly: true }),
            btn({
              key: "btn",
              className: "right-absolute",
              type: "danger",
              size: "xs",
              icon: "trash",
              onClick: _.bind(this.removeValue, this, i++)
            })
          ]);
        }, this);

        children.push(d.input({
          key: "newValue",
          ref: "newValue",
          className: this.props.className,
          type: "text",
          onChange: this.setNewValue,
          value: this.state.newValue,
          onBlur: _.bind(this.addValue, this, false),
          placeholder: this.props.placeholder,
          onKeyDown: this.handleEnter
        }));

        return d.div({}, children);
      }
    });
  });