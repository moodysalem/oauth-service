<!DOCTYPE html>
<html lang="en">
<#include "Head.ftl">
    <body>
        <div class="container-fluid">
            <div class="row">
                <div class="col-lg-8 col-lg-offset-2">
                    <h1 class="page-header text-center">${model.client.application.name?html}</h1>
                </div>
            </div>

            <div class="row">
                <div class="col-lg-8 col-lg-offset-2">
                    <p class="lead text-center">
                        <strong>${model.client.name?html}</strong>
                        is requesting the following permissions
                    </p>

                    <form method="POST">
                        <#list model.clientScopes as cScope>
                            <div class="well well-sm">
                                <div class="row">
                                    <div class="col-sm-2 text-center">
                                        <img class="scope-thumbnail" src="${cScope.scope.thumbnail?html}">
                                    </div>
                                    <div class="col-sm-8">
                                        <h3>
                                        ${cScope.scope.displayName?html}
                                            <i class="fa fa-question-circle"
                                               data-title="${cScope.scope.description?html}"></i>
                                            <small>${(cScope.priority=="REQUIRE")?then("Required","")}</small>
                                        </h3>

                                        <p>${(cScope.reason)!"No reason given."?html}</p>
                                    </div>
                                    <div class="col-sm-2 text-center">
                                        <div class="toggle-checkbox">
                                            <input type="checkbox" checked
                                                   id="SCOPE${cScope.scope.id}" name="SCOPE${cScope.scope.id}"
                                                   name="SCOPE${cScope.scope.id}"
                                            ${(cScope.priority=="REQUIRE")?then("disabled","")} />
                                            <label for="SCOPE${cScope.scope.id}"></label>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </#list>

                        <input type="checkbox" name="rememberMe"
                               class="hidden" ${model.rememberMe?then("checked", "")} />


                        <div class="row">
                            <div class="col-sm-4 col-sm-offset-2">
                                <div class="form-group">
                                    <button onclick="window.location = window.location"
                                            class="btn btn-danger btn-block">
                                        <i class="fa fa-ban"></i>
                                        Cancel
                                    </button>
                                </div>
                            </div>
                            <div class="col-sm-4">
                                <div class="form-group">
                                    <button type="submit" class="btn btn-block btn-primary">
                                        <i class="fa fa-check"></i>
                                        Grant
                                    </button>
                                </div>
                            </div>
                        </div>
                        <input type="hidden" value="${model.token.token?html}" name="login_token">
                    </form>
                </div>
            </div>
        </div>
        <script>
            $(function () {
                $("[data-title]").tooltip();
            });
        </script>
    </body>

</html>
