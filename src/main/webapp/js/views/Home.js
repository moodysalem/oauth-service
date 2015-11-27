/**
 *
 */
define([ "react", "underscore", "rbs" ], function (React, _, rbs) {
  "use strict";
  var util = rbs.util;

  var d = React.DOM;
  var spec = "https://tools.ietf.org/html/rfc6749";
  return util.rf({
    render: function () {
      return d.div({}, [
        d.div({
          className: "home-bg",
          key: "home-bg"
        }, d.div({ className: "container" }, [
          d.div({
            key: "1",
            className: "col-sm-7 col-sm-offset-1"
          }, [
            d.div({
              key: "1",
              className: "header-lead"
            }, [
              d.h1({ key: "h1", className: "page-header" }, "OAuth2 Cloud"),
              d.p({
                key: "p",
                className: "lead"
              }, [ "Simple and reliable hosted ", d.a({ key: "spec", href: spec }, "OAuth2"), " server" ])
            ])
          ])
        ])),
        d.div({ key: "body-of-home", className: "container" }, [
          d.div({ key: "bs", className: "bootstrap-startup row" }, [
            d.div({
              key: "col",
              className: "col-sm-12"
            }, [
              d.h2({ key: "1" }, "Bootstrap your Startup"),
              d.p({ key: "2" }, "Don't spend time writing code to integrate with Facebook, Google, Amazon, or sending verification " +
                "and reset password e-mails. OAuth2Cloud provides everything you need to get users authenticated.")
            ])
          ]),
          d.hr({ key: "hr1" }),
          d.div({ key: "legacy-startup", className: "legacy-startup row" }, [
            d.div({
              key: "1",
              className: "col-sm-12"
            }, [
              d.h2({ key: "1" }, "Integrate with Legacy Code"),
              d.p({ key: "2" }, "If you already have an identity server, we provide a simple and easy method to integrate that system" +
                "  allowing users to log in using their existing e-mail and password.")
            ])
          ]),
          d.hr({ key: "hr2" }),
          d.div({ key: "your-data", className: "your-data row" }, [
            d.div({
              key: "1",
              className: "col-sm-12"
            }, [
              d.h2({ key: "1" }, "Own your Data"),
              d.p({ key: "2" }, "All your application data is completely separate from every other OAuth2 Application we serve. " +
                "We provide a powerful API to access and manipulate the data.")
            ])
          ]),
          d.hr({ key: "hr3" }),
          d.div({
            key: "info-text",
            className: "info-text"
          }, [
            d.h4({ key: "1" }, "More Info"),
            d.p({ key: "2" }, "OAuth2Cloud is an OAuth2 server as a service. Rather than write your own OAuth2 server, you can register an " +
              "application with OAuth2Cloud, define the login methods and scopes, and use our server with your own APIs to authenticate users. " +
              "In addition, you can use OAuth2 cloud to manage third-party clients to your APIs and even allow public registration with your approval. " +
              "Below is a short summary of our features:"),
            d.ul({ key: "ul" }, [
              d.li({ key: "1" }, [ "Full implementation of the final OAuth2 specification, ", d.a({
                key: "a",
                href: spec
              }, "RFC6749") ]),
              d.li({ key: "2" }, "Login and Registration via Facebook, Google, Amazon using your own client ID and secret"),
              d.li({ key: "3" }, "Create and assign scopes to clients"),
              d.li({ key: "4" }, "Allow other users to register clients to your application"),
              d.li({ key: "5" }, "Integrate with legacy databases using a simple webhook"),
              d.li({ key: "6" }, "Access any of the above using our powerful API")
            ])
          ]),
          d.hr({ key: "hr4" }),
          d.div({ key: "the-team" }, [
            d.h5({ key: "tctt", className: "text-center the-team" }, "The Team"),
            d.div({ key: "row", className: "row" }, [
              d.div({
                key: "1",
                className: "col-sm-4 col-sm-offset-4"
              }, d.div({ className: "text-center card-inverse" }, [
                d.img({ key: "img", src: "res/moody.jpg", className: "team-thumb" }),
                d.h5({ key: "name" }, "Moody Salem"),
                d.p({ key: "info" }, [ "I created this site to provide login for my own small projects. I hope you'll also use it and benefit from it. If you have " +
                "any questions or would like to help, please contact me at ", d.a({
                  key: "email",
                  href: "mailto:moody@oauth2cloud.com"
                }, "moody@oauth2cloud.com") ])
              ]))
            ])
          ])
        ])
      ]);
    }
  });
});