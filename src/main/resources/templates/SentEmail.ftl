<!DOCTYPE html>
<html lang="en">
<head>
<#include "Head.ftl">
    <title>${model.client.name} - ${model.client.application.name?html}</title>
</head>

<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
            <h1 class="page-header text-center overflow-ellipsis">${model.client.application.name?html}</h1>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-4 col-lg-offset-4 col-sm-6 col-sm-offset-3">
            <div class="alert alert-info" id="sent-email-alert">
                <div class="text-center">
                    <i class="fa fa-envelope"></i> <strong>Check your inbox!</strong>
                </div>
                We just sent you an email with a link to access
                your <strong>${model.client.application.name?html}</strong> account.
                Click the button in that email and you’ll be all set!
            </div>
        </div>
    </div>
</div>
</body>