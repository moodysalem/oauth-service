<!DOCTYPE html>
<html>
<head></head>
<body>
<script>
    "use strict";
    var code = null, error = null, errorDescription = null;
    <#if model.code??>
    code = "${model.code?js_string}";
    </#if>
    <#if model.error??>
    error = "${model.error?js_string}";
    </#if>
    <#if model.errorDescription??>
    errorDescription = "${model.errorDescription?js_string}";
    </#if>
    if (window.opener && window.opener.postMessage) {
        // this message is only allowed for same-origin
        window.opener.postMessage({
            code: code,
            error: error,
            errorDescription: errorDescription
        }, window.location.origin);
    }
</script>
</body>

</html>