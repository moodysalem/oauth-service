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
            d.h3({ key: "intro" }, "Intro"),
            d.h4({ key: "purpose" }, "Purpose")
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