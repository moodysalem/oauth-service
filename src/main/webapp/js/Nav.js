/**
 *
 */
define(["react", "underscore", "model", "rbs/components/mixins/Model", "rbs/components/mixins/NavbarHelper",
        "rbs/components/layout/Navbar", "rbs/components/layout/NavbarGroup", "rbs/components/layout/Icon"],
    function (React, _, m, model, nh, navbar, ng, icon) {
        "use strict";

        return _.rf({
            mixins: [model, nh],
            render: function () {
                var mdl = this.state.model;
                var leftLinks = [];

                var rightLinks = [];

                if (mdl.token) {
                    var dn = _.concatWS(" ", mdl.token.user_details.firstName, mdl.token.user_details.lastName);
                    rightLinks.push({
                        text: "Logged in as " + dn
                    });
                }

                return navbar({
                    brand: React.DOM.span({}, [icon({key: "i", name: "chain"}), "OAuth2 Cloud"])
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