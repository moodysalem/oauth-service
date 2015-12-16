<style>
    label[for] {
        cursor: pointer;
    }

    #form-signin, #form-register, #form-reset {
        padding: 1em;
        background-color: rgba(0, 0, 0, 0.02);
        margin-bottom: 2em;
    }

    .center-line {
        position: relative;
    }

    .center-line:after {
        position: absolute;
        width: 100%;
        content: '';
        height: 1px;
        top: 50%;
        left: 0;
        z-index: -1;
        background-color: rgba(0, 0, 0, 0.2);
    }

    .or-block {
        display: inline-block;
        width: 5em;
        padding: 0.15em;
        color: rgba(0, 0, 0, 0.35);
        border-radius: 4em;
    }

    @media (max-width: 768px) {
        .login-btn-text {
            display: none;
        }
    }

    /*
        permissions page
    */

    .client-scope-row {
        display: flex;
        align-items: center;
        flex-direction: row;
    }

    .client-scope-row .client-scope-thumbnail {
        width: 125px;
        flex: none;
        padding: 0.5em;
    }

    .client-scope-row .client-scope-thumbnail img.scope-thumbnail {
        width: 100%;
    }

    .client-scope-row .client-scope-description {
        flex: 1 0;
    }

    .client-scope-row .client-scope-toggle {
        flex: none;
        width: 120px;
        text-align: center;
        color: blue;
        text-shadow: 0 0 1px rgba(0, 0, 0, 0.8);
    }

    .toggle-checkbox input[type=checkbox] {
        visibility: hidden;
    }

    .toggle-checkbox {
        display: inline-block;
        width: 120px;
        height: 40px;
        background: #333;

        border-radius: 50px;
        position: relative;
    }

    .toggle-checkbox:before {
        content: 'Yes';
        position: absolute;
        top: 9px;
        left: 17px;
        height: 2px;
        color: #26ca28;
        font-size: 16px;
    }

    .toggle-checkbox:after {
        content: 'No';
        position: absolute;
        top: 9px;
        left: 80px;
        height: 2px;
        color: #111;
        font-size: 16px;
    }

    .toggle-checkbox label {
        display: block;
        width: 52px;
        height: 22px;
        border-radius: 50px;

        -webkit-transition: all 0.2s;
        -moz-transition: all 0.2s;
        -ms-transition: all 0.2s;
        -o-transition: all 0.2s;
        transition: all 0.2s;
        cursor: pointer;
        position: absolute;
        top: 9px;
        z-index: 1;
        left: 12px;
        background: #ddd;
    }

    /**
     * Create the checkbox event for the label
     */
    .toggle-checkbox input[type=checkbox]:checked + label {
        left: 60px;
        background: #26ca28;
    }

</style>