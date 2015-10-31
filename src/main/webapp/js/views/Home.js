/**
 *
 */
define(["react", "underscore"], function (React, _) {
    "use strict";

    return _.rf({
        render: function () {
            return React.DOM.div({ className: "container" }, [
                React.DOM.h1({className: "page-header", key:"h1"}, "OAuth2 Cloud"),
                React.DOM.p({className: "lead", key:"p"}, "A hosted OAuth2 server supporting login via Facebook, Amazon, and Google. A work in progress.")
            ]);
        }
    });
});