<form method="POST">
    <input type="hidden" name="action" value="email"/>

    <div class="form-group">
        <label class="control-label" for="email">E-mail Address</label>
        <input type="email" id="email" name="email" class="form-control"
               autofocus required
               placeholder="joe@gmail.com"/>
    </div>

    <div class="checkbox">
        <label>
            <input type="checkbox" name="remember_me" checked/> Remember me for 1 month
        </label>
    </div>

    <div class="form-group">
        <button class="btn btn-primary btn-block" type="submit">
            <i class="fa fa-sign-in"></i>
            <span>Submit</span>
        </button>
    </div>
</form>