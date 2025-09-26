@extends('auth.sites.legacy.base')

@section('meta')
    <title>Reset password &bull; {{ config('app.name') }}</title>

    <meta name="description" content="Reset the password for your BranchLock account.">
@endsection

@section('form')
    <h2 class="mb-1 title text-center">Reset Password</h2>
    <p class="text-center text-muted mb-3">Don't have an account? <a href="{{ route('register.legacy') }}">Sign up</a></p>
    <form class="py-3" method="post" action="{{ route('password.email') }}" id="form1">
        @csrf
        <div class="mb-4">
            <label for="email" class="form-label">E-Mail</label>
            <input type="email" class="form-control" id="email" name="email" placeholder="name@example.com"
                   value="{{ old('email') }}">
            <div class="invalid-feedback">
                <i class="fa-solid fa-circle-exclamation me-2"></i>Please enter a valid email address.
            </div>
        </div>
        @include('auth.partials.hcaptcha')
    </form>
@endsection
