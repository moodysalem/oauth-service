<form method="POST">
    <input type="hidden" name="action" value="facebook"/>

    <input type="hidden" id="facebook-token" name="facebook_token"/>

    <button class="btn btn-sm btn-primary btn-block" id="facebook-login" type="button" disabled>
        <i class="fa fa-facebook-square fa-lg"></i>
        <span class="hidden-xs">via Facebook</span>
    </button>
</form>

<script>
    (function () {
        var btn = $('#facebook-login'),
                input = $('#facebook-token');

        window.fbAsyncInit = function () {
            btn.prop("disabled", false);

            FB.init({
                appId: '${model.client.application.facebookCredentials?js_string}',
                xfbml: false,
                version: 'v2.8'
            });


            btn.click(function () {
                FB.login(function (response) {
                    if (response && response.status == 'connected' &&
                            response.authResponse && response.authResponse.accessToken) {
                        input.val(response.authResponse.accessToken).closest('form').submit();
                    }
                }, {scope: 'email'});
            });
        };

        (function (d, s, id) {
            var js, fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id)) {
                return;
            }
            js = d.createElement(s);
            js.id = id;
            js.src = "//connect.facebook.net/en_US/sdk.js";
            fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'facebook-jssdk'));
    })();
</script>