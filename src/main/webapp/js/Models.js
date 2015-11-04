/**
 *
 */
define([ "util", "backbone" ], function (util, Backbone) {
  "use strict";
  var mdl = Backbone.Model;
  var cl = Backbone.Collection;

  var APPLICATIONS_URL = util.path(API_URL, "applications");
  return {
    Application: mdl.extend({
      urlRoot: APPLICATIONS_URL
    }),
    Applications: cl.extend({
      url: APPLICATIONS_URL
    })
  };
});