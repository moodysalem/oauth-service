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
                            <button class="btn btn-lg btn-primary btn-block" id="submitReset" disabled type="submit">
                                <i class="fa fa-pencil"></i>
                                <span>Submit</span>
                            </button>
                        </div>
                    </form>
                <#else>
                    <div class="alert alert-success">
                        <i class="fa fa-check"></i>
                        <strong>Success</strong> Your password has been changed.
                        <#if model.passwordResetCode.referer??>
                            Click <a class="alert-link" href="${model.passwordResetCode.referer?html}">here</a> to
                            return.
                        </#if>
                    </div>
                </#if>
                </div>
            </div>
        </div>

        <script>
            $(function () {
                var submitting = false;
                $("#form-reset").submit(function () {
                    submitting = true;
                    $("#form-reset").find("input").prop("readOnly", true).end()
                            .find("#submitReset").prop("disabled", true)
                            .find("span").text("Working...").end()
                            .find("i").removeClass("fa-envelope-o").addClass("fa-pulse fa-spinner");
                });

                var cp = $("#confirmPassword");
                var cpfg = cp.closest(".form-group");
                var pw = $("#password");
                var sr = $("#submitReset");

                $("#confirmPassword, #password").on("change keyup", function () {
                    var p1 = pw.val();
                    var p2 = cp.val();
                    if (typeof p1 === "string" && p1.length > 0 && p2 === p1) {
                        if (!submitting) {
                            sr.prop("disabled", false);
                        } else {
                            sr.prop("disabled", true);
                        }
                    } else {
                        sr.prop("disabled", true);
                    }
                });

                cp.on("focus blur keyup change", function () {
                    if (cp.is(":focus")) {
                        var p1 = pw.val();
                        var p2 = cp.val();
                        if (p1 !== p2) {
                            cpfg.addClass("has-error");
                        } else {
                            cpfg.removeClass("has-error");
                        }
                    } else {
                        cpfg.removeClass("has-error");
                    }
                });
            });
        </script>
    </body>
</html>