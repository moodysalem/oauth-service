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
                <input type="hidden" name="action" value="permissions"/>

            <#list model.clientScopes as cScope>
                <div class="well well-sm">
                    <div class="client-scope-row">
                        <div class="client-scope-thumbnail">
                            <#if cScope.scope.thumbnail??>
                                <img class="scope-thumbnail" src="${cScope.scope.thumbnail?html}">
                            </#if>
                        </div>
                        <div class="client-scope-description">
                            <h3>
                            ${cScope.scope.displayName?html}
                                <i class="fa fa-question-circle"
                                   data-title="${cScope.scope.description?html}"></i>
                            </h3>

                            <p>${(cScope.reason)!"No reason given."?html}</p>
                        </div>
                        <div class="client-scope-toggle">
                            <#if (cScope.priority == "REQUIRE")>
                                REQUIRED
                            <#else>
                                <div class="toggle-checkbox">
                                    <input type="checkbox" checked
                                           id="SCOPE${cScope.scope.id?c}"
                                           name="SCOPE${cScope.scope.id?c}"/>
                                    <label for="SCOPE${cScope.scope.id?c}"></label>
                                </div>
                            </#if>
                        </div>
                    </div>
                </div>
            </#list>

                <input type="checkbox" name="rememberMe"
                       class="hidden" ${model.rememberMe?then("checked", "")}/>


                <div class="row">
                    <div class="col-sm-4 col-sm-offset-2">
                        <div class="form-group">
                            <a href="${model.cancelUrl?html}" class="btn btn-danger btn-block">
                                <i class="fa fa-ban"></i>
                                Cancel
                            </a>
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
