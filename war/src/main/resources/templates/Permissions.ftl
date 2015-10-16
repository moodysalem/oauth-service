<!DOCTYPE html>
<html lang="en">
<#include "Head.ftl">
    <body>
        <div class="container">
            <h2>Grant these Permissions?</h2>

            <form method="POST">
            <#list model.clientScopes as cScope>
                <#escape x as x?html>
                <div class="row client-scope">
                    <div class="col-sm-2 text-center vcenter">
                        <#if cScope.priority != "REQUIRE">
                            <input id="SCOPE${cScope.scope.id}" name="SCOPE${cScope.scope.id}" type="checkbox" checked/>
                        </#if>
                    </div><!--
                    --><div class="col-sm-2 vcenter">
                        <label for="SCOPE${cScope.scope.id}">
                            <img class="scope-thumbnail" src="${cScope.scope.thumbnail}"/>
                        </label>
                    </div><!--
                    --><div class="col-sm-8 vcenter">
                        <label for="SCOPE${cScope.scope.id}">
                        ${cScope.scope.displayName}
                        </label>
                    </div>
                </div>
                </#escape>
            </#list>
                <input type="hidden" name="rememberMe" value="${model.rememberMe?then("on", "off")}}" />
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
