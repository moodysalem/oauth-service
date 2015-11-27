/**
 * view scopes for an application
 */
define([ "react", "rbs", "underscore", "rbs/components/layout/Alert", "js/Models", "rbs/components/combo/Table", "js/views/Loading",
    "rbs/components/layout/Icon", "rbs/mixins/Model", "rbs/components/controls/Pagination",
    "rbs/components/controls/Button", "rbs/components/layout/Modal", "./ScopeForm",
    "rbs/components/collection/Alerts", "./AppHeader", "./ConfirmDeleteModal", "./MustBeLoggedIn", "model" ],
  function (React, rbs, _, alert, mdls, table, lw, icon, model, pag, btn, modal, sf, alerts, ah, delModal, mbli, m) {
    "use strict";

    var util = rbs.util;
    var rpt = React.PropTypes;
    var d = React.DOM;

    var ta = [
      {
        attribute: "thumbnail",
        label: "Thumbnail",
        component: util.rf({
          render: function () {
            if (this.props.value === null) {
              return null;
            }
            return d.span({ className: "text-center" }, d.img({
              src: this.props.value,
              className: "scope-thumbnail"
            }));
          }
        })
      },
      {
        attribute: "name",
        label: "Name",
        sortOn: "name",
        component: d.span
      },
      {
        attribute: "displayName",
        label: "Display Name",
        sortOn: "displayName",
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
            return "Yes";
          }
          return "No";
        }
      },
      {
        component: util.rf({
          displayName: "actions for scope",

          getInitialState: function () {
            return {
              editOpen: false,
              deleteOpen: false,
              modelCopy: new mdls.Scope()
            };
          },

          closeEdit: function () {
            this.setState({
              editOpen: false
            });
          },

          openEdit: function () {
            this.state.modelCopy.set(this.props.model.toJSON());
            this.setState({
              editOpen: true
            });
          },

          openDelete: function () {
            this.setState({
              deleteOpen: true
            });
          },

          closeDelete: function () {
            this.setState({
              deleteOpen: false
            });
          },

          mixins: [ model ],

          render: function () {
            return d.div({}, [
              d.div({
                key: "dd",
                className: "text-center btn-container"
              }, [
                btn({
                  key: "edit",
                  type: "warning",
                  size: "xs",
                  caption: "Edit",
                  icon: "pencil",
                  onClick: this.openEdit
                }),
                btn({
                  key: "del",
                  size: "xs",
                  caption: "Delete",
                  type: "danger",
                  icon: "trash",
                  onClick: this.openDelete
                })
              ]),
              delModal({
                key: "delModal",
                open: this.state.deleteOpen,
                onClose: this.closeDelete,
                title: "Delete Scope: " + this.state.model.name,
                deleteMessage: "This will remove the scope from all clients and tokens. This operation cannot be undone.",
                onDelete: _.bind(function () {
                  this.props.model.destroy({ wait: true });
                }, this)
              }),
              modal({
                key: "modal",
                open: this.state.editOpen,
                title: "Edit Scope",
                onClose: this.closeEdit
              }, [
                d.div({
                  key: "mb",
                  className: "modal-body"
                }, [
                  sf({
                    key: "sf",
                    ref: "sf",
                    onSubmit: _.bind(function () {
                      this.state.modelCopy.save().then(_.bind(function (model) {
                        this.props.model.set(model);
                        this.closeEdit();
                      }, this));
                    }, this),
                    model: this.state.modelCopy
                  }),
                  alerts({
                    watch: this.state.modelCopy,
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
                    onClick: this.closeEdit,
                    caption: "Cancel"
                  }),
                  btn({
                    key: "save",
                    icon: "save",
                    ajax: true,
                    type: "success",
                    onClick: _.bind(function () {
                      this.refs.sf.submit();
                    }, this),
                    caption: "Save"
                  })
                ])
              ])
            ]);
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
        if (m.isLoggedIn()) {
          this.state.scopes.fetch();
          this.state.app.fetch();
        }
      },

      closeCreate: function () {
        this.setState({
          createOpen: false
        });
      },

      render: function () {
        if (!m.isLoggedIn()) {
          return mbli();
        }
        return d.div({ className: "container" }, [
          ah({
            title: "Scopes",
            key: "sh",
            model: this.state.app,
            onCreate: _.bind(function () {
              this.state.scope.clear();
              this.state.scope.set({ application: { id: +this.props.applicationId } });
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