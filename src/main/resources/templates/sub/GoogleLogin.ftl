<form method="POST">
    <input type="hidden" name="action" value="google"/>

    <input type="hidden" id="google-token" name="google_token"/>

    <button class="btn btn-sm btn-danger btn-block" id="google-login" type="button" disabled>
        <i class="fa fa-google fa-lg"></i>
        <span class="hidden-xs">via Google</span>
    </button>
</form>


<script src="https://apis.google.com/js/platform.js"></script>

<script>
    (function () {
        var loginButton = $('#google-login'),
                tokenInput = $('#google-token');

        function enableGoogleButton() {
            loginButton.prop("disabled", false);
        }

        if (gapi && typeof gapi.load == 'function') {
            gapi.load('auth2', function () {
                if (!gapi.auth2 || !gapi.auth2.init) {
                    return;
                }

                enableGoogleButton();

                var auth2 = gapi.auth2.init({
                    client_id: "${model.client.application.googleCredentials.id?js_string}",
                    fetch_basic_profile: true,
                    scope: 'email'
                });

                loginButton.click(function () {
                    // Sign the user in, and then retrieve their ID token for the server
                    // to validate
                    auth2.signIn()
                            .then(
                                    function () {
                                        var token = auth2.currentUser.get().getAuthResponse().id_token;
                                        if (token) {
                                            tokenInput.val(token).closest("form").submit();
                                        }
                                    }
                            );
                });
            });
        }
    })();
</script>