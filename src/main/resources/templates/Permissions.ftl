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
                <#assign
                clientScope=userClientScope.clientScope
                scope=userClientScope.clientScope.scope
                acceptedScope=userClientScope.acceptedScope>
                <div class="well well-sm">
                    <div class="client-scope-row">
                        <div class="client-scope-thumbnail">
                            <#if scope.thumbnail??>
                                <img class="scope-thumbnail" src="${scope.thumbnail?html}">
                            </#if>
                        </div>
                        <div class="client-scope-description">
                            <h3>
                            ${scope.displayName?html}
                                <i class="fa fa-question-circle"
                                   data-title="${scope.description?html}"></i>
                            </h3>

                            <p>${(clientScope.reason)!"No reason given."?html}</p>
                        </div>
                        <div class="client-scope-toggle">
                            <#if acceptedScope??>
                                ACCEPTED
                            <#else>
                                <#if (clientScope.priority == "REQUIRED")>
                                    REQUIRED
                                <#else>
                                    <div class="toggle-checkbox">
                                        <input title="${scope.name?html}" type="checkbox" checked
                                               name="SCOPE-${scope.id}"/>
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
