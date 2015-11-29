/**
 * This is where you can register a client
 */
define([ "react", "rbs", "underscore", "js/Models", "model", "./MustBeLoggedIn", "jquery" ],
  function (React, rbs, _, mdls, m, mbli, $) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    var util = rbs.util;
    var alt = rbs.components.layout.Alert;
    var form = rbs.components.model.Form;
    var btn = rbs.components.controls.Button;
    var bb = rbs.backbone;

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
          pa: new mdls.PublicApplication({ id: this.props.applicationId }),
          newClient: new bb.Model()
        };
      },

      componentDidMount: function () {
        this.state.pa.fetch();
      },

      createClient: function () {
        $.ajax({
          method: "POST",
          url: util.path(API_URL, "publicclients"),
          data: JSON.stringify(this.state.newClient.toJSON())
        }).then(_.bind(function () {

        }, this), _.bind(function () {

        }, this));
      },

      render: function () {
        if (!m.isLoggedIn()) {
          return mbli({});
        }

        return d.div({ className: "container" }, [
          header({ key: "hdr", model: this.state.pa }),
          form({
            key: "F",
            model: this.state.newClient,
            onSubmit: this.createClient,
            attributes: [
              {
                attribute: "name",
                component: "text",
                label: "Client Name",
                required: true,
                placeholder: "Client Name",
                tip: "Enter the name for your client."
              },
              {
                component: btn,
                caption: "Submit",
                submit: true,
                block: true,
                ajax: true,
                type: "primary",
                icon: "plus"
              }
            ]
          })
        ]);
      }
    });
  });