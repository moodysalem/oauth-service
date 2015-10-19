<style>
    label[for] {
        cursor: pointer;
    }

    #form-signin, #form-register, #form-reset {
        padding: 1em;
        background-color: rgba(0, 0, 0, 0.03);
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

    .scope-thumbnail {
        width: 100%;
        max-width: 150px;
        border-radius: 100%;
        box-shadow: 0 0 1px inset;
    }

    .toggle-checkbox input[type=checkbox] {
        visibility: hidden;
    }

    .toggle-checkbox {
        display: inline-block;
        width: 120px;
        height: 40px;
        background: #333;
        margin-top: 50px;

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

        -webkit-transition: all .5s ease;
        -moz-transition: all .5s ease;
        -o-transition: all .5s ease;
        -ms-transition: all .5s ease;
        transition: all .5s ease;
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