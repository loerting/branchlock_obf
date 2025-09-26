<footer class="footer mt-auto py-3 bg-body border-top d-flex flex-wrap justify-content-between align-items-center">
    <div class="container">
        <div class="d-flex flex-wrap justify-content-between align-items-center">
            <p class="col-md-6 mb-0 text-muted d-none d-md-block">&copy; {{ date('Y') }} {{ config('app.name') }}</p>

            <ul class="nav col-md-6 justify-content-end">
                <li class="list-inline-item text-muted"><a href="{{ route('home.terms') }}">Terms</a></li>
                <li class="list-inline-item text-muted">&bull;</li>
                <li class="list-inline-item text-muted"><a href="{{ route('home.privacy') }}">Privacy</a></li>
                <li class="list-inline-item text-muted">&bull;</li>
                <li class="list-inline-item text-muted"><a href="{{ route('home.imprint') }}">Impressum</a></li>
            </ul>
        </div>
    </div>
</footer>
