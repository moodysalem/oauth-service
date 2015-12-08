/**
 *
 */
define([ "react", "rbs", "underscore", "rbs/components/controls/Button", "./ContactModal" ],
  function (React, rbs, _, btn, cm) {
    "use strict";

    var util = rbs.util;
    var d = React.DOM;
    var rpt = React.PropTypes;

    return util.rf({
      displayName: "Pricing",

      getInitialState: function () {
        return {
          contactOpen: false
        };
      },

      openContact: function () {
        this.setState({
          contactOpen: true
        });
      },

      closeContact: function () {
        this.setState({
          contactOpen: false
        });
      },

      render: function () {
        return d.div({ className: "container" }, [
          d.h2({ key: "h2", className: "page-header" }, [
            btn({
              type: "success",
              key: "cu",
              caption: "Contact Us",
              icon: "question",
              size: "sm",
              className: "pull-right",
              onClick: this.openContact
            }),
            "Pricing"
          ]),
          d.p({ key: "lead", className: "lead" }, [
            "One-tenth of a cent ($0.001 USD) per API call, or 1000 calls per dollar."
          ]),
          d.p({ key: "more" }, [
            "That excludes any calls made to the administrative API to manage users, clients, scopes or tokens. " +
            "Each Application gets 10000 free calls per month. Bills are calculated on the first of the month."
          ]),
          d.small({
            key: "small",
            className: "sm-margin-bottom"
          }, "* These pricing terms may change at any time. " +
            "Best efforts will be made to accommodate existing users, and export your data if necessary. At least 1 month of notice will be given for pricing increases."),
          cm({ key: "cm", open: this.state.contactOpen, onClose: this.closeContact })
        ]);
      }
    });
  });