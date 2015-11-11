/**
 *
 */
define([ "util", "backbone" ], function (util, Backbone) {
  "use strict";
  var mdl = Backbone.Model;
  var cl = Backbone.Collection;

  var APPLICATIONS_URL = util.path(API_URL, "applications");
  var SCOPES_URL = util.path(API_URL, "scopes");
  var CLIENTS_URL = util.path(API_URL, "clients");
  return {
    Application: mdl.extend({
      urlRoot: APPLICATIONS_URL
    }),
    Applications: cl.extend({
      url: APPLICATIONS_URL
    }),
    Scope: mdl.extend({
      urlRoot: SCOPES_URL
    }),
    Scopes: cl.extend({
      url: SCOPES_URL
    }),
    Client: mdl.extend({
      urlRoot: CLIENTS_URL
    }),
    Clients: cl.extend({
      url: CLIENTS_URL
    }),
    ClientTypes: new cl([
      {
        id: "CONFIDENTIAL",
        name: "CONFIDENTIAL"
      },
      {
        id: "PUBLIC",
        name: "PUBLIC"
      }
    ]),
    ClientFlows: new cl([
      {
        id: "IMPLICIT",
        name: "IMPLICIT"
      },
      {
        id: "CODE",
        name: "CODE"
      },
      {
        id: "RESOURCE_OWNER_CREDENTIALS",
        name: "RESOURCE_OWNER_CREDENTIALS"
      },
      {
        id: "CLIENT_CREDENTIALS",
        name: "CLIENT_CREDENTIALS"
      }
    ])
  };
});