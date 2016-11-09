<#setting url_escaping_charset='UTF-8'>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/>
<meta name="description" content="Log In Page">
<meta name="author" content="Moody Salem">

<!-- favicon url -->
<#if (model.faviconUrl)??>
<link rel="icon" href="${model.faviconUrl?html}">
</#if>

<!-- flexbox styles always available -->
<link rel="stylesheet"
      href="https://cdn.rawgit.com/moodysalem/flexbox-css/7b5063fb5985904245bf7e2a75f1d2221440ebe5/dist/flexbox-css-min.css"
      integrity="sha384-5DzewQ8m2sTfg+tmtmzOdfg89aydUYzyuLsVNPbFlm9w4+DFPrRmbHqajYALly7W" crossorigin="anonymous">

<!-- Bootstrap core CSS -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.7/css/bootstrap.min.css"
      integrity="sha256-916EbMg70RQy9LHiGkXzG8hSg9EdNy97GazNG/aiY1w=" crossorigin="anonymous"/>

<!-- jquery -->
<script src="https://code.jquery.com/jquery-1.11.3.min.js"
        integrity="sha384-+54fLHoW8AHu3nHtUxs9fW2XKOZ2ZwKHB5olRtKSDTKJIb1Na1EceFZMS8E72mzW"
        crossorigin="anonymous"></script>

<!-- bootstrap js -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.7/js/bootstrap.min.js"
        integrity="sha256-U5ZEeKfGNOja007MMD3YBI0A3OSZOQbeG6z2f2Y0hu8=" crossorigin="anonymous"></script>

<!-- bootstrap theme or the theme associated with the application -->
<#if (model.stylesheetUrl)??>
<link rel="stylesheet" href="${model.stylesheetUrl?html}">
</#if>

<!-- fontawesome icons -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css"
      integrity="sha256-AIodEDkC8V/bHBkfyxzolUMw57jeQ9CauwhVW6YJ9CA=" crossorigin="anonymous"/>

<!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
<script src="https://cdnjs.cloudflare.com/ajax/libs/html5shiv/3.7.3/html5shiv.min.js" integrity="sha384-qFIkRsVO/J5orlMvxK1sgAt2FXT67og+NyFTITYzvbIP1IJavVEKZM7YWczXkwpB" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/respond.js/1.4.2/respond.min.js" integrity="sha384-ZoaMbDF+4LeFxg6WdScQ9nnR1QC2MIRxA1O9KWEXQwns1G8UNyIEZIQidzb0T1fo" crossorigin="anonymous"></script>
<![endif]-->

<#include "Style.ftl">