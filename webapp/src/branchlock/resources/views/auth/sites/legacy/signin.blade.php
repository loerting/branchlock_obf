@extends('auth.sites.legacy.base')

@section('meta')
    <title>Log in &bull; {{ config('app.name') }}</title>

    <meta name="description" content="Log in to your BranchLock account.">
@endsection

@section('form')
    <h2 class="mb-1 title text-center">Sign In</h2>
    <p class="text-center text-muted mb-3">Don't have an account? <a href="{{ route('register.legacy') }}">Sign up</a></p>
    <form class="py-3" method="post" action="{{ route('login.legacy.perform') }}" id="form1">
        @csrf
        <div class="mb-3">
            <label for="email" class="form-label">E-Mail</label>
            <input type="email" class="form-control" id="email" name="email" placeholder="name@example.com"
                   value="{{ old('email') }}">
            <div class="invalid-feedback">
                <i class="fa-solid fa-circle-exclamation me-2"></i>Please enter a valid email address.
            </div>
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">Password</label>
            <div class="inner-addon right-addon">
                <i class="fa fa-regular fa-eye opacity-75 toggle-password" data-target="password"></i>
                <input type="password" id="password" name="password" class="form-control" required min="6">
                <div class="invalid-feedback">
                    <i class="fa-solid fa-circle-exclamation me-2"></i>Your password must be at least 6 characters long.
                </div>
            </div>
        </div>
        <div class="text-end me-3">
            <p><small><a href="{{ route('password.reset') }}">Forgot the password?</a></small></p>
        </div>
        @include('auth.partials.hcaptcha')
    </form>
@endsection
