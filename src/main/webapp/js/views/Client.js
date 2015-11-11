/**
 *
 */
define([ "react", "util", "js/Models", "rbs/components/combo/Table", "rbs/components/mixins/Model", "./ClientForm",
    "./Loading", "rbs/components/collection/Alerts", "rbs/components/controls/Button", "router" ],
  function (React, util, mdls, table, model, cf, lw, alerts, btn, r) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    var ta = [
      {
        attribute: "scope.name",
        sortOn: "scope.name",
        label: "Scope",
        component: d.span
      },
      {
        attribute: "priority",
        sortOn: "priority",
        label: "Priority",
        component: d.span
      },
      {
        attribute: "reason",
        sortOn: "reason",
        label: "Reason",
        component: d.span
      },
      {
        attribute: "approved",
        sortOn: "approved",
        label: "Approved",
        component: d.span
      }
    ];

    return util.rf({
      displayName: "single client",

      getInitialState: function () {
        return {
          client: new mdls.Client({ id: this.props.id }),
          cs: new mdls.ClientScopes().setParam("clientId", this.props.id)
        };
      },

      componentDidMount: function () {
        this.state.client.fetch();
        this.state.cs.fetch();
      },

      render: function () {
        return d.div({
          className: "container"
        }, [
          d.h2({ className: "page-header", key: "cd" }, "Edit Client"),
          lw({ key: "cf", watch: this.state.client }, cf({
            key: "cf",
            ref: "cf",
            model: this.state.client,
            allFields: true,
            onSubmit: _.bind(function () {
              this.state.client.save();
            }, this)
          })),
          alerts({
            key: "al",
            watch: this.state.client,
            showSuccess: false
          }),
          d.div({
            key: "btns",
            className: "row"
          }, [
            d.div({ key: "s", className: "col-xs-6" },
              btn({
                caption: "Delete",
                type: "danger",
                block: true,
                ajax: true,
                icon: "trash",
                onClick: _.bind(function () {
                  var appId = this.state.client.get("application.id");
                  this.state.client.destroy({ wait: true }).then(function () {
                    r.navigate(util.path("applications", appId), { navigate: true });
                  });
                }, this)
              })),
            d.div({ key: "ss", className: "col-xs-6" },
              btn({
                caption: "Save",
                ajax: true,
                type: "success",
                block: true,
                icon: "save",
                onClick: _.bind(function () {
                  this.refs.cf.submit();
                }, this)
              }))
          ]),
          d.h3({ key: "h3", className: "page-header" }, "Client Scopes"),
          lw({ key: "tbl", watch: this.state.cs }, table({
            collection: this.state.cs,
            attributes: ta
          }))
        ]);
      }
    });
  });