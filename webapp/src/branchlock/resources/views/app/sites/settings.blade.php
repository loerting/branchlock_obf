@extends('app.layout')

@section('meta')
    <title>Settings &bull; {{ config('app.name') }}</title>
@endsection

@section('content')
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

    <div class="row row-cols-1 g-3">
        <div class="col">
            @if(Auth()->user()->auth_type === 'legacy')
                <button type="button" class="btn btn-primary rounded-5" data-bs-toggle="modal" data-bs-target="#changePasswordModal">Change Password</button>
            @else
                <p class="text-muted mb-0">You cannot manage your account here because you are logged in with
                    <strong>{{ ucfirst(Auth()->user()->auth_type) }}</strong>.</p>
            @endif
        </div>

        <div class="col">
            <button type="button" class="btn btn-primary rounded-5" data-bs-toggle="modal" data-bs-target="#manageTokensModal"
                @disabled(auth()->user()->role === \App\Models\User::ROLE_SANDBOX)>
                Manage API tokens
            </button>
        </div>

        <div class="col">
            @if(session()->has('original_admin_user'))
                <a class="btn btn-success rounded-5" href="{{ route('app.admin.switchback') }}">Sign in back as admin</a>
            @else
                <a class="btn btn-danger rounded-5" href="{{ route('logout') }}">Sign Out</a>
            @endif
        </div>
    </div>
@endsection

@section('modals')
    @include('app.sites.change-password-modal')
    @include('app.sites.manage-tokens-modal')
@endsection
