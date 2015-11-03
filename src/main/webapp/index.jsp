<%@ page import="com.oauth2cloud.server.applications.admin.filter.TokenFilter" %>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <link rel="stylesheet" href="rbs/css/rbs.css" type="text/css"/>
    <link rel="shortcut icon" href="res/favicon.ico?v=1"/>
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/bootswatch/3.3.5/paper/bootstrap.min.css"
          integrity="sha384-8uu+B/3A5Pjofed/yR1V7M6z9vL+Q2qm6uWNxIog8oyuvh9Avf22OpU7QLY6YJri" crossorigin="anonymous">
    <base href="/">

    <title>OAuth2 Cloud</title>

    <script>
        window.debug = true;
        window.clientId = "<%= StringEscapeUtils.escapeEcmaScript(TokenFilter.CLIENT_ID) %>";
        if (typeof window.location.hash === "string" && window.location.hash.length > 0) {
            var frag = window.location.hash.substring(1);
            var obj = {};
            window.history.replaceState(void(0), void(0), window.location.pathname);
            var fragPcs = frag.split("&");
            var i;
            for (i = 0; i < fragPcs.length; i++) {
                var pp = fragPcs[ i ].split("=");
                if (pp.length != 2) {
                    continue;
                }
                var n = decodeURIComponent(pp[ 0 ]), v = decodeURIComponent(pp[ 1 ]);
                obj[ n ] = v;
            }
            window.hashObject = obj;
        }
    </script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/require.js/2.1.20/require.min.js"
            integrity="sha384-3Ft7qPP9cYCINavEQr8puY8Ng61an4d3jQRqlOv+NhS7aKteY9qqGaqzcGk5hT7c" crossorigin="anonymous"
            data-main="js/App.js"></script>
</head>
<body>
<div id="nav"></div>
<div id="app">
    <!-- Loading animation container -->
    <div class="loading">
        <!-- We make this div spin -->
        <div class="spinner">
            <!-- Mask of the quarter of circle -->
            <div class="mask">
                <!-- Inner masked circle -->
                <div class="maskedCircle"></div>
            </div>
        </div>
    </div>
    <style>
        @keyframes spin {
            from {
                -webkit-transform: rotate(0deg);
                -moz-transform: rotate(0deg);
                -ms-transform: rotate(0deg);
                -o-transform: rotate(0deg);
                transform: rotate(0deg);
            }
            to {
                -webkit-transform: rotate(360deg);
                -moz-transform: rotate(360deg);
                -ms-transform: rotate(360deg);
                -o-transform: rotate(360deg);
                transform: rotate(360deg);
            }
        }

        @-webkit-keyframes spin {
            from {
                -webkit-transform: rotate(0deg);
                -moz-transform: rotate(0deg);
                -ms-transform: rotate(0deg);
                -o-transform: rotate(0deg);
                transform: rotate(0deg);
            }
            to {
                -webkit-transform: rotate(360deg);
                -moz-transform: rotate(360deg);
                -ms-transform: rotate(360deg);
                -o-transform: rotate(360deg);
                transform: rotate(360deg);
            }
        }

        .loading {
            position: absolute;
            top: 50%;
            left: 50%;
            width: 28px;
            height: 28px;
            margin: -14px 0 0 -14px;
        }

        .loading .spinner {
            position: absolute;
            left: 1px;
            top: 1px;
            width: 26px;
            height: 26px;
            -webkit-animation: spin 1s infinite linear;
            -moz-animation: spin 1s infinite linear;
            -o-animation: spin 1s infinite linear;
            animation: spin 1s infinite linear;
        }

        .loading .mask {
            width: 12px;
            height: 12px;
            overflow: hidden;
        }

        .loading .maskedCircle {
            width: 20px;
            height: 20px;
            border-radius: 12px;
            border: 3px solid #666;
        }
    </style>
</div>
</body>
</html>