/**
 *
 */
define([ "react", "util", "underscore", "rbs/components/layout/Icon", "rbs/components/layout/Tip" ],
  function (React, util, _, icon, tip) {
    "use strict";

    var rpt = React.PropTypes;
    var d = React.DOM;

    return util.rf({
      propTypes: {
        method: rpt.string.isRequired,
        endpoint: rpt.string.isRequired,
        parameters: rpt.arrayOf(
          rpt.shape({
            req: rpt.bool.isRequired,
            name: rpt.string.isRequired,
            type: rpt.string.isRequired,
            loc: rpt.string.isRequired,
            desc: rpt.string.isRequired,
            opts: rpt.arrayOf(rpt.string),
            value: rpt.string
          }).isRequired
        ),
        contentType: rpt.oneOf([ "application/x-www-form-urlencoded", "application/json" ])
      },

      getDefaultProps: function () {
        return {
          contentType: "application/x-www-form-urlencoded"
        };
      },

      getInitialState: function () {
        return {
          parameters: {}
        };
      },

      handleChange: function (paramName, event) {
        var val = event.target.value;
        if (typeof val !== "string" || val.trim().length === 0) {
          val = null;
        }
        var newVal = {};
        newVal[ paramName ] = val;
        var newParams = _.extend({}, this.state.parameters, newVal);
        this.setState({
          parameters: newParams
        });
      },

      getInput: function (oneP) {
        var val = (typeof oneP.value === "undefined" ? this.state.parameters[ oneP.name ] : oneP.value),
          ro = typeof oneP.value !== "undefined";
        if (_.isArray(oneP.opts)) {
          return d.select({
            className: "form-control input-sm",
            readOnly: ro,
            value: val,
            onChange: _.bind(this.handleChange, this, oneP.name)
          }, _.map(oneP.opts, function (o) {
            return d.option({ key: o, value: o }, o);
          }));
        }
        return d.input({
          type: "text",
          placeholder: oneP.name,
          className: "form-control input-sm",
          readOnly: (typeof oneP.value !== "undefined"),
          value: (typeof oneP.value === "undefined" ? this.state.parameters[ oneP.name ] : oneP.value),
          onChange: _.bind(this.handleChange, this, oneP.name)
        });
      },

      getParameterTable: function () {
        return d.div({ key: "d" }, [
          d.h5({ key: "hdr" }, "Parameters"),
          d.table({ key: "t", className: "table table-bordered table-responsive-horizontal" }, [
            d.thead({ key: "th" },
              d.tr({}, [
                d.td({ key: "r" }, tip({ tip: "Whether the parameter is required as part of a valid request" }, "Required")),
                d.td({ key: "1" }, tip({ tip: "The expected name of the parameter" }, "Name")),
                d.td({ key: "2" }, tip({ tip: "The data type of the parameter" }, "Type")),
                d.td({ key: "val" }, tip({ tip: "Enter a value to see an example request" }, "Value")),
                d.td({ key: "4" }, tip({ tip: "Where the parameter should be found in the request" }, "Location")),
                d.td({ key: "3" }, tip({ tip: "What the parameter does." }, "Description"))
              ])
            ),
            d.tbody({ key: "tb" },
              _.map(this.props.parameters, function (oneP) {
                return d.tr({ key: oneP.name }, [
                  d.td({ key: "r", "data-title": "Required" }, oneP.req ? "Yes" : "No"),
                  d.td({ key: "1", "data-title": "Name" }, oneP.name),
                  d.td({ key: "2", "data-title": "Type" }, oneP.type),
                  d.td({ key: "val", "data-title": "Value" }, d.span({}, this.getInput(oneP))),
                  d.td({ key: "3", "data-title": "Location" }, oneP.loc),
                  d.td({ key: "4", "data-title": "Description" }, oneP.desc)
                ])
              }, this)
            )
          ])
        ]);
      },

      getParamObj: function (location) {
        location = location.toLowerCase();
        var dataObj = {};
        _.each(this.props.parameters, function (oneP) {
          if ((oneP.req || oneP.value || this.state.parameters[ oneP.name ]) && oneP.loc.toLowerCase() === location) {
            var val = typeof oneP.value !== "undefined" ? oneP.value : this.state.parameters[ oneP.name ];
            if (typeof val !== "string") {
              val = "";
            }
            dataObj[ oneP.name ] = val;
          }
        }, this);
        return dataObj;
      },

      getBody: function () {
        var dataObj = this.getParamObj("body");
        if (_.keys(dataObj).length > 0) {
          var toReturn = "-d '";
          if (this.props.contentType === "application/json") {
            toReturn += JSON.stringify(dataObj);
          } else {
            toReturn += $.param(dataObj, true);
          }
          toReturn += "'";
          return toReturn;
        }
        return "";
      },

      getHeaders: function () {
        var headerObj = this.getParamObj("header");
        if (_.keys(headerObj).length > 0) {
          var toReturn = [];
          _.each(headerObj, function (value, key, list) {
            toReturn.push("-H '" + key + ": " + value + "'");
          });
          return toReturn.join(" ");
        }
        return "";
      },

      getEndpoint: function () {
        var paramObj = this.getParamObj("query");
        return this.props.endpoint + (_.keys(paramObj).length > 0 ? ("?" + $.param(paramObj, true)) : "");
      },

      render: function () {
        return d.div({ className: "card " }, [
          d.div({ key: "ep", className: "well well-sm nowrap-scroll" }, [
            "curl",
            " ",
            (this.props.method !== "GET" ? d.strong({ key: "m" }, [ " -X ", this.props.method ]) : ""),
            " ",
            this.getEndpoint(),
            " ",
            this.getBody(),
            " ",
            this.getHeaders()
          ]),
          this.getParameterTable()
        ])
      }
    });
  });