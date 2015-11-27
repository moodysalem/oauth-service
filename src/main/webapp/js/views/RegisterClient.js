/**
 *
 */
define([ "react", "rbs", "underscore", "rbs/components/layout/Alert", "js/Models", "rbs/components/mixins/Model" ],
  function (React, rbs, _, alt, mdls, model) {
    "use strict";

    var util = rbs.util;
    var d = React.DOM;
    var rpt = React.PropTypes;

    var header = util.rf({
      displayName: "public app header",
      mixins: [ model ],
      render: function () {
        var dn = null;
        if (this.state.model.name) {
          dn = " for " + this.state.model.name;
        }

        return d.h2({ className: "page-header", key: "oph" }, [
          "Register Clients",
          d.small({ key: "name" }, dn)
        ]);
      }
    });

    return util.rf({
      displayName: "register clients",
      getInitialState: function () {
        return {
          pa: new mdls.PublicApplication({ id: this.props.applicationId })
        };
      },

      componentDidMount: function () {
        this.state.pa.fetch();
      },


      render: function () {
        return d.div({ className: "container" }, [
          header({ key: "hdr", model: this.state.pa }),
          alt({
            key: "info",
            icon: "info",
            level: "info",
            strong: "Info",
            message: "This page not yet implemented."
          })
        ]);
      }
    });
  });