<!DOCTYPE html>
<html lang="en">
<#include "Head.ftl">
    <body>
        <div class="container-fluid">
            <div class="row">
                <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
                    <h1 class="page-header text-center">${model.userCode.user.application.name?html}</h1>
                </div>
            </div>
            <div class="row" id="form-row">
                <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
                    <div class="alert alert-${model.alertLevel?html}">
                        ${model.message?html}
                        Click <a class="alert-link" href="${model.userCode.referrer?html}">here</a> to return to where you
                        registered.
                    </div>
                </div>
            </div>
        </div>

    </body>
</html>