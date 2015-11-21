/**
 * view scopes for an application
 */
define([ "underscore", "react", "util", "rbs/components/combo/Table", "js/Models", "./Loading", "rbs/components/controls/Pagination",
    "./AppHeader", "rbs/components/layout/Modal", "rbs/components/collection/Alerts", "rbs/components/controls/Button",
    "./ClientForm", "rbs/components/mixins/Model", "rbs/components/model/GridRow", "rbs/components/mixins/Events",
    "./ConfirmDeleteModal", "model", "./MustBeLoggedIn" ],
  function (_, React, util, table, mdls, lw, pag, ah, modal, alerts, btn, cf, model, row, events, delModal, m, mbli) {
    "use strict";

    var rpt = React.PropTypes;
    var d = React.DOM;

    var nameComp = util.rf({
      mixins: [ model ],
      render: function () {
        return d.div({}, this.state.model.name);
      }
    });

    var scopeAttributes = [
      {
        attribute: "priority",
        valueAttribute: "name",
        searchOn: "name",
        label: "Priority",
        component: "select",
        className: "form-control input-sm",
        collection: mdls.ClientScopePriorities,
        modelComponent: nameComp
      },
      {
        attribute: "reason",
        label: "Reason",
        component: "textarea",
        className: "form-control input-sm",
        placeholder: "Reason for scope"
      },
      {
        attribute: "approved",
        label: "Approved",
        component: "checkbox"
      },
      {
        component: btn,
        type: "danger",
        icon: "ban",
        size: "xs",
        ajax: true,
        onClick: function () {
          this.props.model.destroy({ wait: true });
        }
      }
    ];

    var ta = [
      {
        attribute: "name",
        sortOn: "name",
        label: "Name",
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
          displayName: "actions for client",

          mixins: [ model ],

          getInitialState: function () {
            var scopes = (new mdls.Scopes()).setPageSize(1000);
            return {
              editOpen: false,
              modelCopy: new mdls.Client(),
              scopesOpen: false,
              clientScopes: new mdls.ClientScopes(),
              scopes: scopes,
              attributes: this.getTableAttributes(scopes),
              deleteOpen: false
            };
          },

          getTableAttributes: function (scopes) {
            return [
              {
                attribute: "scope",
                label: "Scope",
                searchOn: "name",
                component: "select",
                className: "form-control input-sm",
                collection: scopes,
                modelComponent: nameComp
              }
            ].concat(scopeAttributes);
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
            this.state.clientScopes.reset();
            this.state.scopes.setParam("applicationId", this.state.model.application.id);
            if (this.isMounted()) {
              this.setState({
                scopesOpen: true
              }, function () {
                this.state.clientScopes.setParam("clientId", this.state.model.id);
                this.state.clientScopes.fetch();
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
                  key: "scopes",
                  size: "xs",
                  caption: "Scopes",
                  type: "primary",
                  icon: "book",
                  onClick: this.openScopes
                }),
                btn({
                  key: "del",
                  size: "xs",
                  type: "danger",
                  caption: "Delete",
                  icon: "trash",
                  onClick: this.openDelete
                })
              ]),
              delModal({
                key: "delmodal",
                title: "Delete Client: " + this.state.model.name,
                open: this.state.deleteOpen,
                onDelete: _.bind(function () {
                  this.props.model.destroy({ wait: true });
                }, this),
                deleteMessage: "This operation cannot be undone.",
                onClose: this.closeDelete
              }),
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
                    watch: [ this.state.clientScopes, this.state.scopes ]
                  }, table({
                    collection: this.state.clientScopes,
                    attributes: this.state.attributes
                  })),
                  alerts({
                    watch: this.state.clientScopes,
                    key: "alts",
                    showSuccess: false
                  })
                ]),
                d.div({
                  key: "mf",
                  className: "modal-footer"
                }, [
                  btn({
                    key: "done",
                    ajax: true,
                    icon: "Done",
                    onClick: this.closeScopes,
                    caption: "Done"
                  }),
                  btn({
                    key: "add",
                    icon: "plus",
                    ajax: true,
                    type: "primary",
                    caption: "Add",
                    onClick: _.bind(function () {
                      this.state.clientScopes.add({ client: { id: this.state.model.id } });
                    }, this)
                  }),
                  btn({
                    key: "save",
                    icon: "save",
                    ajax: true,
                    type: "success",
                    onClick: _.bind(function () {
                      this.state.clientScopes.save();
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
          client: (new mdls.Client()),
          search: "",
          lastSearch: ""
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
        if (m.isLoggedIn()) {
          this.state.app.fetch();
          this.state.clients.fetch();
        }
      },

      searchChange: function (e) {
        var v = e.target.value;
        this.setState({
          search: v
        });
      },

      handleEnter: function (e) {
        if (e.keyCode === 13) {
          this.search();
          this.refs.search.blur();
        }
      },

      search: function () {
        if (this.state.search === this.state.lastSearch) {
          return;
        }
        this.state.clients.setPageNo(0).setParam("search", this.state.search).fetch();
        this.setState({
          lastSearch: this.state.search
        });
      },

      render: function () {
        if (!m.isLoggedIn()) {
          return mbli();
        }
        return d.div({ className: "container" }, [
          ah({
            key: "h",
            title: "Clients",
            model: this.state.app,
            onCreate: this.openCreate
          }),
          d.div({
            key: "search",
            className: "row"
          }, [
            d.div({ key: "search", className: "col-sm-8" }, d.div({ className: "form-group" }, d.input({
              type: "text",
              value: this.state.search,
              ref: "search",
              placeholder: "Search Text",
              className: "form-control",
              onChange: this.searchChange,
              onKeyDown: this.handleEnter
            }))),
            d.div({ key: "btn", className: "col-sm-4" }, d.div({ className: "form-group" }, btn({
              block: true, ajax: true,
              caption: "Search",
              icon: "search",
              disabled: this.state.search === this.state.lastSearch,
              onClick: _.bind(this.search, this)
            })))
          ]),
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