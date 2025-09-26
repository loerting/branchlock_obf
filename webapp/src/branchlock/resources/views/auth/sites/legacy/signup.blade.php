@extends('auth.sites.legacy.base')

@section('meta')
    <title>Sign up &bull; {{ config('app.name') }}</title>

    <meta name="description" content="Sign up for a BranchLock account.">
@endsection

@section('form')
    <h2 class="mb-1 title text-center">Create an Account</h2>
    <p class="text-center text-muted mb-3">Already have an account? <a href="{{ route('login.legacy') }}">Sign in</a></p>
    <form class="py-3" method="post" action="{{ route('register.legacy.perform') }}" class="needs-validation" id="form1" novalidate>
        @csrf
        <div class="mb-3">
            <label for="email" class="form-label">E-Mail</label>
            <input type="email" class="form-control" id="email" name="email" placeholder="name@example.com"
                   value="{{ old('email') }}" required>
            <div class="invalid-feedback">
                <i class="fa-solid fa-circle-exclamation me-2"></i>Please enter a valid email address.
            </div>
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">Password</label>
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
        <div class="mb-3">
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
        {{--
        <div class="mb-3">
            <label for="referral" class="form-label">How did you hear about Branchlock? <span
                    class="text-muted">(Optional)</span></label>
            <textarea class="form-control" id="referral" name="referral" rows="2" maxlength="160"
                      placeholder="Search engine, recommended by a friend, etc." style="resize: none;">{{ old('referral') }}</textarea>
        </div>
        --}}
        <div class="container mt-4 mb-2">
            <div class="row">
                <div class="col-12 d-flex justify-content-center">
                    <div class="form-check mb-3">
                        <input class="form-check-input" type="checkbox" id="consent" name="consent">
                        <label class="form-check-label" for="consent">
                            I confirm that I have read, understand and agree to Branchlock's
                            <a href="{{ route('home.terms') }}" target="_blank">Terms of Service</a> and
                            <a href="{{ route('home.privacy') }}" target="_blank">Privacy Policy</a>.
                        </label>
                        <div class="invalid-feedback">
                            <i class="fa-solid fa-circle-exclamation me-2"></i>You must agree before submitting.
                        </div>
                    </div>
                </div>
                {{--
                <div class="col-12 d-flex justify-content-center">
                    <div class="md-checkbox small">
                        <input class="form-check-input" type="checkbox" name="newsletters" id="newsletters">
                        <label class="form-check-label" for="newsletters">
                            It's okay to send me emails with updates, changelogs, newsletters and
                            more. (Optional)
                        </label>
                        <div class="invalid-feedback text-center">
                            You must agree before submitting.
                        </div>
                    </div>
                </div>
                --}}
            </div>
        </div>

        @include('auth.partials.hcaptcha')
    </form>
@endsection
