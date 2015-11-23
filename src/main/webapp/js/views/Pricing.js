/**
 *
 */
define([ "react", "util", "underscore", "backbone", "rbs/components/controls/Button", "./ContactModal" ],
  function (React, util, _, Backbone, btn, cm) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    return util.rf({
      displayName: "pricing",
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
          d.h1({ key: "h1", className: "page-header" }, [
            btn({
              type: "success",
              key: "cu",
              caption: "Contact Us",
              icon: "question",
              className: "pull-right",
              onClick: this.openContact
            }),
            "Pricing"
          ]),
          d.p({ key: "lead", className: "lead" }, [
            d.strong({ key: "s" }, "No fee or rate limiting system is currently implemented."),
            " Please be respectful of the following suggestions and contact us to cover fees if you wish to exceed the limits."
          ]),
          d.table({ className: "table table-bordered", key: "T" }, [
            d.thead({ key: "Th" }, d.tr({ key: "H" }, [
              d.th({ key: "#" }, "Max # Users"),
              d.th({ key: "calls" }, "Requests per second"),
              d.th({ key: "cost" }, "Suggested Fee")
            ])),
            d.tbody({ key: "tb" }, [
              d.tr({ key: "b" }, [
                d.th({ key: "2" }, "10,000"),
                d.th({ key: "3" }, "10"),
                d.th({ key: "4" }, "Free")
              ]),
              d.tr({ key: "p" }, [
                d.th({ key: "2" }, "25,000"),
                d.th({ key: "3" }, "30"),
                d.th({ key: "4" }, "$30/mo.")
              ]),
              d.tr({ key: "pp" }, [
                d.th({ key: "2" }, "50,000"),
                d.th({ key: "3" }, "45"),
                d.th({ key: "4" }, "$50/mo.")
              ]),
              d.tr({ key: "more" }, [
                d.th({ key: "2" }, ">50,000"),
                d.th({ key: "3" }, ">45"),
                d.th({ key: "4" }, d.a({ key: "cu", href: "#", onClick: this.openContact }, " Contact Us"))
              ])
            ])
          ]),
          d.small({ key: "small" }, "* Note that these pricing terms may change at any time. " +
            "Best efforts will be made to accommodate existing users."),
          cm({ key: "cm", open: this.state.contactOpen, onClose: this.closeContact })
        ]);
      }
    });
  });