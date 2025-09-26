<!DOCTYPE html>
<html lang="{{ config('app.locale') }}" data-bs-theme="{{ request()->cookie('theme', 'light') }}">
<head>
    @include('home.partials.head')
</head>
<body class="d-flex flex-column min-vh-100 bg-image-1 p-0">

<main class="h-100 animate__animated animate__fadeIn">
    @yield('content')
</main>

<div class="text-center m-2 opacity-75">
    <a href="/">
        <img src="{{ url('img/logo-sm.svg') }}" alt="Branchlock" width="25" height="auto">
        <h6 class="fw-bold text-light small" href="/">{{ config('app.name') }}</h6>
    </a>
</div>

@yield('extra-scripts')

@include('home.partials.global-scripts')
</body>
</html>
