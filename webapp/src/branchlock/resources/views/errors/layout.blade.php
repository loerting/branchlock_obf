<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>@yield('title')</title>

    <link href="{{ url('css/app.css') }}" rel="stylesheet">
    <style>
        @import url("https://fonts.googleapis.com/css2?family=Poppins:wght@300;500;700&display=swap"); body, html{align-items: center; background-color: #111217; font-family: Poppins, sans-serif; height: 100%; margin: 0; padding: 0; width: 100%}.container{left: 50%; position: absolute; text-align: center; top: 50%; transform: translate(-50%, -50%); width: 90%}p{color: #d1d1d1; font-size: 1.5em; font-weight: 300; line-height: 1em; line-height: 1.5em}@media only screen and (max-width: 900px){p{font-size: 1.5em; font-weight: 400; line-height: 1.5em}.container{width: 90%}.big_icon{margin-right: 8px}}h1{font-weight: 700}.text{color: #3F51B5;}.normal, .text{font-size: 3rem}.normal{color: #d1d1d1;}@-webkit-keyframes gradientText{0%{background-position: 0 50%}50%{background-position: 100% 50%}to{background-position: 0 50%}}@keyframes gradientText{0%{background-position: 0 50%}50%{background-position: 100% 50%}to{background-position: 0 50%}}@media screen and (min-width: 900px){.btn-big:hover{transform: scale(.96)}.btn-big:active{transform: scale(.9)}}@media screen and (max-width: 900px){.normal, .text{font-size: 3rem}p{font-size: 1.25rem}}
    </style>
</head>

<body>
<div class="container">
    <h1><span class="text">@yield('code')</span></h1>
    <h1><span class="normal">@yield('message')</span></h1>
</div>

</body>
</html>
