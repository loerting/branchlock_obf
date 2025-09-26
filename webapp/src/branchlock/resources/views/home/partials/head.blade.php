<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

<link rel="icon" href="{{ url('favicon.png') }}">

<meta http-equiv="x-ua-compatible" content="IE=edge,chrome=1">
<meta http-equiv="Content-Language" content="{{ app()->getLocale() }}">

<meta name=theme-color content="#3F51B5">
<meta name="color-scheme" content="light">
@yield('meta')

@vite('webfonts.css')
@vite(['resources/sass/app.scss', 'resources/js/app.js'])
@livewireStyles
