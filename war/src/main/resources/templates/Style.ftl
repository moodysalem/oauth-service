<style>
    label[for] {
        cursor: pointer;
    }

    #form-signin, #form-register {
        padding: 1em;
        background-color: rgba(0, 0, 0, 0.03);
        margin-bottom: 2em;
    }

    .scope-thumbnail {
        width: 100%;
        max-width: 150px;
        border-radius: 100%;
        box-shadow: 0 0 1px inset;
    }

    .green-circle-checkbox {
        width: 40px;
        height: 40px;
        background: #ddd;
        display: inline-block;
        border-radius: 100%;
        position: relative;
        -webkit-box-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
        -moz-box-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
    }

    .green-circle-checkbox input[type=checkbox] {
        visibility: hidden;
    }

    .green-circle-checkbox label {
        display: block;
        width: 30px;
        height: 30px;
        border-radius: 100px;

        -webkit-transition: all .5s ease;
        -moz-transition: all .5s ease;
        -o-transition: all .5s ease;
        -ms-transition: all .5s ease;
        transition: all .5s ease;
        cursor: pointer;
        position: absolute;
        top: 5px;
        left: 5px;
        z-index: 1;

        background: #333;

        -webkit-box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.5);
        -moz-box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.5);
        box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.5);
    }

    .green-circle-checkbox input[type=checkbox]:checked + label {
        background: #26ca28;
    }

    .green-circle-checkbox input[type=checkbox]:checked:disabled + label {
        background: darkgreen;
    }
</style>