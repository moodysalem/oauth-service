/**
 * view scopes for an application
 */
define([ "react", "util", "rbs/components/layout/Alert", "js/Models", "rbs/components/combo/Table", "js/views/Loading",
    "rbs/components/layout/Icon", "rbs/components/mixins/Model", "rbs/components/controls/Pagination", "rbs/components/layout/Dropdown",
    "rbs/components/layout/DropdownItem", "rbs/components/controls/Button" ],
  function (React, util, alert, mdls, table, lw, icon, model, pag, dd, di, btn) {
    "use strict";

    var rpt = React.PropTypes;
    var d = React.DOM;

    var scopesHeader = util.rf({
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
            "Scopes",
            d.small({ key: "s" }, dn)
          ])
        ]);
      }
    });

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
      },
      {
        component: util.rf({
          mixins: [ model ],
          render: function () {
            return d.div({ className: "pull-right" }, dd({
              caption: "Actions",
              right: true,
              icon: "ellipsis-v",
              size: "xs"
            }, [
              di({
                key: "edit",
                caption: "Edit",
                icon: "pencil",
                onClick: _.bind(function () {
                  //this.props.model.destroy({ wait: true });
                }, this)
              }),
              di({
                key: "del",
                caption: "Delete",
                icon: "trash",
                onClick: _.bind(function () {
                  this.props.model.destroy({ wait: true });
                }, this)
              })
            ]));
          }
        })
      }
    ];

    return util.rf({
      propTypes: {
        applicationId: rpt.string
      },

      getInitialState: function () {
        return {
          scopes: new mdls.Scopes().setParam("applicationId", this.props.applicationId),
          app: new mdls.Application({ id: this.props.applicationId }),
          createOpen: false,
          scope: new mdls.Scope({ application: { id: this.props.applicationId } })
        };
      },

      componentDidMount: function () {
        this.state.scopes.fetch();
        this.state.app.fetch();
      },

      render: function () {
        return d.div({ className: "container" }, [
          scopesHeader({ key: "sh", model: this.state.app }),
          lw({
            key: "t",
            watch: this.state.scopes
          }, [
            table({
              key: "scopes",
              collection: this.state.scopes,
              attributes: ta,
              onCreate: _.bind(function () {
                this.setState({
                  createOpen: true
                })
              }, this)
            }),
            d.div({ className: "text-center", key: "pag" },
              pag({
                collection: this.state.scopes
              })
            )
          ])
        ]);
      }
    });
  });