<!DOCTYPE html>
<html lang="en">
<#include "Head.ftl">
    <body>
        <div class="container">
            <form class="form-signin" method="POST">
                <h2 class="form-signin-heading">
                    <#escape x as x?html>
                    ${model.client.application.name}
                    <small>${model.client.name}</small>
                    </#escape>
                </h2>

                <label for="inputEmail" class="sr-only">E-mail address</label>
                <input type="email" id="inputEmail" name="email" class="form-control" placeholder="E-mail address"
                       required
                       autofocus>
                <label for="inputPassword" class="sr-only">Password</label>
                <input type="password" id="inputPassword" name="password" class="form-control" placeholder="Password"
                       required>

                <div class="checkbox">
                    <label>
                        <input type="checkbox" name="rememberMe"> Remember me
                    </label>
                </div>

                <div class="form-group">
                    <button class="btn btn-lg btn-primary btn-block" type="submit">
                        <i class="fa fa-sign-in"></i>
                        Sign In
                    </button>
                </div>

                <#escape x as x?html>
                <#-- failed to log in -->
                    <#if model.loginError??>
                        <div class="alert alert-danger">${model.loginError}</div>
                    </#if>
                </#escape>
            </form>
        </div>
    </body>
</html>
