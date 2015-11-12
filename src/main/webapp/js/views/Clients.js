/**
 * view scopes for an application
 */
define([ "react", "util", "rbs/components/combo/Table", "js/Models", "./Loading", "rbs/components/controls/Pagination",
    "./AppHeader", "rbs/components/layout/Modal", "rbs/components/collection/Alerts", "rbs/components/controls/Button",
    "./ClientForm", "rbs/components/mixins/Model", "rbs/components/layout/Dropdown", "rbs/components/layout/DropdownItem" ],
  function (React, util, table, mdls, lw, pag, ah, modal, alerts, btn, cf, model, dd, di) {
    "use strict";

    var rpt = React.PropTypes;
    var d = React.DOM;

    var scopeAttributes = [
      {
        attribute: "scope.name",
        label: "Scope",
        component: d.span
      },
      {
        attribute: "priority",
        label: "Priority",
        component: "select",
        className: "form-control",
        collection: mdls.ClientScopePriorities,
        modelComponent: util.rf({
          mixins: [ model ],
          render: function () {
            return d.div({}, this.state.model.name);
          }
        })
      },
      {
        attribute: "reason",
        label: "Reason",
        component: "textarea",
        className: "form-control",
        placeholder: "Reason for scope"
      }
    ];

    var ta = [
      {
        attribute: "name",
        sortOn: "name",
        label: "Name",
        tip: "name",
        component: d.span
      },
      {
        attribute: "type",
        sortOn: "type",
        label: "Type",
        component: d.span
      },
      {
        attribute: "tokenTtl",
        sortOn: "tokenTtl",
        label: "TTL",
        component: d.span
      },
      {
        attribute: "refreshTokenTtl",
        sortOn: "refreshTokenTtl",
        label: "Refresh TTL",
        component: d.span
      },
      {
        attribute: "flows",
        label: "Flows",
        component: d.span,
        formatFunction: function (val) {
          return val.join(", ");
        }
      },
      {
        component: util.rf({
          displayName: "dropdown client",

          mixins: [ model ],

          getInitialState: function () {
            return {
              editOpen: false,
              modelCopy: new mdls.Client(),
              scopesOpen: false,
              scopes: new mdls.ClientScopes()
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

          openScopes: function () {
            this.state.scopes.reset();
            if (this.isMounted()) {
              this.setState({
                scopesOpen: true
              }, function () {
                this.state.scopes.setParam("clientId", this.state.model.id);
                this.state.scopes.fetch();
              });
            }
          },

          closeScopes: function () {
            if (this.isMounted()) {
              this.setState({
                scopesOpen: false
              });
            }
          },


          render: function () {
            return d.div({ className: "pull-right" }, [
              dd({
                key: "dd",
                caption: "Actions",
                icon: "ellipsis-v",
                size: "sm",
                right: true
              }, [
                di({
                  key: "edit",
                  caption: "Edit",
                  icon: "pencil",
                  onClick: this.openEdit
                }),
                di({
                  key: "scopes",
                  caption: "Client Scopes",
                  icon: "book",
                  onClick: this.openScopes
                }),
                di({
                  key: "del",
                  caption: "Delete",
                  icon: "trash",
                  onClick: _.bind(function () {
                    this.props.model.destroy({ wait: true });
                  }, this)
                })
              ]),
              modal({
                key: "modal",
                open: this.state.editOpen,
                title: "Edit " + this.state.model.name,
                size: "lg",
                onClose: this.closeEdit
              }, [
                d.div({
                  key: "mb",
                  className: "modal-body"
                }, [
                  cf({
                    allFields: true,
                    key: "cf",
                    ref: "cf",
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
                      this.refs.cf.submit();
                    }, this),
                    caption: "Save"
                  })
                ])
              ]),

              modal({
                key: "scopes",
                open: this.state.scopesOpen,
                title: "Edit Scopes for " + this.state.model.name,
                size: "lg",
                onClose: this.closeScopes
              }, [
                d.div({
                  key: "mb",
                  className: "modal-body"
                }, [
                  lw({
                    key: "tbl",
                    watch: this.state.scopes
                  }, table({
                    collection: this.state.scopes,
                    attributes: scopeAttributes
                  })),
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
                    onClick: this.closeScopes,
                    caption: "Cancel"
                  }),
                  btn({
                    key: "add",
                    icon: "plus",
                    ajax: true,
                    type: "primary",
                    caption: "Add",
                    onClick: _.bind(function () {
                      alert("Not yet implemented.");
                      //this.state.scopes.add({ client: { id: this.state.model.id } });
                    }, this)
                  }),
                  btn({
                    key: "save",
                    icon: "save",
                    ajax: true,
                    type: "success",
                    onClick: _.bind(function () {
                      alert("Not yet implemented.");
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
      displayName: "clients",

      propTypes: {
        applicationId: rpt.string
      },

      getInitialState: function () {
        return {
          app: new mdls.Application({ id: this.props.applicationId }),
          clients: (new mdls.Clients()).setParam("applicationId", this.props.applicationId),
          createOpen: false,
          client: (new mdls.Client())
        };
      },

      openCreate: function () {
        this.state.client.clear();
        this.state.client.set({ application: { id: +this.props.applicationId } });
        this.setState({
          createOpen: true
        });
      },

      closeCreate: function () {
        this.setState({
          createOpen: false
        });
      },

      componentDidMount: function () {
        this.state.app.fetch();
        this.state.clients.fetch();
      },

      render: function () {
        return d.div({ className: "container" }, [
          ah({
            key: "h",
            title: "Clients",
            model: this.state.app,
            onCreate: this.openCreate
          }),
          lw({ key: "t", watch: this.state.clients }, [
              table({
                key: "table",
                className: "vertical-align-middle",
                collection: this.state.clients,
                attributes: ta
              }),
              d.div({ key: "P", className: "text-center" }, pag({
                collection: this.state.clients
              }))
            ]
          ),

          modal({
            key: "modal",
            open: this.state.createOpen,
            title: "Add Client",
            size: "lg",
            onClose: this.closeCreate
          }, [
            d.div({
              key: "mb",
              className: "modal-body"
            }, [
              cf({
                key: "cf",
                ref: "cf",
                onSubmit: _.bind(function () {
                  this.state.client.save().then(_.bind(function (model) {
                    this.closeCreate();
                    this.state.clients.add(model);
                  }, this));
                }, this),
                model: this.state.client
              }),
              alerts({
                watch: this.state.client,
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
                  this.refs.cf.submit();
                }, this),
                caption: "Add"
              })
            ])
          ])
        ]);
      }
    });
  });