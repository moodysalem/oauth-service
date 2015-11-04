/**
 *
 */
define([ "react", "util", "js/Models", "model", "rbs/components/combo/Table", "rbs/components/controls/Button",
    "rbs/components/controls/LoadingWrapper", "rbs/components/layout/Modal", "rbs/components/mixins/Model" ],
  function (React, util, mdls, m, table, btn, lw, modal, model) {
    "use strict";

    var d = React.DOM;

    var appTableColumns = [
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
        label: "Clients",
        key: "btn",
        component: util.rf({
          mixins: [ model ],
          render: function () {
            return btn({
              caption: "Manage",
              type: "warning",
              size: "sm",
              icon: "pencil",
              href: util.path("applications", this.state.model.id, "clients")
            })
          }
        })
      }
    ];

    return util.rf({
      getInitialState: function () {
        return {
          apps: new mdls.Applications(),
          createOpen: false
        };
      },

      componentDidMount: function () {
        this.state.apps.fetch();
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
              href: "applications/create",
              icon: "plus"
            }),
            "My Applications"
          ]),
          lw({
            key: "t",
            watch: this.state.apps
          }, table({
            collection: this.state.apps,
            attributes: appTableColumns
          }))
        ]);
      }
    });
  });