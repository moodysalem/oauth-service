<!DOCTYPE html>
<html lang="en">
<#include "Head.ftl">
<body>
<div class="container">
    <h2 class="page-header">Something went wrong...</h2>

    <div class="alert alert-danger">
        <i class="fa fa-exclamation-triangle"></i>
    ${model?html}
    </div>
</div>
</body>
</html>
