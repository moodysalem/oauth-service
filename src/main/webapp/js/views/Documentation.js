/**
 *
 */
define([ "react", "util", "rbs/components/layout/Icon", "rbs/components/layout/Tip", "underscore" ],
  function (React, util, icon, tip, _) {
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
            desc: rpt.string.isRequired,
            opts: rpt.arrayOf(rpt.string),
            value: rpt.string
          }).isRequired
        ),
        contentType: rpt.oneOf([ "application/x-www-form-urlencoded" ])
      },

      getDefaultProps: function () {
        return {
          contentType: "application/x-www-form-urlencoded"
        };
      },

      getInitialState: function () {
        return {
          parameters: {}
        };
      },

      handleChange: function (paramName, event) {
        var val = event.target.value;
        if (typeof val !== "string" || val.trim().length === 0) {
          val = null;
        }
        var newVal = {};
        newVal[ paramName ] = val;
        var newParams = _.extend({}, this.state.parameters, newVal);
        this.setState({
          parameters: newParams
        });
      },

      getInput: function (oneP) {
        var val = (typeof oneP.value === "undefined" ? this.state.parameters[ oneP.name ] : oneP.value),
          ro = typeof oneP.value !== "undefined";
        if (_.isArray(oneP.opts)) {
          return d.select({
            className: "form-control",
            readOnly: ro,
            value: val,
            onChange: _.bind(this.handleChange, this, oneP.name)
          }, _.map(oneP.opts, function (o) {
            return d.option({ key: o, value: o }, o);
          }));
        }
        return d.input({
          type: "text",
          placeholder: oneP.name,
          className: "form-control input-sm",
          readOnly: (typeof oneP.value !== "undefined"),
          value: (typeof oneP.value === "undefined" ? this.state.parameters[ oneP.name ] : oneP.value),
          onChange: _.bind(this.handleChange, this, oneP.name)
        });
      },

      getParameterTable: function () {
        return d.div({ key: "d" }, [
          d.h5({ key: "hdr" }, "Parameters"),
          d.table({ key: "t", className: "table table-bordered table-responsive-horizontal" }, [
            d.thead({ key: "th" },
              d.tr({}, [
                d.td({ key: "r" }, tip({ tip: "Whether the parameter is required as part of a valid request" }, "Required")),
                d.td({ key: "1" }, tip({ tip: "The expected name of the parameter" }, "Name")),
                d.td({ key: "2" }, tip({ tip: "The data type of the parameter" }, "Type")),
                d.td({ key: "val" }, tip({ tip: "Enter a value to see an example request" }, "Value")),
                d.td({ key: "4" }, tip({ tip: "Where the parameter should be found in the request" }, "Location")),
                d.td({ key: "3" }, tip({ tip: "What the parameter does." }, "Description"))
              ])
            ),
            d.tbody({ key: "tb" },
              _.map(this.props.parameters, function (oneP) {
                return d.tr({ key: oneP.name }, [
                  d.td({ key: "r", "data-title": "Required" }, oneP.req ? "Yes" : "No"),
                  d.td({ key: "1", "data-title": "Name" }, oneP.name),
                  d.td({ key: "2", "data-title": "Type" }, oneP.type),
                  d.td({ key: "val", "data-title": "Value" }, d.span({}, this.getInput(oneP))),
                  d.td({ key: "3", "data-title": "Location" }, oneP.loc),
                  d.td({ key: "4", "data-title": "Description" }, oneP.desc)
                ])
              }, this)
            )
          ])
        ]);
      },

      getParamObj: function (location) {
        location = location.toLowerCase();
        var dataObj = {};
        _.each(this.props.parameters, function (oneP) {
          if ((oneP.req || oneP.value || this.state.parameters[ oneP.name ]) && oneP.loc.toLowerCase() === location) {
            var val = typeof oneP.value !== "undefined" ? oneP.value : this.state.parameters[ oneP.name ];
            if (typeof val !== "string") {
              val = "";
            }
            dataObj[ oneP.name ] = val;
          }
        }, this);
        return dataObj;
      },

      getBody: function () {
        var dataObj = this.getParamObj("body");
        if (_.keys(dataObj).length > 0) {
          var toReturn = "-d '";
          if (this.props.contentType === "application/json") {
            toReturn += JSON.stringify(dataObj);
          } else {
            toReturn += $.param(dataObj, true);
          }
          toReturn += "'";
          return toReturn;
        }
        return "";
      },

      getHeaders: function () {
        var headerObj = this.getParamObj("header");
        if (_.keys(headerObj).length > 0) {
          var toReturn = [];
          _.each(headerObj, function (value, key, list) {
            toReturn.push("-H '" + key + ": " + value + "'");
          });
          return toReturn.join(" ");
        }
        return "";
      },

      getEndpoint: function () {
        var paramObj = this.getParamObj("query");
        return this.props.endpoint + (_.keys(paramObj).length > 0 ? ("?" + $.param(paramObj, true)) : "");
      },

      render: function () {
        return d.div({ className: "card " }, [
          d.div({ key: "ep", className: "well well-sm nowrap-scroll" }, [
            "curl ",
            (this.props.method !== "GET" ? d.strong({ key: "m" }, [ " -X ", this.props.method ]) : ""),
            " ",
            this.getEndpoint(),
            " ",
            this.getBody(),
            " ",
            this.getHeaders()
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
              d.ul({ key: "suboauth" }, [
                d.li({ key: "1" }, d.a({ href: "#authorization_code" }, "Authorization Code")),
                d.li({ key: "2" }, d.a({ href: "#resource_owner_password" }, "Resource Owner Password")),
                d.li({ key: "3" }, d.a({ href: "#client_credentials" }, "Client Credentials")),
                d.li({ key: "4" }, d.a({ href: "#refresh_token" }, "Refresh Token"))
              ]),
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
                  endpoint: "https://oauth2cloud.com/oauth/authorize",
                  parameters: [
                    {
                      req: true,
                      name: "response_type",
                      type: "string",
                      loc: "query",
                      opts: [ "token", "code" ],
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
                }),
                d.p({ key: "3" }, "To exchange an authorization code for a token, or exchange a refresh token for another " +
                  "access token, use the token endpoint. Each of these actions requires a different grant_type."),
                d.h5({ key: "h51", id: "authorization_code" }, "Authorization Code"),
                ep({
                  key: "4",
                  method: "POST",
                  endpoint: "https://oauth2cloud.com/oauth/token",
                  parameters: [
                    {
                      req: true,
                      name: "grant_type",
                      value: "authorization_code",
                      type: "string",
                      loc: "body",
                      desc: "The grant_type is one of the following values: 'authorization_code', 'password', 'client_credentials', 'refresh'"
                    },
                    {
                      req: true,
                      name: "code",
                      type: "string",
                      loc: "body",
                      desc: "The authorization code received as a query parameter in the redirect URI from the authorize endpoint."
                    },
                    {
                      req: true,
                      name: "redirect_uri",
                      type: "string",
                      loc: "body",
                      desc: "The exact redirect URI with which the authorization code grant flow was initialized."
                    },
                    {
                      req: true,
                      name: "client_id",
                      type: "string",
                      loc: "body",
                      desc: "The ID of the client that requested the authorization code "
                    }
                  ]
                }),
                d.h5({ key: "h52", id: "resource_owner_password" }, "Resource Owner Password"),
                ep({
                  key: "password",
                  method: "POST",
                  endpoint: "https://oauth2cloud.com/oauth/token",
                  parameters: [
                    {
                      req: true,
                      name: "grant_type",
                      value: "password",
                      type: "string",
                      loc: "body",
                      desc: "The grant_type is one of the following values: 'authorization_code', 'password', 'client_credentials', 'refresh'"
                    },
                    {
                      req: true,
                      name: "Authorization",
                      type: "string",
                      loc: "header",
                      desc: "'Basic ' followed by the base64-encoded clientid:secret. This is required for the resource owner password flow."
                    },
                    {
                      req: true,
                      name: "username",
                      type: "string",
                      loc: "body",
                      desc: "The e-mail address of the user."
                    },
                    {
                      req: true,
                      name: "password",
                      type: "string",
                      loc: "body",
                      desc: "The password of the user."
                    }
                  ]
                }),
                d.h5({ key: "h53", id: "client_credentials" }, "Client Credentials"),
                ep({
                  key: "client_credentials",
                  method: "POST",
                  endpoint: "https://oauth2cloud.com/oauth/token",
                  parameters: [
                    {
                      req: true,
                      name: "grant_type",
                      value: "client_credentials",
                      type: "string",
                      loc: "body",
                      desc: "The grant_type is one of the following values: 'authorization_code', 'password', 'client_credentials', 'refresh'"
                    },
                    {
                      req: true,
                      name: "Authorization",
                      type: "string",
                      loc: "header",
                      desc: "'Basic ' followed by the base64-encoded clientid:secret. This is required for the resource owner password flow."
                    },
                    {
                      req: false,
                      name: "scope",
                      type: "string",
                      loc: "body",
                      desc: "The scopes for which the client is requesting a token."
                    }
                  ]
                }),
                d.h5({ key: "h54", id: "refresh_token" }, "Refresh Token"),
                ep({
                  key: "refresh",
                  method: "POST",
                  endpoint: "https://oauth2cloud.com/oauth/token",
                  parameters: [
                    {
                      req: true,
                      name: "grant_type",
                      value: "refresh",
                      type: "string",
                      loc: "body",
                      desc: "The grant_type is one of the following values: 'authorization_code', 'password', 'client_credentials', 'refresh'"
                    },
                    {
                      req: true,
                      name: "Authorization",
                      type: "string",
                      loc: "header",
                      desc: "'Basic ' followed by the base64-encoded clientid:secret. This is required for the resource owner password flow."
                    },
                    {
                      req: true,
                      name: "refresh_token",
                      type: "string",
                      loc: "body",
                      desc: "The refresh token for the user for which a new access token should be obtained."
                    },
                    {
                      req: false,
                      name: "scope",
                      type: "string",
                      loc: "body",
                      desc: "The scopes for which the access token should be valid. These scopes cannot exceed the scopes " +
                      " for which the refresh token was obtained, even if the user has accepted those scopes for the client " +
                      "to which the refresh token was distributed."
                    }
                  ]
                })
              ]),
              d.h4({ key: "admin", id: "admin" }, "Admin"),
              d.p({ key: "admininfo" }, "This documentation is not yet complete. In the meantime, please use the administrative features on this " +
                "website to manage your OAuth2 application."),
              d.h3({ key: "addtnl", id: "addtl" }, "Additional Features"),
              d.h4({ key: "legacy", id: "legacy" }, "Legacy Integration"),
              d.p({
                key: "legacyinfo"
              }, "You may need to integrate with an existing database of users. We allow existing users' e-mails and passwords " +
                " to be ported via and endpoint that is hit with every failed login attempt. You can specify this endpoint" +
                " in your application settings. This endpoint will receive a POST containing the e-mail and password and should return " +
                " the user's first name and last name if the user's credentials are valid. OAuth2Cloud will then create a user and issue " +
                " an OAuth2 token."),
              ep({
                key: "lgep",
                endpoint: "https://your-legacy-url.com",
                method: "POST",
                parameters: [
                  {
                    req: true,
                    name: "email",
                    type: "string",
                    loc: "body",
                    desc: "The e-mail address that the user entered."
                  },
                  {
                    req: true,
                    name: "password",
                    type: "string",
                    loc: "body",
                    desc: "The password that the user entered."
                  }
                ]
              })
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