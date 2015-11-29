/**
 * This is where you can register a client
 */
define([ "react", "rbs", "underscore", "js/Models", "model", "./MustBeLoggedIn" ],
  function (React, rbs, _, mdls, m, mbli) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    var util = rbs.util;
    var alt = rbs.components.layout.Alert;

    var header = util.rf({
      displayName: "public app header",
      mixins: [ rbs.mixins.Model ],
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
        if (!m.isLoggedIn()) {
          return mbli({});
        }

        return d.div({ className: "container" }, [
          header({ key: "hdr", model: this.state.pa }),

        ]);
      }
    });
  });