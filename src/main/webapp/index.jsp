<%@ page import="com.oauth2cloud.server.applications.admin.filter.TokenFilter" %>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils" %>
<!DOCTYPE html>

<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <link rel="stylesheet" href="rbs/css/rbs.css" type="text/css"/>
    <link rel="shortcut icon" href="res/favicon.ico?v=1">
    <base href="/">

    <title>OAuth2 Cloud</title>

    <script>
        window.debug = true;
        window.clientId = "<%= StringEscapeUtils.escapeEcmaScript(TokenFilter.CLIENT_ID) %>";
    </script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/require.js/2.1.20/require.min.js" data-main="js/App.js"></script>
</head>
<body>
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
            -webkit-animation: spin 1s infini te linear;
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