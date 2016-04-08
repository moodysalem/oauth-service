<!DOCTYPE html>
<html>
<head></head>
<body>
<script>
    (function () {
        "use strict";
        var message;
    <#if model.loginCookie??>
        message = {
            status: "logged_in",
            token: null
        };

        <#if model.tokenResponse??>
            message.token = {
                access_token: "${model.tokenResponse.accessToken?js_string}",
                expires_in: ${model.tokenResponse.expiresIn?c},
                scope: "${model.tokenResponse.scope?js_string}",
                token_type: "${model.tokenResponse.tokenType?js_string}",
                client_id: "${model.tokenResponse.clientId?js_string}",
                application_id: "${model.tokenResponse.applicationId}",
                user_details: null
            };

            <#if model.tokenResponse.userDetails??>
                message.token.user_details = {
                    email: "${model.tokenResponse.userDetails.email?js_string}",
                    first_name: "${model.tokenResponse.userDetails.firstName?js_string}",
                    last_name: "${model.tokenResponse.userDetails.lastName?js_string}",
                    user_id: "${model.tokenResponse.userDetails.userId}"
                };
            </#if>
        </#if>
    <#else>
        message = {
            status: "logged_out"
        };
    </#if>

        if (window.parent && window.parent.postMessage) {
            // this message is only allowed for same-origin
            window.parent.postMessage(message, "${model.targetOrigin?js_string}");
        }
    })();
</script>
</body>

</html>