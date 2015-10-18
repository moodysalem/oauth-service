<!DOCTYPE html>
<html lang="en">
<#include "Head.ftl">
    <body>
        <div class="container-fluid">
            <div class="row">
                <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
                    <h1 class="page-header text-center">${model.passwordResetCode.user.application.name?html}</h1>
                </div>
            </div>
            <div class="row" id="form-row">
                <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
                    <#if !model.passwordResetCode.used>
                    <form id="form-reset" method="POST">
                        <h2>
                            Change Password
                        </h2>

                        <div class="form-group">
                            <label class="control-label" for="password">New Password</label>
                            <input type="password" id="password" name="password" class="form-control input-lg"
                                   placeholder="New Password" required autofocus>
                        </div>
                        <div class="form-group">
                            <label class="control-label" for="confirmPassword">Confirm New Password</label>
                            <input type="password" id="confirmPassword" class="form-control input-lg"
                                   placeholder="Confirm New Password" required>
                        </div>

                        <input type="hidden" name="code" value="${model.passwordResetCode.code?html}"/>

                        <div class="form-group">
                            <button class="btn btn-lg btn-primary btn-block" id="submitReset" type="submit">
                                <i class="fa fa-pencil"></i>
                                <span>Submit</span>
                            </button>
                        </div>
                    </form>
                    <#else>
                        <div class="alert alert-success">
                            <i class="fa fa-check"></i>
                            <strong>Success</strong> Your password has been changed.
                        </div>
                    </#if>
                </div>
            </div>
        </div>

        <script>
            $(function () {
                $("#form-reset").submit(function () {
                    $("#form-reset").find("input").prop("readOnly", true).end()
                            .find("#submitReset").prop("disabled", true)
                            .find("span").text("Working...").end()
                            .find("i").removeClass("fa-envelope-o").addClass("fa-pulse fa-spinner");
                });
            });
        </script>
    </body>
</html>