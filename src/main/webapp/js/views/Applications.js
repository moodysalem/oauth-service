/**
 *
 */
define([ "react", "util", "js/Models", "model", "rbs/components/combo/Table", "rbs/components/controls/Button",
    "./Loading", "rbs/components/layout/Modal", "rbs/components/mixins/Model", "rbs/components/layout/Dropdown",
    "rbs/components/model/Form", "rbs/components/collection/Alerts", "router", "rbs/components/layout/DropdownItem" ],
  function (React, util, mdls, m, table, btn, lw, modal, model, dd, form, alerts, r, di) {
    "use strict";

    var d = React.DOM;

    var appTableColumns = [
      {
        label: "ID",
        attribute: "id",
        sortOn: "id",
        component: d.span
      },
      {
        label: "Name",
        sortOn: "name",
        component: util.rf({
          mixins: [ model ],
          render: function () {
            return d.a({ href: util.path("applications", this.state.model.id) }, this.state.model.name);
          }
        })
      },
      {
        label: "Support E-mail",
        attribute: "supportEmail",
        sortOn: "supportEmail",
        component: d.span
      },
      {
        key: "btn",
        component: util.rf({
          mixins: [ model ],
          render: function () {
            return d.div({ className: "pull-right" }, dd({
              caption: "Actions",
              type: "primary",
              size: "xs",
              icon: "tachometer",
              right: true
            }, [
              di({
                key: "edit",
                caption: "Edit",
                icon: "pencil",
                href: util.path("applications", this.state.model.id)
              }),
              di({
                key: "scp",
                caption: "Scopes",
                icon: "book",
                href: util.path("applications", this.state.model.id, "scopes")
              }),
              di({
                key: "mc",
                caption: "Manage Clients",
                icon: "gavel",
                href: util.path("applications", this.state.model.id, "clients")
              })
            ]))
          }
        })
      }
    ];

    var fA = [
      {
        attribute: "name",
        label: "Name",
        tip: "Enter the name to display for this application.",
        component: "text",
        required: true
      },
      {
        attribute: "supportEmail",
        label: "Support E-mail",
        tip: "Enter the e-mail address to which support e-mails should be sent.",
        component: "email",
        required: true
      }
    ];

    return util.rf({
      displayName: "apps",

      getInitialState: function () {
        return {
          apps: new mdls.Applications(),
          app: new mdls.Application(),
          createOpen: false
        };
      },

      componentDidMount: function () {
        this.state.apps.fetch();
      },

      closeCreate: function () {
        if (this.isMounted()) {
          this.setState({
            createOpen: false
          });
        }
      },

      render: function () {
        return d.div({
          className: "container"
        }, [
          d.h2({ className: "page-header", key: "p" }, [
            btn({
              key: "btn",
              caption: "Create",
              type: "success",
              className: "pull-right",
              icon: "plus",
              onClick: _.bind(function () {
                this.state.app.clear();
                this.setState({
                  createOpen: true
                });
              }, this)
            }),
            "My Applications"
          ]),
          lw({
            key: "t",
            watch: this.state.apps
          }, table({
            collection: this.state.apps,
            attributes: appTableColumns
          })),

          modal({
            key: "ca",
            open: this.state.createOpen,
            title: "Create New Application",
            onClose: this.closeCreate
          }, [
            d.div({ className: "modal-body", key: "mb" }, [
              form({
                key: "f",
                ref: "_f",
                attributes: fA,
                model: this.state.app,
                onSubmit: _.bind(function () {
                  this.state.app.save().then(function (mdl) {
                    r.navigate(util.path("applications", mdl.id), { trigger: true });
                  });
                }, this)
              }),
              alerts({
                key: "alerts",
                watch: this.state.app,
                showSuccess: false
              })
            ]),
            d.div({ className: "modal-footer", key: "mf" }, [
              btn({
                key: "cancel",
                caption: "Cancel",
                icon: "cancel",
                ajax: true,
                onClick: this.closeCreate
              }),
              btn({
                key: "cr",
                caption: "Create",
                type: "success",
                ajax: true,
                icon: "plus",
                onClick: _.bind(function () {
                  this.refs._f.submit();
                }, this)
              })
            ])
          ])
        ]);
      }
    });
  });