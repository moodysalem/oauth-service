<#-- The Login Form -->
<form class="form-signin" method="POST">
    <h2 class="form-signin-heading">
    ${model.client.application.name}
        <small>${model.client.name}</small>
    </h2>

    <label for="inputEmail" class="sr-only">E-mail address</label>
    <input type="email" id="inputEmail" name="email" class="form-control" placeholder="E-mail address" required
           autofocus>
    <label for="inputPassword" class="sr-only">Password</label>
    <input type="password" id="inputPassword" name="password" class="form-control" placeholder="Password" required>

    <div class="checkbox">
        <label>
            <input type="checkbox" value="remember-me"> Remember me
        </label>
    </div>

    <#-- failed to log in -->
    <#if model.loginError??>
        <div class="alert alert-danger">${model.loginError}</div>
    </#if>

    <button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
</form>