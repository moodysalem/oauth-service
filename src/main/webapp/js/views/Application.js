/**
 *
 */
define([ "react", "util", "./ApplicationForm", "js/Models", "rbs/components/controls/Button", "model",
    "rbs/components/mixins/Events", "router", "rbs/components/layout/Modal", "rbs/components/layout/Alert" ],
  function (React, util, af, mdls, btn, m, events, r, modal, alt) {
    "use strict";

    var d = React.DOM;

    return util.rf({
      displayName: "app",

      mixins: [ events ],

      getInitialState: function () {
        return {
          app: (new mdls.Application({
            id: this.props.id === "create" ? null : this.props.id
          })),
          deleteModalOpen: false
        };
      },

      componentDidMount: function () {
        this.listenTo(this.state.app, "sync", this.showClientsButton);
        if (this.props.id !== "create") {
          this.state.app.fetch();
        } else {
          this.listenTo(this.state.app, "sync", function (model) {
            r.navigate(util.path("applications", model.get("id")), { trigger: true })
          });
        }
      },

      closeDeleteModal: function () {
        if (this.isMounted()) {
          this.setState({
            deleteModalOpen: false
          });
        }
      },
      render: function () {
        var dn = (this.props.id === "create") ? "New Application" : "Edit Application";
        var isNew = this.props.id === "create";

        return d.div({ className: "container" }, [
          d.h2({
            key: "h2",
            className: "page-header text-center"
          }, [
            btn({
              key: "back",
              type: "primary",
              caption: "Back",
              className: "pull-left",
              href: "applications",
              icon: "long-arrow-left"
            }),
            isNew ? null : btn({
              key: "b",
              icon: "edit",
              caption: "Clients",
              type: "warning",
              className: "pull-right",
              href: util.path("applications", this.props.id, "clients")
            }),
            dn
          ]),
          af({
            key: "af",
            ref: "af",
            model: this.state.app,
            onSubmit: _.bind(function () {
              this.state.app.save();
            }, this)
          }),
          d.div({
            key: "btnrow",
            className: "row"
          }, [
            d.div({ className: "col-xs-6", key: "1" }, btn({
              disabled: isNew,
              caption: "Delete",
              type: "danger",
              icon: "trash",
              ajax: true,
              block: true,
              onClick: _.bind(function () {
                this.setState({
                  deleteModalOpen: true
                });
              }, this)
            })),
            d.div({ className: "col-xs-6", key: "2" }, btn({
              caption: isNew ? "Create" : "Save",
              type: "success",
              ajax: true,
              icon: "save",
              block: true,
              onClick: _.bind(function () {
                this.refs.af.submit();
              }, this)
            }))
          ]),
          modal({
            key: "dm",
            open: this.state.deleteModalOpen,
            title: "Delete Application",
            onClose: this.closeDeleteModal
          }, [
            d.div({ className: "modal-body", key: "mb" }, [
              alt({
                key: "p",
                level: "danger",
                strong: "Warning!",
                message: "Please confirm that you would like to delete this application. This cannot be undone."
              })
            ]),
            d.div({ className: "modal-footer", key: "mf" }, [
              btn({
                key: "cancel",
                caption: "Cancel",
                icon: "cancel",
                ajax: true,
                onClick: this.closeDeleteModal
              }),
              btn({
                key: "del",
                caption: "Confirm Delete",
                type: "danger",
                ajax: true,
                icon: "trash",
                onClick: _.bind(function () {
                  this.state.app.destroy().then(function () {
                    r.navigate("applications", { trigger: true });
                  });
                }, this)
              })
            ])
          ])
        ]);
      }
    });
  });