/**
 *
 */
define([ "rbs" ], function (rbs) {
  "use strict";
  var util = rbs.util, Backbone = rbs.backbone;

  var mdl = Backbone.Model;
  var cl = Backbone.Collection;

  var APPLICATIONS_URL = util.path(API_URL, "applications");
  var PUBLIC_APPLICATIONS_URL = util.path(API_URL, "publicapplications");
  var SCOPES_URL = util.path(API_URL, "scopes");
  var CLIENTS_URL = util.path(API_URL, "clients");
  var CLIENT_SCOPES_URL = util.path(API_URL, "clientscopes");
  var USERS_URL = util.path(API_URL, "users");
  return {
    Application: mdl.extend({
      urlRoot: APPLICATIONS_URL
    }),
    Applications: cl.extend({
      url: APPLICATIONS_URL
    }),
    PublicApplication: mdl.extend({
      urlRoot: PUBLIC_APPLICATIONS_URL
    }),
    PublicApplications: cl.extend({
      url: PUBLIC_APPLICATIONS_URL
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
    ClientScope: mdl.extend({
      urlRoot: CLIENT_SCOPES_URL
    }),
    ClientScopes: cl.extend({
      url: CLIENT_SCOPES_URL
    }),
    User: mdl.extend({
      urlRoot: USERS_URL
    }),
    Users: cl.extend({
      url: USERS_URL
    }),
    ClientTypes: new cl([
      {
        name: "CONFIDENTIAL"
      },
      {
        name: "PUBLIC"
      }
    ]),
    ClientFlows: new cl([
      {
        name: "IMPLICIT"
      },
      {
        name: "CODE"
      },
      {
        name: "RESOURCE_OWNER_CREDENTIALS"
      },
      {
        name: "CLIENT_CREDENTIALS"
      }
    ]),
    ClientScopePriorities: new cl([
      {
        name: "ALWAYS"
      },
      {
        name: "ASK"
      },
      {
        name: "REQUIRE"
      }
    ])
  };
});