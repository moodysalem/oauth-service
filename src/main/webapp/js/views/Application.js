/**
 *
 */
define([ "react", "util", "./ApplicationForm", "js/Models", "rbs/components/controls/Button", "model" ],
  function (React, util, af, mdls, btn, m) {
    "use strict";

    var d = React.DOM;

    return util.rf({
      displayName: "app",

      getInitialState: function () {
        return {
          app: (new mdls.Application({
            id: this.props.id === "create" ? null : this.props.id,
            ownerId: m.get("token.user_details.id")
          }))
        };
      },

      componentDidMount: function () {
        if (this.props.id !== "create") {
          this.state.app.fetch();
        }
      },

      render: function () {
        var dn = (this.props.id === "create") ? "New Application" : "Edit Application";

        return d.div({ className: "container" }, [
          d.h2({
            key: "h2",
            className: "page-header"
          }, dn),
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
              caption: "Delete",
              type: "danger",
              icon: "ban",
              block: true
            })),
            d.div({ className: "col-xs-6", key: "2" }, btn({
              caption: "Save",
              type: "success",
              icon: "save",
              block: true,
              onClick: _.bind(function () {
                this.refs.af.submit();
              }, this)
            }))
          ])
        ]);
      }
    });
  });