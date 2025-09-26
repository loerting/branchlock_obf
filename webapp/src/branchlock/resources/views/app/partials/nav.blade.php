<header>
    <nav class="navbar navbar-expand-md fixed-top navbar-dark bg-primary shadow-sm no-select">
        <div class="container-fluid">
            <a class="navbar-brand p-1 ms-2" href="/">
                <img src="{{ url('img/logo-sm.svg') }}" alt="" width="24" height="auto" class="d-inline-block align-text-top">
                <span class="ms-3">Branchlock</span>
            </a>
            <!--span class="badge text-bg-warning">Beta</span-->
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarCollapse" aria-controls="navbarCollapse"
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
