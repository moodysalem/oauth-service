<!DOCTYPE html>
<html lang="en">
<head>
<#include "Head.ftl">
    <title>${model.client.name?html} - ${model.client.application.name?html}</title>
</head>
<body>

<div class="container-fluid">
    <div class="row">
        <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
            <h1 class="page-header text-center overflow-ellipsis">${model.client.application.name?html}</h1>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
            <h2>
                Sign In
                <br/>
                <div class="overflow-ellipsis">
                    <small>${model.client.name?html}</small>
                </div>
            </h2>

            <div class="padded-card">
            <#-- google login -->
            <#if model.client.application.googleCredentials??>
                <#include "sub/GoogleLogin.ftl">
                <hr/>
            </#if>

            <#-- facebook login -->
            <#if model.client.application.facebookCredentials??>
                <#include "sub/FacebookLogin.ftl">
                <hr/>
            </#if>

            <#-- e-mail sign in always available -->
            <#include "sub/EmailLogin.ftl">
            </div>

            <script>
                // if any form submits, show loading indicators
                $('form').submit(function () {
                    $('button')
                            .prop('disabled', true)
                            .find('i')
                            .removeClass()
                            .addClass('fa fa-pulse fa-spinner');
                });
            </script>

        <#if model.loginErrorCode??>
            <div class="alert alert-danger" id="error-code-alert" style="margin-top: 10px;">
                <i class="fa fa-exclamation-triangle"></i> ${model.loginErrorCode.message?html}
            </div>
        </#if>

        </div>
    </div>
</div>
</body>
</html>