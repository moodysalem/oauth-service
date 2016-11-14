<!DOCTYPE html>
<html lang="en">
<#include "Head.ftl">
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-lg-8 col-lg-offset-2">
            <h1 class="page-header text-center">${model.loginCode.client.application.name?html}</h1>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-8 col-lg-offset-2">
            <p class="lead text-center">
            <#if model.alreadyAuthorized>
                You have already authorized <strong>${model.loginCode.client.name?html}</strong>
            <#else>
                <strong>${model.loginCode.client.name?html}</strong> is requesting the following permissions
            </#if>
            </p>

            <form method="POST">
            <#list model.userClientScopes as userClientScope>
                <#assign clientScope=userClientScope.clientScope scope=userClientScope.clientScope.scope>
                <div class="well well-sm">
                    <div class="display-flex flex-wrap-wrap justify-content-center align-items-center">
                        <div class="flex-shrink-0" style="padding: 0.5em; width: 128px;">
                            <img class="scope-thumbnail" style="width:100%;"
                                 src="${(scope.thumbnail)!"https://placehold.it/128x128"?html}">
                        </div>
                        <div class="flex-grow-1 text-center">
                            <h3>
                            ${scope.displayName?html}
                                <i class="fa fa-question-circle"
                                   data-title="${scope.description?html}"></i>
                            </h3>

                            <p>${(clientScope.reason)!"No reason given."?html}</p>
                        </div>
                        <div class="flex-shrink-0" style="width: 120px; text-align:center;">
                            <#if userClientScope.accepted>
                                Accepted <i class="fa fa-question-circle"
                                            data-title="You have already accepted this scope"></i>
                            <#else>
                                <#if (clientScope.priority == "REQUIRED")>
                                    Required <i class="fa fa-question-circle"
                                                data-title="This scope is required to log in for this client"></i>
                                <#else>
                                    <div class="toggle-checkbox">
                                        <input title="${scope.name?html}" type="checkbox" checked
                                               id="scope-${clientScope.id}"
                                               name="SCOPE-${clientScope.id}"/>
                                        <label for="scope-${clientScope.id}"></label>
                                    </div>
                                </#if>
                            </#if>
                        </div>
                    </div>
                </div>
            </#list>

                <div class="row">
                    <div class="col-sm-4 col-sm-offset-2">
                        <div class="form-group">
                            <button class="btn btn-danger btn-block" type="submit" name="action" value="cancel">
                                <i class="fa fa-ban"></i>
                                Cancel
                            </button>
                        </div>
                    </div>
                    <div class="col-sm-4">
                        <div class="form-group">
                            <button class="btn btn-block btn-primary" type="submit" name="action" value="ok">
                                <i class="fa fa-check"></i>
                                OK
                            </button>
                        </div>
                    </div>
                </div>
            </form>

        </div>
    </div>
</div>
<script>
    $(function () {
        $("i[data-title]").tooltip();
    });
</script>
</body>

</html>
