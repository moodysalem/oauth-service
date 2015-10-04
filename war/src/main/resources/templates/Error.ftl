<!DOCTYPE html>
<html lang="en">
<#include "Head.ftl">
    <body>
        <div class="container">
        <#escape x as x?html>
            <div class="alert alert-danger"><strong>Error!</strong> ${model}</div>
        </#escape>
        </div>
    </body>
</html>
