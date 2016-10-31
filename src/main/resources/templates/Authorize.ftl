<!DOCTYPE html>
<html lang="en">
<head>
<#include "Head.ftl">
    <title>${model.client.application.name?html} Log In</title>
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
            <h1 class="page-header text-center">${model.client.application.name?html}</h1>
        </div>
    </div>
    <div class="row">
        <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
            <form id="form-signin" method="POST">
                <input type="hidden" name="action" value="email"/>

                <h2>
                    Sign In<br/>
                    <small>${model.client.name?html}</small>
                </h2>
                <div class="form-group">
                    <label class="control-label" for="email">E-mail Address</label>
                    <input type="email" id="email" name="email" class="form-control"
                           autofocus placeholder="E-mail address" required>
                </div>

                <div class="checkbox">
                    <label>
                        <input type="checkbox" name="remember_me" checked> Remember me for 1 month
                    </label>
                </div>

                <div class="form-group">
                    <button class="btn btn-primary btn-block" id="submit-login" type="submit">
                        <i class="fa fa-sign-in"></i>
                        <span>Submit</span>
                    </button>
                </div>
            </form>

        <#if model.sentEmail>
            <div class="alert alert-info" id="sent-email-alert">
                <i class="fa fa-envelope"></i>
                Your log in e-mail has been sent.
            </div>
        <#elseif model.loginErrorCode??>
            <div class="alert alert-danger" id="error-code-alert">
                <i class="fa fa-exclamation-triangle"></i>
            ${model.loginErrorCode.message?html}
            </div>
        </#if>


        <#-- google login -->
        <#if model.client.application.googleCredentials??>
            <hr/>
            <form>
                <input type="hidden" name="action" value="google"/>
                <input type="hidden" id="google-token" name="google_token"/>
                <button class="btn btn-sm btn-danger btn-block" id="google-login" type="button">
                    <i class="fa fa-google fa-lg"></i>
                </button>
            </form>
            <script src="https://apis.google.com/js/platform.js"></script>
            <script>
                var loginButton = $('#google-login');
                var tokenInput = $('#google-token');

                function disableLoginButton() {
                    loginButton.prop("disabled", true);
                }

                // define a function to be called when google loads
                if (gapi && typeof gapi.load === "function") {
                    gapi.load('auth2', function () {
                        if (!gapi.auth2 || !gapi.auth2.init) {
                            disableLoginButton();
                            return;
                        }

                        var auth2 = gapi.auth2.init({
                            client_id: "${model.client.application.googleCredentials.id?js_string}",
                            fetch_basic_profile: true,
                            scope: 'profile email'
                        });

                        $(function () {
                            loginButton.click(function () {
                                // Sign the user in, and then retrieve their ID token for the server
                                // to validate
                                auth2.signIn()
                                        .then(
                                                function () {
                                                    var token = auth2.currentUser.get().getAuthResponse().access_token;
                                                    if (token) {
                                                        tokenInput.val(token).closest("form").submit();
                                                    }
                                                }
                                        );
                            });
                        });
                    });
                } else {
                    disableLoginButton();
                }
            </script>
        </#if>
        <#-- close the alternative login button section -->

            <script>
                function showLoadingIndicators() {
                    $("#form-signin").find("input").prop("readOnly", true).end()
                            .find("button").prop("disabled", true)
                            .find("#submit-login span").text("Signing in...").end()
                            .find("i.fa-sign-in").removeClass("fa-sign-in").addClass("fa-pulse fa-spinner");
                }

                $(function () {
                    $("#form-signin").submit(showLoadingIndicators);
                });
            </script>
        </div>
    </div>
</div>
</body>
</html>