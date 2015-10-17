<!DOCTYPE html>
<html lang="en">
<#include "Head.ftl">
    <body>
        <div class="container-fluid">
            <div class="row">
                <div class="col-lg-8 col-lg-offset-2 col-sm-12">
                    <h1 class="page-header text-center">${model.application.name?html}</h1>
                </div>
            </div>
            <div class="row" id="form-row">

                <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
                    <form id="form-reset" method="POST">
                        <h2>
                            Reset Password
                        </h2>

                        <div class="form-group">
                            <label class="control-label" for="email">E-mail Address</label>
                            <input type="email" id="email" name="email" class="form-control input-lg"
                                   placeholder="E-mail address" required autofocus>
                        </div>

                        <div class="form-group">
                            <button class="btn btn-lg btn-primary btn-block" id="submitReset" type="submit">
                                <i class="fa fa-envelope-o"></i>
                                <span>Submit</span>
                            </button>
                        </div>
                    </form>

                <#-- something happened while logging in -->
                <#if model.error??>
                    <div class="alert alert-danger">
                        <i class="fa fa-exclamation-triangle"></i>
                        <strong>Error</strong>
                    ${model.error?html}
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