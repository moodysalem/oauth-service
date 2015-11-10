/**
 * view scopes for an application
 */
define([ "react", "util", "rbs/components/layout/Alert", "js/Models", "rbs/components/combo/Table", "js/views/Loading",
    "rbs/components/layout/Icon", "rbs/components/mixins/Model", "rbs/components/controls/Pagination", "rbs/components/layout/Dropdown",
    "rbs/components/layout/DropdownItem", "rbs/components/controls/Button", "rbs/components/layout/Modal", "./ScopeForm",
    "rbs/components/collection/Alerts" ],
  function (React, util, alert, mdls, table, lw, icon, model, pag, dd, di, btn, modal, sf, alerts) {
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
        attribute: "thumbnail",
        label: "Thumbnail",
        component: util.rf({
          render: function () {
            return d.span({ className: "text-center" }, d.img({
              src: this.props.value,
              className: "scope-thumbnail"
            }));
          }
        })
      },
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
        className: "text-center",
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
              size: "sm"
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
        var app = new mdls.Application({ id: this.props.applicationId });
        return {
          scopes: new mdls.Scopes().setParam("applicationId", this.props.applicationId),
          app: app,
          createOpen: false,
          scope: new mdls.Scope()
        };
      },

      componentDidMount: function () {
        this.state.scopes.fetch();
        this.state.app.fetch();
      },

      closeCreate: function () {
        this.setState({
          createOpen: false
        });
      },

      render: function () {
        return d.div({ className: "container" }, [
          scopesHeader({
            key: "sh", model: this.state.app, onCreate: _.bind(function () {
              this.state.scope.clear();
              this.state.scope.set({ application: { id: +this.props.applicationId, version: 0 } });
              this.setState({
                createOpen: true
              });
            }, this)
          }),
          lw({
            key: "t",
            watch: this.state.scopes
          }, [
            table({
              className: "vertical-align-middle",
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
          ]),
          modal({
            key: "modal",
            open: this.state.createOpen,
            title: "Add Scope",
            onClose: this.closeCreate
          }, [
            d.div({
              key: "mb",
              className: "modal-body"
            }, [
              sf({
                key: "sf",
                ref: "sf",
                onSubmit: _.bind(function () {
                  this.state.scope.save().then(_.bind(function (model) {
                    this.closeCreate();
                    this.state.scopes.add(model);
                  }, this));
                }, this),
                model: this.state.scope
              }),
              alerts({
                watch: this.state.scope,
                key: "alts",
                showSuccess: false
              })
            ]),
            d.div({
              key: "mf",
              className: "modal-footer"
            }, [
              btn({
                key: "cancel",
                ajax: true,
                icon: "cancel",
                onClick: this.closeCreate,
                caption: "Cancel"
              }),
              btn({
                key: "save",
                icon: "plus",
                ajax: true,
                type: "success",
                onClick: _.bind(function () {
                  this.refs.sf.submit();
                }, this),
                caption: "Add"
              })
            ])
          ])
        ]);
      }
    });
  });