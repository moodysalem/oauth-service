<!DOCTYPE html>
<html lang="en">
<#include "Head.ftl">
    <body>
        <div class="container">
            <h2>Authorize These Permissions</h2>

            <form method="POST">
            <#list model.clientScopes as cScope>
                <div class="row client-scope">
                    <div class="col-sm-2 text-center">
                        <input id="${cScope.scope.name}" name="${cScope.scope.name}" type="checkbox" checked/>
                    </div>
                    <div class="col-sm-2">
                        <img class="scope-thumbnail" src="${cScope.scope.thumbnail}"/>
                    </div>
                    <div class="col-sm-8">
                        <label for="${cScope.scope.name}">
                        ${cScope.scope.displayName}
                        </label>
                    </div>
                </div>
            </#list>
                <div class="row">
                    <div class="col-sm-6">
                        <button onclick="window.location = window.location" class="btn btn-danger btn-block">
                            <i class="fa fa-ban"></i>
                            Cancel
                        </button>
                    </div>
                    <div class="col-sm-6">
                        <button type="submit" class="btn btn-block btn-primary">
                            <i class="fa fa-check"></i>
                            Grant
                        </button>
                    </div>
                </div>
                <input type="hidden" value="${model.token.token}" name="login_token"/>
            </form>
        </div>
    </body>
</html>
