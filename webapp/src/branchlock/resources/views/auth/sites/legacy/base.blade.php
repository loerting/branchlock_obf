@extends('auth.layout')

@section('content')
    <div class="container h-100">
        <div class="row h-100 justify-content-center align-items-center mx-lg-5">
            <div class="col-12 col-lg-8 col-xl-6">
                <noscript>
                    <h5 class="text-danger text-center">This page requires JavaScript to work properly.</h5>
                </noscript>

                @if ($errors->any())
                    <div class="alert alert-danger py-3">
                        <p class="fw-bold">Whoops! Something went wrong:</p>
                        <ul class="m-0">
                            @foreach ($errors->all() as $error)
                                <li>{{ $error }}</li>
                            @endforeach
                        </ul>
                    </div>
                @endif
                @if(Session::has('success'))
                    <div class="alert alert-success mb-3">
                        {{ Session::get('success') }}
                    </div>
                @endif

                <div class="card form-signin bg-light rounded-4 shadow px-md-4 pt-2">
                    <div class="card-body pb-2">
                        @yield('form')
                    </div>
                </div>
            </div>

        </div>
    </div>
@endsection

@section('extra-scripts')
    <script src="https://js.hcaptcha.com/1/api.js" async defer></script>
    @vite('resources/js/snippets/auth.js')
@endsection
