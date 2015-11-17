/**
 *
 */
define([ "react", "util", "underscore", "./MustBeLoggedIn", "model", "rbs/components/combo/Table", "js/Models", "./Loading",
    "./AppHeader", "rbs/components/controls/Pagination", "rbs/components/controls/Button" ],
  function (React, util, _, mbli, m, table, mdls, lw, ah, pag, btn) {
    "use strict";

    var d = React.DOM;
    var rpt = React.PropTypes;

    var userAttributes = [
      {
        attribute: "email",
        sortOn: "email",
        label: "E-mail",
        component: d.span
      },
      {
        attribute: "firstName",
        sortOn: "firstName",
        label: "First Name",
        component: d.span
      },
      {
        attribute: "lastName",
        sortOn: "lastName",
        label: "Last Name",
        component: d.span
      },
      {
        attribute: "verified",
        sortOn: "verified",
        label: "Verified",
        component: d.span,
        formatFunction: function (val) {
          if (val) {
            return "Yes";
          } else {
            return "No";
          }
        }
      }
    ];

    return util.rf({
      displayName: "Users",

      getInitialState: function () {
        return {
          users: new mdls.Users().setParam("applicationId", this.props.applicationId),
          app: new mdls.Application({ id: this.props.applicationId }),
          search: "",
          lastSearch: ""
        };
      },

      componentDidMount: function () {
        if (m.isLoggedIn()) {
          this.state.users.fetch();
          this.state.app.fetch();
        }
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
        this.state.users.setPageNo(0).setParam("search", this.state.search).fetch();
        this.setState({
          lastSearch: this.state.search
        });
      },

      searchChange: function (e) {
        this.setState({
          search: e.target.value
        });
      },

      render: function () {
        if (!m.isLoggedIn()) {
          return mbli();
        }

        return d.div({
          className: "container"
        }, [
          ah({ title: "Users", key: "he", model: this.state.app }),
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
          lw({ key: "table", watch: this.state.users }, [
            table({
              key: "t",
              collection: this.state.users,
              attributes: userAttributes
            }),
            d.div({
              className: "text-center",
              key: "pag"
            }, pag({
              collection: this.state.users
            }))
          ])
        ]);
      }
    });
  });