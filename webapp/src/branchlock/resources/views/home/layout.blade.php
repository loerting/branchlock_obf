<!DOCTYPE html>
<html lang="{{ config('app.locale') }}" data-bs-theme="{{ request()->cookie('theme', 'light') }}">
<head>
    @include('home.partials.head')
</head>
<body class="d-flex flex-column min-vh-100 @yield('bg')">
@include('home.partials.nav')

<main class="d-flex flex-column">
    @yield('content')
</main>

@include('home.partials.footer')

@include('app.partials.toasts')
@yield('extra-scripts')

@include('home.partials.global-scripts')
</body>
</html>
