<ul class="navbar-nav nav-buttons mx-2">
    @if(!Auth::check())
        <li class="nav-item">
            <a class="btn btn-light btn-sm rounded-5" href="{{ route('login.show') }}"><i class="fa-solid fa-door-open"></i> Login</a>
        </li>
    @else
        @if (!Str::contains(Route::currentRouteName(), 'app'))
            <li class="nav-item my-auto me-2">
                <a class="btn btn-dark btn-sm rounded-5" href="{{ route('app.projects') }}">
                    <i class="fa-solid fa-arrow-up-right-from-square fa-fw me-1"></i>
                    Web app
                </a>
            </li>
        @endif

        <li class="nav-item dropdown">
            <a class="nav-link dropdown-toggle no-icon" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                <img class="avatar rounded-5 shadow-sm" src="{{ Auth()->user()->avatar ?? '/img/social/default-avatar.svg' }}" width="22" height="22"
                     alt="{{ Auth()->user()->username }}">
            </a>
            <ul class="dropdown-menu dropdown-menu-end profile mt-3 rounded-4">
                <li class="row px-3 py-2">
                    <div class="col-3">
                        <img class="avatar rounded-5" src="{{ Auth()->user()->avatar ?? '/img/social/default-avatar.svg' }}" width="48" height="48"
                             alt="{{ Auth()->user()->username }}">
                        <!--span class="badge text-bg-warning">Licensed</span>
                        <span class="badge text-bg-success">Solo</span-->
                    </div>
                    <div class="col-9">
                        <h6 class="fw-bold mb-0">{{ Auth()->user()->name ?? Auth()->user()->username }}</h6>
                        <p class="{{ isset(Auth()->user()->username) ? '' : 'fw-bold' }} small m-0">{{ Auth()->user()->email }}</p>

                        <a type="button" class="btn btn-dark btn-sm text-center mt-3 rounded-5 themeToggle">
                            <i class="fa-solid fa-circle-half-stroke fa-fw"></i> Change theme
                        </a>
                    </div>
                </li>
                <li>
                    <hr class="dropdown-divider">
                </li>
                <li class="row d-grid g-0 p-3 pt-1 pb-1">
                    @if (!Str::contains(Route::currentRouteName(), 'app'))
                        <a type="button" class="btn btn-primary rounded-4 mb-3" href="{{ route('app.projects') }}">
                            <i class="fa-solid fa-arrow-up-right-from-square fa-fw me-1"></i> Web application
                        </a>
                    @endif
                    @if(session()->has('original_admin_user'))
                        <a type="button" class="btn btn-success rounded-4" href="{{ route('app.admin.switchback') }}">
                            <i class="fa-solid fa-door-closed fa-fw me-1"></i> Sign in back as admin
                        </a>
                    @else
                        <a type="button" class="btn btn-danger rounded-4" href="{{ route('logout') }}">
                            <i class="fa-solid fa-door-closed fa-fw me-1"></i> Sign out
                        </a>
                    @endif
                </li>
            </ul>
        </li>
    @endif

    @if(!Auth::check())
        <li class="nav-item ms-lg-2 my-auto">
            <a type="button" class="btn btn-dark btn-sm rounded-5 themeToggle">
                <i class="fa-solid fa-circle-half-stroke"></i> <span class="d-inline d-lg-none">Dark mode</span>
            </a>
        </li>
    @endif
</ul>
