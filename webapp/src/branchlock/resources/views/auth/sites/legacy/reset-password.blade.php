@extends('auth.sites.legacy.base')

@section('meta')
    <title>Reset password &bull; {{ config('app.name') }}</title>

    <meta name="description" content="Reset the password for your BranchLock account.">
@endsection

@section('form')
    <h2 class="mb-1 title text-center">Reset Password</h2>
    <form class="py-3" method="post" action="{{ route('password.update') }}" id="form1">
        @csrf
        <input type="hidden" name="token" value="{{$token}}">
        <div class="mb-3">
            <label for="email" class="form-label">E-Mail</label>
            <input type="email" class="form-control" id="email" name="email" placeholder="name@example.com"
                   value="{{ old('email') }}">
            <div class="invalid-feedback">
                <i class="fa-solid fa-circle-exclamation me-2"></i>Please enter a valid email address.
            </div>
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">New Password</label>
            <div class="inner-addon right-addon">
                <i class="fa fa-regular fa-eye opacity-75 toggle-password" data-target="password"></i>
                <input type="password" id="password" name="password" class="form-control"
                       aria-describedby="passwordHelpBlock" required
                       min="8"
                       data-bs-container="body" data-bs-toggle="popover"
                       data-bs-placement="right"
                       data-bs-html="true"
                       data-bs-trigger="focus"
                       data-bs-content='Your password must be at least 8 characters long.
                                   We recommend using a strong, randomly generated, unique password.'>
                <div class="invalid-feedback">
                    <i class="fa-solid fa-circle-exclamation me-2"></i>Your password must be at least 8 characters long.
                </div>
            </div>
        </div>
        <div class="mb-4">
            <label for="password_confirm" class="form-label">Confirm Password</label>
            <div class="inner-addon right-addon">
                <i class="fa fa-regular fa-eye opacity-75 toggle-password" data-target="password_confirm"></i>
                <input type="password" id="password_confirm" name="password_confirm" class="form-control"
                       aria-describedby="passwordHelpBlock" required>
                <div class="invalid-feedback">
                    <i class="fa-solid fa-circle-exclamation me-2"></i>Your password must be at least 8 characters long.
                </div>
            </div>
        </div>
        @include('auth.partials.hcaptcha')
    </form>
@endsection
