/**
 *
 */
define([ "react", "underscore", "model", "rbs/components/mixins/Model", "rbs/components/mixins/NavbarHelper",
    "rbs/components/layout/Navbar", "rbs/components/layout/NavbarGroup", "rbs/components/layout/Icon", "js/OAuth2", "util" ],
  function (React, _, m, model, nh, navbar, ng, icon, oauth2, util) {
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
          }
        ];

        var rightLinks = [];

        if (mdl.token) {
          leftLinks.push({
            text: "Applications",
            icon: "users",
            href: "/applications"
          });


          var dn = util.concatWS(" ", mdl.token.user_details.firstName, mdl.token.user_details.lastName);
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
                window.location.href = window.location.origin;
              }, function () {
                util.debug("loggedout with error");
                window.location.href = window.location.origin;
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

        return navbar({
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
        ]);
      }
    });
  });