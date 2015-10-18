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
                        <h2>
                            Sign In<br/>
                            <small>${model.client.name?html}</small>
                        </h2>
                        <div class="form-group">
                            <label class="control-label" for="email">E-mail Address</label>
                            <input type="email" id="email" name="email" class="form-control input-lg"
                                   placeholder="E-mail address" required autofocus>
                        </div>
                        <div class="form-group">
                            <label class="control-label" for="password">Password</label>
                            <input type="password" id="password" name="password" class="form-control input-lg"
                                   placeholder="Password" required>
                        </div>

                        <div class="checkbox">
                            <label>
                                <input type="checkbox" name="rememberMe" checked> Remember me
                            </label>
                        </div>

                        <div class="form-group">
                            <button class="btn btn-lg btn-primary btn-block" id="submitLogin" type="submit">
                                <i class="fa fa-sign-in"></i>
                                <span>Sign In</span>
                            </button>
                        </div>

                        <div class="form-group">
                            <a href="/reset?applicationId=${model.client.application.id}">Forgot Password?</a>
                        </div>

                        <div class="center-line text-center form-group">
                            <div class="or-block bg-info">OR</div>
                        </div>

                        <div class="row">
                            <div class="col-xs-4">
                            <#if model.client.application.facebookAppId??>
                                <input type="hidden" id="facebookToken" name="facebookToken"/>
                                <button class="btn btn-sm btn-primary btn-block" id="facebookLogin" type="button">
                                    <i class="fa fa-facebook fa-2x"></i>
                                </button>
                                <script>
                                    window.fbAsyncInit = function () {
                                        FB.init({
                                            appId: "${model.client.application.facebookAppId?c}",
                                            xfbml: false,
                                            version: 'v2.5'
                                        });
                                    };

                                    (function (d, s, id) {
                                        var js, fjs = d.getElementsByTagName(s)[ 0 ];
                                        if (d.getElementById(id)) {
                                            return;
                                        }
                                        js = d.createElement(s);
                                        js.id = id;
                                        js.src = "//connect.facebook.net/en_US/sdk.js";
                                        fjs.parentNode.insertBefore(js, fjs);
                                    }(document, 'script', 'facebook-jssdk'));

                                    $(function () {
                                        $("#facebookLogin").click(function () {
                                            FB.login(function (response) {
                                                console.log(response);
                                                if (response.status === 'connected') {
                                                    // Logged into your app and Facebook.
                                                    $("#facebookToken").val(response.authResponse.accessToken)
                                                            .closest("form").submit();
                                                }
                                            }, { scope: "public_profile,email" });
                                        });
                                    });
                                </script>
                            </#if>
                            </div>
                            <div class="col-xs-4">
                            <#if model.client.application.googleClientId??>
                                <input type="hidden" id="googleToken" name="googleToken"/>
                                <button class="btn btn-sm btn-danger btn-block" id="googleLogin" type="button">
                                    <i class="fa fa-google fa-2x"></i>
                                </button>
                                <script src="//apis.google.com/js/platform.js" onload="initGoogle();" async
                                        defer></script>
                                <script>
                                    var initGoogle = function () {
                                        gapi.load('auth2', function () {
                                            var auth2 = gapi.auth2.init({
                                                client_id: "${model.client.application.googleClientId?js_string}",
                                                fetch_basic_profile: true,
                                                scope: 'profile'
                                            });
                                            $(function () {
                                                $("#googleLogin").click(function () {
                                                    // Sign the user in, and then retrieve their ID token for the server
                                                    // to validate
                                                    auth2.signIn().then(function () {
                                                        var token = auth2.currentUser.get().getAuthResponse().id_token;
                                                        if (token) {
                                                            $("#googleToken").val(token).closest("form").submit();
                                                        }
                                                    });
                                                });
                                            });
                                        });
                                    };
                                </script>
                            </#if>
                            </div>
                            <div class="col-xs-4">
                            <#if model.client.application.twitterConsumerSecret??>
                                <input type="hidden" id="amazonToken" name="amazonToken"/>
                                <button class="btn btn-sm btn-info btn-block" id="amazonLogin" type="button">
                                    <i class="fa fa-amazon fa-2x"></i>
                                </button>
                                <script>
                                    // this is custom code for opening a popup to the twitter page
                                    $(function () {
                                        $("#amazonLogin").click(function () {
                                            window.twitterLogin =
                                                    window.open("${model.baseUrl?js_string}twitter?clientId=${model.client.identifier?js_string}",
                                                            "_blank", "toolbar=no, scrollbars=yes, resizable=yes, top=500, left=500, width=800, height=800");
                                        });
                                    });
                                </script>
                            </#if>
                            </div>
                        </div>

                    </form>


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
                    <form id="form-register" method="POST" action="/register">
                        <h2 class="form-signin-heading">
                            Register
                        </h2>

                        <div class="row">
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="firstName" class="control-label">First Name</label>
                                    <input type="text" id="firstName" name="firstName" class="form-control input-lg"
                                           placeholder="First Name" required/>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <div class="form-group">
                                    <label for="lastName" class="control-label">Last Name</label>
                                    <input type="text" id="lastName" name="lastName" class="form-control input-lg"
                                           placeholder="Last Name" required/>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label" for="registerEmail">E-mail Address</label>
                            <input type="email" id="registerEmail" name="email" class="form-control input-lg"
                                   placeholder="E-mail address" required autofocus>
                        </div>
                        <div class="form-group">
                            <label class="control-label" for="registerPassword">Password</label>
                            <input type="password" id="registerPassword" name="password" class="form-control input-lg"
                                   placeholder="Password" required>
                        </div>

                        <div class="form-group">
                            <label class="control-label" for="confirmPassword">Confirm Password</label>
                            <input type="password" id="confirmPassword" class="form-control input-lg"
                                   placeholder="Confirm Password" required>
                        </div>

                        <button class="btn btn-lg btn-success btn-block" id="submitRegister" type="submit">
                            <i class="fa fa-user-plus"></i>
                            <span>Register</span>
                        </button>
                    </form>
                </div>
            </div>
        </div>

        <script>
            $(function () {
                $("#form-signin").submit(function () {
                    $("#form-signin").find("input").prop("readOnly", true).end()
                            .find("button").prop("disabled", true)
                            .find("#submitLogin span").text("Signing in...").end()
                            .find("i.fa-sign-in").removeClass("fa-sign-in").addClass("fa-pulse fa-spinner");
                });

                $("#form-register").submit(function () {
                    $("#form-register").find("input").prop("readOnly", true).end()
                            .find("#submitRegister").prop("disabled", true)
                            .find("span").text("Submitting...").end()
                            .find("i").removeClass("fa-user-plus").addClass("fa-pulse fa-spinner");
                });
            });
        </script>
    </body>
</html>