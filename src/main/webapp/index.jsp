<%@ page import="com.oauth2cloud.server.applications.admin.filter.TokenFilter" %>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils" %>
<% response.setStatus(200);%>
<!DOCTYPE html>
<html>
<head>
    <base href="/">
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <link rel="stylesheet" href="rbs/css/rbs.css" type="text/css"/>
    <link rel="stylesheet" href="res/oauth2cloud.css">
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/bootswatch/3.3.5/cosmo/bootstrap.min.css"
          integrity="sha384-X1WZVl4a9n8ONvqi5NUzo9FzcyMTWJ8TeF5AiqROAUkyrMYBenoixW9fMe6aWb6L" crossorigin="anonymous">
    <link rel="shortcut icon" href="res/favicon.ico?v=1"/>
    <!--<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/bootswatch/3.3.5/paper/bootstrap.min.css"
          integrity="sha384-8uu+B/3A5Pjofed/yR1V7M6z9vL+Q2qm6uWNxIog8oyuvh9Avf22OpU7QLY6YJri" crossorigin="anonymous">-->

    <title>OAuth2 Cloud</title>

    <script>
        window.debug = <%= System.getProperty("DEBUG") != null %>;
        var API_URL = "api";
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
    <div class="loading">
        <div class="loader">
        </div>
        <br/><br/>
        <span id="loading-text">Loading JavaScript...</span>
    </div>
</div>
</body>
</html>