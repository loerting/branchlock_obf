<!DOCTYPE html>
<html lang="{{ config('app.locale') }}" data-bs-theme="{{ request()->cookie('theme', 'light') }}">
<head>
    @include('home.partials.head')
</head>
<body class="d-flex flex-column bg-light">
@include('app.partials.nav')

<div class="bg-light h-100 w-100 d-md-none" style="z-index: 10000;">
    <div class="container mt-5 text-center text-danger font-monospace">
        <i class="fa-solid fa-triangle-exclamation fa-3x me-1 mb-3"></i>
        <h4>We currently do not support resolutions below 768px</h4>
    </div>
</div>

<main class="app flex-shrink-0 d-none d-md-flex">
    <div class="wrapper d-flex">
        <aside class="sidebar shadow-none">
            <div class="sidebar-section sidebar-menu p-0">
                <ul>
                    <li>
                        <a class="d-flex align-items-center{{ Route::is('app.projects') || Route::is('app.obfuscator.jar.show') ? ' active' : '' }}"
                           href="{{ route('app.projects') }}">
                            <i class="fas fa-cubes"></i>
                            <span class="menu-text flex-fill">Projects</span>
                            <!--span class="badge text-bg-success me-1">3.2.5</span-->
                        </a>
                    </li>
                    <li>
                        <a class="d-flex align-items-center{{ Route::is('app.stacktrace') ? ' active' : '' }}" href="{{ route('app.stacktrace') }}">
                            <i class="fas fa-route"></i>
                            <span class="menu-text flex-fill">Stacktrace</span>
                        </a>
                    </li>
                </ul>
            </div>
            <div class="sidebar-section sidebar-menu p-0 position-absolute" style="bottom: 0;">
                <ul style="margin-bottom: .7rem">
                    @if(auth()->user()['role'] === \App\Models\User::ROLE_ADMIN)
                        <li>
                            <a class="d-flex align-items-center{{ Route::is('app.admin') ? ' active' : '' }}" href="{{ route('app.admin') }}">
                                <i class="fas fa-tools"></i>
                                <span class="menu-text flex-fill">Administration</span>
                            </a>
                        </li>
                    @endif
                    <li>
                        <a class="d-flex align-items-center{{ Route::is('app.settings') ? ' active' : '' }}" href="{{ route('app.settings') }}">
                            <i class="fas fa-cog"></i>
                            <span class="menu-text flex-fill">Settings</span>
                        </a>
                    </li>
                    <li>
                        <a class="d-flex align-items-center{{ Route::is('app.support') ? ' active' : '' }}" href="{{ route('app.support') }}">
                            <i class="fas fa-question-circle"></i>
                            <span class="menu-text flex-fill">Support</span>
                        </a>
                    </li>
                </ul>
            </div>
        </aside>
    </div>

    <div class="page-content animate__animated animate__fadeIn">
        <div class="container px-4 px-xl-5 py-xl-3">
            <!--h3 class="fw-semibold mb-4">@yield('title')<span>@yield('subtitle')</span></h3-->
            @yield('content')

            <noscript>
                <div class="alert alert-danger mt-4">
                    <p class="fw-bold">Whoops! Something went wrong:</p>
                    <ul class="m-0">
                        This page requires JavaScript to work properly.
                    </ul>
                </div>
            </noscript>
        </div>
    </div>
</main>

@include('app.partials.toasts')

@include('app.sites.projects.feedback-modal')
@yield('modals')

@yield('extra-scripts')

@include('home.partials.global-scripts')
</body>
</html>
