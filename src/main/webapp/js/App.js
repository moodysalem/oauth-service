window.require.config({
    baseUrl: ""
});
window.define(["rbs/RequireConfig"], function (rc) {
    window.require.config(rc);

    window.require(["react", "react-dom", "underscore", "backbone"], function (React, dom, _, Backbone) {
        var d = React.DOM;
        dom.render(_.rf({
            render: function () {
                return d.div({className: "container"}, [
                    d.h1({key: "h1",className:"page-header"}, "OAuth2 Cloud"),
                    d.p({key:"P",className:"lead"}, "Work in progress.")
                ]);
            }
        })(), $("#app").get(0));
    });
});