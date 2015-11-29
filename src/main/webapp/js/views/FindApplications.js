/**
 * list of applications that can be registered for
 */
define([ "react", "rbs", "underscore", "rbs/components/layout/Alert", "model", "js/Models",
    "rbs/components/controls/Button", "rbs/components/collection/Rows", "./PublicApplication", "./Loading" ],
  function (React, rbs, _, alt, m, mdls, btn, rows, publicApp, lw) {
    "use strict";

    var util = rbs.util;
    var d = React.DOM;
    var rpt = React.PropTypes;

    return util.rf({
      displayName: "pub apps",

      getInitialState: function () {
        return {
          pa: new mdls.PublicApplications(),
          search: "",
          lastSearch: ""
        };
      },

      componentDidMount: function () {
        this.state.pa.fetch();
      },

      searchChange: function (e) {
        var v = e.target.value;
        this.setState({
          search: v
        });
      },

      handleEnter: function (e) {
        if (e.keyCode === 13) {
          this.search();
          this.refs.search.blur();
        }
      },

      search: function () {
        if (this.state.search === this.state.lastSearch) {
          return;
        }
        this.state.pa.setPageNo(0).setParam("name", this.state.search).fetch();
        this.setState({
          lastSearch: this.state.search
        });
      },

      render: function () {

        return d.div({ className: "container" }, [
          d.h1({ key: "h1", className: "page-header" }, "Public Applications"),
          d.p({
            key: "lead",
            className: "lead"
          }, "You can use this page to register clients for other users' applications."),
          d.div({
            key: "search",
            className: "row"
          }, [
            d.div({ key: "search", className: "col-sm-8" }, d.div({ className: "form-group" }, d.input({
              type: "text",
              value: this.state.search,
              ref: "search",
              placeholder: "Search Text",
              className: "form-control",
              onChange: this.searchChange,
              onKeyDown: this.handleEnter
            }))),
            d.div({ key: "btn", className: "col-sm-4" }, d.div({ className: "form-group" }, btn({
              block: true, ajax: true,
              caption: "Search",
              icon: "search",
              disabled: this.state.search === this.state.lastSearch,
              onClick: _.bind(this.search, this)
            })))
          ]),
          lw({
            key: "pa",
            watch: this.state.pa
          }, rows({
            key: "rows",
            collection: this.state.pa,
            modelComponent: publicApp,
            xs: 6,
            sm: 4,
            emptyNode: alt({
              key: "empty",
              icon: "info",
              level: "info",
              strong: "Info",
              message: "No public applications found." + ((this.state.lastSearch !== "") ? " Please try a different search term." : "")
            })
          }))
        ]);
      }
    });
  });