/**
 * view scopes for an application
 */
define([ "react", "util", "rbs/components/layout/Alert", "js/Models", "rbs/components/combo/Table", "js/views/Loading",
    "rbs/components/layout/Icon" ],
  function (React, util, alert, mdls, table, lw, icon) {
    "use strict";

    var rpt = React.PropTypes;
    var d = React.DOM;

    var ta = [
      {
        attribute: "displayName",
        label: "Display Name",
        sortOn: "displayName",
        component: d.span
      },
      {
        attribute: "name",
        label: "Name",
        sortOn: "name",
        component: d.span
      },
      {
        attribute: "description",
        label: "Description",
        component: d.span
      },
      {
        attribute: "requiresApprovalFromApplication",
        sortOn: "requiresApprovalFromApplication",
        label: "Requires Approval",
        component: d.span,
        formatFunction: function (val) {
          if (val === true) {
            return icon({ name: "check" });
          }
        }
      }
    ];

    return util.rf({
      propTypes: {
        applicationId: rpt.string
      },

      getInitialState: function () {
        return {
          scopes: new mdls.Scopes().setParam("applicationId", this.props.applicationId),
          app: new mdls.Application({ id: this.props.applicationId })
        };
      },

      componentDidMount: function () {
        this.state.scopes.fetch();
        this.state.app.fetch();
      },

      render: function () {
        return d.div({ className: "container" }, [
          d.h2({ key: "h", className: "page-header" }, "Scopes"),
          lw({
            key: "t",
            watch: this.state.scopes
          }, table({
            collection: this.state.scopes,
            attributes: ta
          }))
        ]);
      }
    });
  });