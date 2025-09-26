@extends('auth.layout')

@section('meta')
    <title>Log in &bull; {{ config('app.name') }}</title>

    <meta name="description" content="Log in to your BranchLock account.">
@endsection

@section('content')
    <div class="container h-100">
        <div class="row h-100 justify-content-center align-items-center mx-auto">

            <div class="col-12 col-xl-10">

                @if ($errors->any())
                    <div class="alert alert-danger mb-4">
                        <p class="fw-bold">Whoops! Something went wrong:</p>
                        <ul class="m-0">
                            @foreach ($errors->all() as $error)
                                <li>{{ $error }}</li>
                            @endforeach
                        </ul>
                    </div>
                @endif

                <div class="row g-5">
                    <div class="col-12 col-lg-6 border-end border-light">
                        <div class="d-grid gap-3 px-3">
                            @foreach($providers as $provider)
                                <a href="{{ route('oauth.redirect', $provider['provider']) }}" class="btn btn-lg btn-light rounded-4" type="button">
                                    <img class="me-2" src="{{ $provider['icon'] }}" alt="{{ $provider['name'] }}" width="25" height="25">
                                    <span>Sign in with {{ $provider['name'] }}</span>
                                </a>
                            @endforeach
                        </div>
                    </div>

                    <div class="col-12 col-lg-6 d-flex align-items-center">
                        <div class="w-100 px-2">
                            <a href="{{ route('login.legacy') }}" class="btn btn-lg btn-dark rounded-4 d-block mb-3" type="button">
                                <i class="fa-solid fa-envelope me-2"></i> <span>Sign in using E-Mail</span>
                            </a>
                            @if(Route::has('login.sandbox'))
                                <form action="{{ route('login.sandbox') }}" method="post" class="w-100">
                                    @csrf
                                    <div class="d-grid">
                                        <button type="submit" class="btn btn-lg btn-dark rounded-4 d-block mb-3 animate__animated animate__pulse animate__slow animate__infinite">
                                            <i class="fa-solid fa-rocket me-2"></i>
                                            <span>Try Free Demo</span>
                                        </button>
                                    </div>
                                </form>
                            @else
                                <a href="#" class="btn btn-lg btn-dark rounded-4 d-block mb-3 disabled"
                                   type="button">
                                    <i class="fa-solid fa-rocket me-2"></i>
                                    <span>Try Free Demo</span>
                                </a>
                            @endif
                            @env('local')
                                <a href="{{ route('login.offline') }}" class="btn btn-lg btn-dark text-warning rounded-4 d-block" type="button">
                                    <i class="fa-solid fa-laptop-code me-2"></i>
                                    <span>Offline dev login</span>
                                </a>
                            @endenv
                        </div>
                    </div>
                    <p class="col-lg-8 mx-auto text-center bg-primary-subtle p-1 rounded-4">By signing in or signing up, you accept our
                        <a href="{{ route('home.terms') }}" target="_blank">Terms of Service</a> and
                        <a href="{{ route('home.privacy') }}" target="_blank">Privacy Policy</a>.</p>
                </div>

            </div>
        </div>
    </div>
@endsection
