<!DOCTYPE html>
<html>
<head></head>
<body>
<script>
    (function () {
        "use strict";
        var message = ${model.authResponse};

        if (window.parent && window.parent.postMessage) {
            // this message is only allowed for same-origin
            window.parent.postMessage(message, "${model.targetOrigin?js_string}");
        }
    })();
</script>
</body>

</html>