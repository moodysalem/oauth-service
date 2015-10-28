<!DOCTYPE html>
<html>
<head></head>
<body>
<script>
    (function () {
        "use strict";
        var hash = window.location.hash;
        var message = {};
        if (typeof hash === "string" && hash.length > 1) {
            hash = hash.substring(1);
            var pcs = hash.split("&");
            for (var i = 0; i < pcs.length; i++) {
                var p = pcs[i];
                var pSplit = p.split("=");
                if (pSplit.length !== 2) {
                    continue;
                }
                message[decodeURIComponent(pSplit[0])] = decodeURIComponent(pSplit[1]);
            }

            if (window.opener && window.opener.postMessage) {
                // this message is only allowed for same-origin
                window.opener.postMessage(message, window.location.origin);
            }
        }
    })();
</script>
</body>

</html>