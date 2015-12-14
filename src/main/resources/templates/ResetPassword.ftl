<!DOCTYPE html>
<html lang="en">
<#include "Head.ftl">
    <body>
        <div class="container-fluid">
            <div class="row">
                <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
                    <h1 class="page-header text-center">${model.application.name?html}</h1>
                </div>
            </div>
            <div class="row" id="form-row">
                <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
                    <form id="form-reset" method="POST">
                        <h3>
                            Reset Password
                        </h3>

                        <div class="form-group">
                            <label class="control-label" for="email">E-mail Address</label>
                            <input type="email" id="email" name="email" class="form-control"
                                   placeholder="E-mail address" required autofocus>
                        </div>

                        <#if model.referrer??>
                            <div class="row">
                                <div class="col-sm-6">
                                    <div class="form-group">
                                        <a class="btn btn-primary btn-block" id="back" href="${model.referrer?html}">
                                            <i class="fa fa-arrow-left"></i>
                                            <span>Back</span>
                                        </a>
                                    </div>
                                </div>
                                <div class="col-sm-6">
                                    <div class="form-group">
                                        <button class="btn btn-success btn-block" id="submitReset" type="submit">
                                            <i class="fa fa-envelope-o"></i>
                                            <span>Submit</span>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        <#else>
                            <div class="form-group">
                                <button class="btn btn-primary btn-block" id="submitReset" type="submit">
                                    <i class="fa fa-envelope-o"></i>
                                    <span>Submit</span>
                                </button>
                            </div>
                        </#if>
                    </form>

                    <#if model.success>
                        <div class="alert alert-success">
                            <i class="fa fa-check"></i>
                            <strong>Success</strong>
                            If your e-mail is associated with an account, you will receive an e-mail with a link to a page
                            to change your password that will work for five minutes.
                        </div>
                    </#if>

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
                            .find("#submitReset").prop("disabled", true).find("span").text("Working...").end()
                            .find("#back").prop("disabled", true).end()
                            .find("i").removeClass("fa-envelope-o").addClass("fa-pulse fa-spinner");
                });
            });
        </script>
    </body>
</html>