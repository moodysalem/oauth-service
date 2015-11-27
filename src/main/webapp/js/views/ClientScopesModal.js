/**
 *
 */
define([ "react", "rbs", "underscore", "./Loading", "rbs/components/collection/Alerts", "js/Models", "rbs/components/combo/Table",
    "rbs/components/controls/Button", "rbs/components/layout/Modal", "rbs/mixins/Model" ],
  function (React, rbs, _, lw, alerts, mdls, table, btn, modal, model) {
    "use strict";

    var util = rbs.util;

    var d = React.DOM;
    var rpt = React.PropTypes;

    var nameComp = util.rf({
      displayName: "name component",
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
        component: util.rf({
          displayName: "delete button",
          getInitialState: function () {
            return {
              confirm: false
            };
          },

          cancel: function () {
            this.setState({
              confirm: false
            });
          },
          confirm: function () {
            this.setState({
              confirm: true
            });
          },

          getButtons: function () {
            if (this.state.confirm) {
              return [
                btn({
                  key: "cancel",
                  caption: "Cancel",
                  size: "xs",
                  ajax: true,
                  onClick: this.cancel
                }),
                btn({
                  key: "btn",
                  type: "danger",
                  caption: "Confirm",
                  icon: "ban",
                  size: "xs",
                  ajax: true,
                  onClick: _.bind(function () {
                    this.props.model.destroy({ wait: true });
                  }, this)
                })
              ];
            } else {
              return [
                btn({
                  key: "btn",
                  type: "danger",
                  caption: "Delete",
                  icon: "ban",
                  size: "xs",
                  ajax: true,
                  onClick: this.confirm
                })
              ];
            }
          },

          render: function () {
            return d.div({ className: "text-center" }, d.div({ className: "btn-group" }, this.getButtons()));
          }
        })
      }
    ];


    return util.rf({
      displayName: "client scopes modal",

      propTypes: {
        // used for fetching the scopes that are currently assigned
        clientId: rpt.number.isRequired,
        // used for fetching the scopes that can be assigned
        applicationId: rpt.number.isRequired
      },

      getDefaultProps: function () {
        return {};
      },

      getInitialState: function () {
        var scopes = new mdls.Scopes().setParam("applicationId");
        return {
          clientScopes: new mdls.ClientScopes(),
          scopes: scopes,
          attributes: [
            {
              attribute: "scope",
              label: "Scope",
              searchOn: "name",
              component: "select",
              className: "form-control input-sm",
              collection: scopes,
              modelComponent: nameComp
            }
          ].concat(scopeAttributes)
        };
      },

      componentDidUpdate: function (prevProps) {
        this.state.clientScopes.setParam("clientId", this.props.clientId);
        this.state.scopes.setParam("applicationId", this.props.applicationId);
        if (!prevProps.open && this.props.open) {
          this.state.clientScopes.reset();
          this.state.clientScopes.fetch();
          this.state.scopes.reset();
          this.state.scopes.fetch();
        }
      },

      render: function () {
        return modal(_.extend({}, this.props, {}), [
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
              onClick: this.props.onClose,
              caption: "Done"
            }),
            btn({
              key: "add",
              icon: "plus",
              ajax: true,
              type: "primary",
              caption: "Add",
              onClick: _.bind(function () {
                this.state.clientScopes.add({ client: { id: this.props.clientId } });
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
        ]);
      }
    });
  });