/**
 *
 */
define([ "react", "util", "underscore", "rbs/components/mixins/Model", "rbs/components/controls/Button" ],
  function (React, util, _, model, btn) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    return util.rf({
      mixins: [ model ],

      getLogo: function () {
        if (this.state.model.logoUrl !== null) {
          return d.img({ key: "logo", src: this.state.model.logoUrl, alt: this.state.model.name + " Logo" });
        }
        return null;
      },

      render: function () {
        return d.div({ className: "thumbnail" }, [
          this.getLogo(),
          d.div({ className: "caption", key: "c" }, [
            d.h3({ key: "name" }, this.state.model.name),
            d.p({ key: "de" }, this.state.model.description),
            d.p({ key: "btns" }, [
              btn({
                key: "reg",
                icon: "external-link-square",
                caption: "Register",
                type: "primary",
                href: util.path("registerclient", this.state.model.id),
                size: "sm"
              })
            ])
          ])
        ]);
      }
    });
  });