/**
 * nav bar
 */
define([ "react", "underscore", "model", "rbs/components/mixins/Model", "rbs/components/mixins/NavbarHelper",
    "rbs/components/layout/Navbar", "rbs/components/layout/NavbarGroup", "rbs/components/layout/Icon", "js/OAuth2", "util",
    "rbs/components/controls/Tappable" ],
  function (React, _, m, model, nh, navbar, ng, icon, oauth2, util, tp) {
    "use strict";

    return util.rf({
      mixins: [ model, nh ],
      render: function () {
        var mdl = this.state.model;
        var leftLinks = [
          {
            text: "Home",
            icon: "home",
            href: "/"
          },
          {
            text: "Documentation",
            icon: "file-text",
            href: "/docs"
          },
          {
            text: "Pricing",
            icon: "money",
            href: "pricing"
          },
          {
            text: "Applications",
            icon: "tachometer",
            href: "/applications"
          },
          {
            text: "Public Applications",
            icon: "gavel",
            href: "/publicapplications"
          }
        ];

        var rightLinks = [];

        if (mdl.token) {
          var dn = util.concatWS(" ", mdl.token.user_details.first_name, mdl.token.user_details.last_name);
          rightLinks.push({
            text: "Logged in as " + dn
          }, {
            text: "Log Out",
            icon: "sign-out",
            onClick: function (e) {
              e.preventDefault();
              m.clear();
              oauth2.logout().then(function () {
                util.debug("loggedout");
              }, function () {
                util.debug("loggedout with error");
              });
            }
          });
        } else {
          rightLinks.push({
            text: "Log In",
            icon: "sign-in",
            href: "#",
            onClick: function (e) {
              e.preventDefault();
              oauth2.login();
            }
          });
        }

        return tp({}, navbar({
          brand: React.DOM.span({}, [
            icon({ key: "i", name: "chain", style: { color: "cornflowerblue" } }),
            "OAuth2 Cloud"
          ])
        }, [
          ng({
            key: "ll"
          }, this.buildLinks(leftLinks)),
          ng({
            key: "rl",
            right: true
          }, this.buildLinks(rightLinks))
        ]));
      }
    });
  });