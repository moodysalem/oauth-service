/**
 *
 */
define([ "react", "rbs", "underscore", "./MustBeLoggedIn", "model", "rbs/components/combo/Table", "js/Models", "./Loading",
    "./AppHeader", "rbs/components/controls/Pagination", "rbs/components/controls/Button", "./UserModal", "rbs/mixins/Model", "./ConfirmDeleteModal" ],
  function (React, rbs, _, mbli, m, table, mdls, lw, ah, pag, btn, um, model, confirmDelete) {
    "use strict";

    var util = rbs.util;
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
      },
      {
        component: util.rf({
          displayName: "button column",

          mixins: [ model ],

          getInitialState: function () {
            return {
              editOpen: false,
              deleteOpen: false,
              userCopy: new mdls.User()
            };
          },

          openEdit: function () {
            this.state.userCopy.set(this.props.model.toJSON());
            this.setState({
              editOpen: true
            });
          },

          closeEdit: function () {
            this.setState({
              editOpen: false
            });
          },

          openDelete: function () {
            this.setState({
              deleteOpen: true
            });
          },
          closeDelete: function () {
            this.setState({
              deleteOpen: false
            });
          },

          render: function () {
            return d.div({}, [
              d.div({ key: "btn", className: "btn-container text-center" }, [
                btn({
                  key: "edit",
                  icon: "pencil",
                  caption: "Edit",
                  type: "warning",
                  size: "xs",
                  onClick: this.openEdit
                }),
                btn({
                  key: "del",
                  icon: "trash",
                  caption: "Delete",
                  size: "xs",
                  type: "danger",
                  onClick: this.openDelete
                })
              ]),
              um({
                key: "modal",
                model: this.state.userCopy,
                open: this.state.editOpen,
                title: "Edit User: " + this.state.model.email,
                onClose: this.closeEdit,
                onSave: _.bind(function (model) {
                  this.props.model.set(model.toJSON());
                  this.closeEdit();
                }, this)
              }),
              confirmDelete({
                key: "del",
                title: "Confirm Delete: " + this.state.model.email,
                open: this.state.deleteOpen,
                onClose: this.closeDelete,
                deleteMessage: "This will remove all information about the user. This operation cannot be undone.",
                onDelete: _.bind(function () {
                  this.props.model.destroy({ wait: true });
                }, this)
              })
            ]);
          }
        })
      }
    ];

    return util.rf({
      displayName: "Users",

      getInitialState: function () {
        return {
          users: new mdls.Users().setParam("applicationId", this.props.applicationId),
          app: new mdls.Application({ id: this.props.applicationId }),
          search: "",
          lastSearch: "",
          newUser: new mdls.User(),
          createOpen: false
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

      openCreate: function () {
        this.state.newUser.clear();
        this.state.newUser.set({
          application: { id: this.props.applicationId }
        });
        this.setState({
          createOpen: true
        });
      },

      closeCreate: function () {
        this.setState({
          createOpen: false
        });
      },

      render: function () {
        if (!m.isLoggedIn()) {
          return mbli();
        }

        return d.div({
          className: "container"
        }, [
          ah({ title: "Users", key: "he", model: this.state.app, onCreate: this.openCreate }),
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
          ]),
          um({
            onSave: _.bind(function () {
              this.state.users.fetch();
              this.closeCreate();
            }, this),
            model: this.state.newUser,
            key: "um",
            title: "Create User",
            open: this.state.createOpen,
            onClose: this.closeCreate
          })
        ]);
      }
    });
  });