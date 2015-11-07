/**
 *
 */
define([ "react", "util" ], function (React, util) {
  "use strict";

  var d = React.DOM;
  return util.rf({
    render: function () {
      var toc = d.div({}, [
        d.h5({ key: "h5" }, "Table of Contents"),
        d.hr({ key: "hr" }),
        d.ul({ key: "ul" }, [
          d.li({ key: "intro" }, d.a({ href: "#intro" }, "Intro")),
          d.ul({ key: "sub" }, [
            d.li({ key: "purpose" }, d.a({ href: "#purpose" }, "Purpose"))
          ])
        ])
      ]);

      return d.div({ className: "container-fluid" }, [
        d.div({ className: "row", key: "1" }, [
          d.div({ key: "c", className: "col-md-8" }, d.h1({
            key: "1",
            className: "page-header"
          }, "Documentation"))
        ]),
        d.div({ className: "row", key: "2" }, [
          d.div({ className: "col-md-4 visible-xs visible-sm", key: "toc-small" }, d.div({
            className: "well",
            key: "toc"
          }, toc)),
          d.div({ className: "col-md-8", key: "content" }, [
            d.h3({ key: "intro", id: "intro" }, "Intro"),
            d.p({ key: "ip" }, "Web applications often require the user to authenticate to access their own data. These web " +
              "applications often store and transmit user data in insecure manners. Completing a secure and reliable OAuth2" +
              " specification is time consuming and difficult. OAuth2 Cloud aims to offload this process and provide a stable platform" +
              " for you to authenticate users and provide access to your APIs."),
            d.h4({ key: "purpose", id: "purpose" }, "Purpose"),
            d.p({ key: "pp" }, "To provide a stable and reliable identity platform on which you can build secure APIs, register " +
              "third party clients to use your APIs, and easily use to authenticate users in web and native applications.")
          ]),
          d.div({ className: "col-md-4 hidden-xs hidden-sm", key: "toc-big" }, d.div({
            className: "well",
            key: "toc"
          }, toc))
        ])
      ]);
    }
  });
});