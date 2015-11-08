/**
 *
 */
define([ "react", "util", "rbs/components/layout/Icon" ], function (React, util, icon) {
  "use strict";
  var d = React.DOM;
  var rpt = React.PropTypes;

  var ep = util.rf({
    propTypes: {
      method: rpt.string.isRequired,
      endpoint: rpt.string.isRequired,
      parameters: rpt.arrayOf(
        rpt.shape({
          req: rpt.bool.isRequired,
          name: rpt.string.isRequired,
          type: rpt.string.isRequired,
          loc: rpt.string.isRequired,
          desc: rpt.string.isRequired
        }).isRequired
      )
    },

    getParameterTable: function () {
      return d.div({ key: "d" }, [
        d.h5({ key: "hdr" }, "Parameters"),
        d.table({ key: "t", className: "table table-striped table-responsive-horizontal" }, [
          d.thead({ key: "th" },
            d.tr({}, [
              d.td({ key: "r" }, "Required"),
              d.td({ key: "1" }, "Name"),
              d.td({ key: "2" }, "Type"),
              d.td({ key: "4" }, "Location"),
              d.td({ key: "3" }, "Description")
            ])
          ),
          d.tbody({ key: "tb" },
            _.map(this.props.parameters, function (oneP) {
              return d.tr({ key: oneP.name }, [
                d.td({ key: "r", "data-title": "Required" }, oneP.req ? icon({ name: "check" }) : null),
                d.td({ key: "1", "data-title": "Name" }, oneP.name),
                d.td({ key: "2", "data-title": "Type" }, oneP.type),
                d.td({ key: "3", "data-title": "Location" }, oneP.loc),
                d.td({ key: "4", "data-title": "Description" }, oneP.desc)
              ])
            })
          )
        ])
      ]);
    },

    render: function () {
      return d.div({ className: "card " }, [
        d.div({ key: "ep", className: "well well-sm" }, [
          d.strong({ key: "m" }, this.props.method),
          " ",
          this.props.endpoint
        ]),
        this.getParameterTable()
      ])
    }
  });

  var code = util.rf({
    displayName: "code",
    render: function () {
      return d.span({ className: "well well-sm" }, this.props.children);
    }
  });

  return util.rf({
    displayName: "Docs",

    getInitialState: function () {
      return {};
    },

    render: function () {
      var toc = d.div({}, [
        d.h5({ key: "h5" }, "Table of Contents"),
        d.hr({ key: "hr" }),
        d.ul({ key: "ul" }, [
          d.li({ key: "intro" }, d.a({ href: "#intro" }, "Introduction")),
          d.ul({ key: "sub" }, [
            d.li({ key: "purpose" }, d.a({ href: "#purpose" }, "Purpose")),
            d.li({ key: "structure" }, d.a({ href: "#structure" }, "Application Structure"))
          ]),
          d.li({ key: "api" }, d.a({ href: "#api" }, "Developer API")),
          d.ul({ key: "subapi" }, [
            d.li({ key: "a" }, d.a({ href: "#oauth2" }, "OAuth2")),
            d.li({ key: "a2" }, d.a({ href: "#admin" }, "Admin"))
          ]),
          d.li({ key: "addtl" }, d.a({ href: "#addtl" }, "Additional Features")),
          d.ul({ key: "subaddtl" }, [
            d.li({ key: "a" }, d.a({ href: "#legacy" }, "Legacy Integration"))
          ])
        ])
      ]);

      return d.div({ className: "container-fluid" }, [
        d.div({ className: "row", key: "1" }, [
          d.div({ key: "c", className: "col-md-8 col-lg-7 col-xl-6 col-lg-offset-1" }, d.h2({
            key: "1",
            className: "page-header"
          }, "Documentation"))
        ]),
        d.div({ className: "row", key: "2" }, [
          d.div({ className: "visible-xs visible-sm", key: "toc-small" }, d.div({
            className: "well",
            key: "toc"
          }, toc)),
          d.div({ className: "col-md-8 col-lg-7 col-lg-offset-1", key: "content" }, [
            d.h3({ key: "intro", id: "intro" }, "Introduction"),
            d.p({ key: "ip" }, "Applications often require users to authenticate to access their own data and use services. These " +
              " services often store and transmit user data, such as e-mails, passwords, and names. Completing a secure and reliable OAuth2 " +
              " specification is time consuming and difficult. OAuth2Cloud aims to provide a customizable OAuth2 implementation as a service."),
            d.h4({ key: "purpose", id: "purpose" }, "Purpose"),
            d.p({ key: "pp" }, "To provide a stable and reliable identity platform on which you can build secure APIs, register " +
              " third party and internal clients for your APIs, and authenticate users and clients for these APIs."),
            d.h4({ key: "structure", id: "structure" }, "Application Structure"),
            d.p({ key: "pp2" }, "OAuth2Cloud follows the OAuth2 specification as closely as possible. For user authentication, you can permit " +
              " clients to use any of the grant methods defined in the ", d.a({
              key: "A",
              href: "https://tools.ietf.org/html/rfc6749"
            }, "specification"), ". After receiving an access token from OAuth2Cloud, clients will pass the tokens to your " +
              " API servers using bearer HTTP authentication. Your API server then verifies with OAuth2Cloud's token info endpoint " +
              " that the token has the proper scopes, and uses the user information to customize the response. "),
            d.h3({ key: "api", id: "api" }, "Developer API"),
            d.p({ key: "apiinfo" }, "OAuth2Cloud provides the typical OAuth2 specification authorize and token endpoints in addition to an API for " +
              " managing your OAuth2 resources. In order to access the administrative API, you must register a client with the OAuth2Cloud application."),
            d.h4({ key: "oauth2", id: "oauth2" }, "OAuth2"),
            d.div({ key: "oauth2info" }, [
              d.p({ key: "1" }, "To access the login screen for your application, use the authorize endpoint."),
              ep({
                key: "2",
                method: "GET",
                endpoint: "https://oauth2cloud.com/authorize",
                parameters: [
                  {
                    req: true,
                    name: "response_type",
                    type: "string",
                    loc: "query",
                    desc: "Either token or code, depending on the desired response type. See the OAuth2 specification."
                  },
                  {
                    req: true,
                    name: "client_id",
                    type: "string",
                    loc: "query",
                    desc: "The ID of the client that is attempting to get user authorization."
                  },
                  {
                    req: true,
                    name: "redirect_uri",
                    type: "string",
                    loc: "query",
                    desc: "The location to which the user should be redirected after completing or cancelling authentication."
                  },
                  {
                    req: false,
                    name: "scope",
                    type: "string",
                    loc: "query",
                    desc: "A space-delimited list of scopes that the client requires. Listing scopes that the client cannot access here will " +
                    "cause an error to be displayed to the user."
                  },
                  {
                    req: false,
                    name: "logout",
                    type: "boolean",
                    loc: "query",
                    desc: "Pass true to log the user out. This is especially useful when the user must accept certain scopes to continue."
                  }
                ]
              })
            ]),
            d.h4({ key: "admin", id: "admin" }, "Admin"),
            d.p({ key: "admininfo" }, "This documentation is not yet complete. In the meantime, please use the administrative features on this " +
              "website to manage your OAuth2 application."),
            d.h3({ key: "addtnl", id: "addtl" }, "Additional Features"),
            d.h4({ key: "legacy", id: "legacy" }, "Legacy Integration"),
            d.p({ key: "legacyinfo" }, "You may need to integrate with an existing database of users. We allow existing users' e-mails and passwords " +
              " to be ported via and endpoint that is hit with every failed login attempt. You can specify this endpoint" +
              " in your application settings. This endpoint will receive a POST containing the e-mail and password and should return " +
              " the user's first name and last name if the user's credentials are valid. OAuth2Cloud will then create a user and issue " +
              " an OAuth2 token.")
          ]),
          d.div({ className: "col-md-4 col-lg-3 hidden-xs hidden-sm", key: "toc-big" }, d.div({
            className: "well",
            key: "toc"
          }, toc))
        ])
      ]);
    }
  });
});