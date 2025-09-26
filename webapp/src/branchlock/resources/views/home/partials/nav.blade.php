<header>
    <nav class="navbar navbar-expand-lg fixed-top navbar-dark bg-primary shadow no-select">
        <div class="container">
            <a class="navbar-brand p-1" href="/">
                <img src="{{ url('img/logo-sm.svg') }}" alt="" width="24" height="auto" class="d-inline-block align-text-top">
                <span class="ms-1">Branchlock</span>
            </a>
            <!--span class="badge text-bg-warning d-none d-lg-block">Beta</span-->
            <button class="navbar-toggler" type="button" data-bs-toggle="offcanvas" href="#offcanvasNavbar" aria-controls="navbarCollapse"
                    aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarCollapse">
                <ul class="navbar-nav ms-auto me-auto mb-2 mb-md-0">
                    @include('home.partials.nav-links')
                </ul>
                @include('home.partials.profile-badge')
            </div>
        </div>
    </nav>
</header>

<div class="offcanvas offcanvas-end" tabindex="-1" id="offcanvasNavbar" aria-labelledby="offcanvasNavbarLabel" style="z-index: 10000;">
    <div class="offcanvas-header">
        <h5 class="offcanvas-title" id="offcanvasNavbarLabel">Branchlock</h5>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body">
        <ul class="navbar-nav">
            @include('home.partials.nav-links')
        </ul>
        @include('home.partials.profile-badge')
    </div>
</div>
