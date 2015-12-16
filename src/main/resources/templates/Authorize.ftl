<!DOCTYPE html>
<html lang="en">
<head>
<#include "Head.ftl">
    <title>${model.client.application.name?html} Log In</title>
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-lg-8 col-lg-offset-2 col-sm-12">
            <h1 class="page-header text-center">${model.client.application.name?html}</h1>
        </div>
    </div>
    <div class="row" id="form-row">

        <div class="col-lg-4 col-lg-offset-2 col-sm-6">
            <form id="form-signin" method="POST">
                <input type="hidden" name="action" value="login"/>

                <h2>
                    Sign In<br/>
                    <small>${model.client.name?html}</small>
                </h2>
                <div class="form-group">
                    <label class="control-label" for="email">E-mail Address</label>
                    <input type="email" id="email" name="email" class="form-control"
                           <#if model.lastEmail??>value="${model.lastEmail}"</#if>
                           <#if !model.lastEmail??>autofocus</#if>
                           placeholder="E-mail address" required>
                </div>
                <div class="form-group">
                    <label class="control-label" for="password">Password</label>
                    <input type="password" id="password" name="password" class="form-control"
                           placeholder="Password" required
                           <#if model.lastEmail??>autofocus</#if>>
                </div>

                <div class="checkbox">
                    <label>
                        <input type="checkbox" name="rememberMe" checked> Remember me
                    </label>
                </div>

                <div class="form-group">
                    <button class="btn btn-primary btn-block" id="submitLogin" type="submit">
                        <i class="fa fa-sign-in"></i>
                        <span>Sign In</span>
                    </button>
                </div>

                <div class="form-group">
                    <a href="${model.baseUri?html}oauth/reset?applicationId=${model.client.application.id?c}&referrer=${model.requestUrl?url}">
                        Forgot Password?
                    </a>
                </div>

            <#-- open the alternative login button section -->
            <#if model.loginButtonSize gt 0>
                <div class="center-line text-center form-group">
                    <div class="or-block bg-info">OR</div>
                </div>

                <div class="row">
                    <#if model.facebookLogin>
                        <div class="col-xs-${model.loginButtonSize}">
                            <input type="hidden" id="facebookToken" name="facebookToken"/>
                            <button class="btn btn-sm btn-primary btn-block" id="facebookLogin" type="button">
                                <i class="fa fa-facebook fa-lg"></i>
                            </button>
                            <script>
                                // define a function to be called when facebook initializes
                                window.fbAsyncInit = function () {
                                    FB.init({
                                        appId: "${model.client.application.facebookAppId?c}",
                                        xfbml: false,
                                        version: 'v2.5'
                                    });

                                    // add a handler to the facebook login button
                                    $(function () {
                                        $("#facebookLogin").click(function () {
                                            if (FB && typeof FB.login === "function") {
                                                FB.login(function (response) {
                                                    if (response.status === 'connected') {
                                                        // Logged into your app and Facebook.
                                                        $("#facebookToken").val(response.authResponse.accessToken)
                                                                .closest("form").submit();
                                                    }
                                                }, { scope: "public_profile,email" });
                                            }
                                        });
                                    });
                                };

                                (function (d, s, id) {
                                    var js, fjs = d.getElementsByTagName(s)[ 0 ];
                                    if (d.getElementById(id)) {
                                        return;
                                    }
                                    js = d.createElement(s);
                                    js.id = id;
                                    js.src = "https://connect.facebook.net/en_US/sdk.js";
                                    fjs.parentNode.insertBefore(js, fjs);
                                }(document, 'script', 'facebook-jssdk'));
                            </script>
                        </div>
                    </#if>
                    <#if model.googleLogin>
                        <div class="col-xs-${model.loginButtonSize}">
                            <input type="hidden" id="googleToken" name="googleToken"/>
                            <button class="btn btn-sm btn-danger btn-block" id="googleLogin" type="button">
                                <i class="fa fa-google fa-lg"></i>
                            </button>
                            <script src="https://apis.google.com/js/platform.js"></script>
                            <script>
                                // define a function to be called when google loads
                                if (gapi && typeof gapi.load === "function") {
                                    gapi.load('auth2', function () {
                                        var auth2 = gapi.auth2.init({
                                            client_id: "${model.client.application.googleClientId?js_string}",
                                            fetch_basic_profile: true,
                                            scope: 'profile email'
                                        });
                                        $(function () {
                                            $("#googleLogin").click(function () {
                                                // Sign the user in, and then retrieve their ID token for the server
                                                // to validate
                                                auth2.signIn().then(function () {
                                                    var token = auth2.currentUser.get().getAuthResponse().access_token;
                                                    if (token) {
                                                        $("#googleToken").val(token).closest("form").submit();
                                                    }
                                                });
                                            });
                                        });
                                    });
                                }
                            </script>
                        </div>
                    </#if>
                    <#if model.amazonLogin>
                        <div class="col-xs-${model.loginButtonSize}">
                            <input type="hidden" id="amazonToken" name="amazonToken"/>
                            <button class="btn btn-sm btn-warning btn-block" id="amazonLogin" type="button">
                                <i class="fa fa-amazon fa-lg"></i>
                            </button>
                            <script>
                                var params = {
                                    scope: "profile",
                                    response_type: "token",
                                    client_id: "${model.client.application.amazonClientId?js_string}"
                                };

                                var redirect = "&redirect_uri=${model.baseUri?js_string}oauth/amazon";
                                var loginUrl = "https://www.amazon.com/ap/oa?";
                                var completeUrl = loginUrl + $.param(params, true) + redirect;

                                $(function () {
                                    var popup = null;

                                    $("#amazonLogin").click(function () {
                                        if (popup !== null && !popup.closed) {
                                            popup.location.href = completeUrl;
                                            return;
                                        }
                                        popup = window.open(completeUrl, "_blank", "height=640,width=810");
                                    });

                                    window.addEventListener("message", function (event) {
                                        if (event.origin !== window.location.origin) {
                                            return;
                                        }
                                        var d = event.data;
                                        if (typeof d.access_token === "string") {
                                            $("#amazonToken").val(d.access_token).closest("form").submit();
                                        }
                                        if (popup !== null && !popup.closed) {
                                            popup.close();
                                        }
                                    }, false);
                                });
                            </script>
                        </div>
                    </#if>
                </div>
            </#if>
            <#-- close the alternative login button section -->
            </form>

            <script>
                $(function () {
                    $("#form-signin").submit(function () {
                        $("#form-signin").find("input").prop("readOnly", true).end()
                                .find("button").prop("disabled", true)
                                .find("#submitLogin span").text("Signing in...").end()
                                .find("i.fa-sign-in").removeClass("fa-sign-in").addClass("fa-pulse fa-spinner");
                    });
                });
            </script>


        <#-- something happened while logging in -->
        <#if model.loginError??>
            <div class="alert alert-danger">
                <i class="fa fa-exclamation-triangle"></i>
                <strong>Error</strong>
            ${model.loginError?html}
            </div>
        </#if>
        </div>
        <div class="col-lg-4 col-sm-6">
            <form id="form-register" method="POST">
                <input type="hidden" name="action" value="register"/>

                <h2 class="form-signin-heading">
                    Register
                </h2>

                <div class="row">
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label for="firstName" class="control-label">First Name</label>
                            <input type="text" id="firstName" name="firstName" class="form-control"
                                   placeholder="First Name" required/>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label for="lastName" class="control-label">Last Name</label>
                            <input type="text" id="lastName" name="lastName" class="form-control"
                                   placeholder="Last Name" required/>
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <label class="control-label" for="registerEmail">E-mail Address</label>
                    <input type="email" id="registerEmail" name="registerEmail" class="form-control"
                           placeholder="E-mail address" required>
                </div>
                <div class="form-group">
                    <label class="control-label" for="registerPassword">Password</label>
                    <input type="password" id="registerPassword" name="registerPassword" class="form-control"
                           placeholder="Password" required>
                </div>

                <div class="form-group">
                    <label class="control-label" for="confirmPassword">Confirm Password</label>
                    <input type="password" id="confirmPassword" class="form-control"
                           placeholder="Confirm Password" required>
                </div>

                <button class="btn btn-success btn-block" id="submitRegister" type="submit">
                    <i class="fa fa-user-plus"></i>
                    <span>Register</span>
                </button>
            </form>

            <script>
                $(function () {
                    $("#form-register").submit(function () {
                        $("#form-register").find("input").prop("readOnly", true).end()
                                .find("#submitRegister").prop("disabled", true)
                                .find("span").text("Submitting...").end()
                                .find("i").removeClass("fa-user-plus").addClass("fa-pulse fa-spinner");
                    });
                });
            </script>

        <#if model.registerError??>
            <div class="alert alert-danger">
                <i class="fa fa-exclamation-triangle"></i>
                <strong>Error</strong>
            ${model.registerError}
            </div>
        </#if>
        <#if model.registerSuccess>
            <div class="alert alert-success">
                <i class="fa fa-check"></i>
                <strong>Success</strong>
                You will receive a confirmation e-mail. You must confirm your e-mail before you can sign in.
            </div>
        </#if>

        </div>
    </div>
</div>
</body>
</html>