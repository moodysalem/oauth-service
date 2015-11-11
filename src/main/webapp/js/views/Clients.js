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
          displayName: "dropdown client",

          getInitialState: function () {
            return {
              editOpen: false,
              modelCopy: new mdls.Client()
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

          mixins: [ model ],

          render: function () {
            return d.div({ className: "pull-right" }, [
              dd({
                key: "dd",
                caption: "Actions",
                right: true,
                icon: "ellipsis-v",
                size: "sm"
              }, [
                di({
                  key: "edit",
                  caption: "Edit",
                  icon: "pencil",
                  onClick: this.openEdit
                }),
                di({
                  key: "del",
                  caption: "Delete",
                  icon: "trash",
                  onClick: _.bind(function () {
                    this.props.model.destroy({ wait: true });
                  }, this)
                }),
                di({
                  key: "scopes",
                  caption: "Client Scopes",
                  icon: "book",
                  href: util.path("clients", this.state.model.id)
                })
              ]),
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
                  cf({
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