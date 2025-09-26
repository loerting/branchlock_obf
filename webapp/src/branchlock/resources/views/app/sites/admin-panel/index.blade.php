@extends('app.layout')

@section('meta')
    <title>Admin Panel &bull; {{ config('app.name') }}</title>
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

    <ul class="nav nav-pills mb-3 side-menu side-menu-horizontal" id="pills-tab_admin" role="tablist">
        <li class="nav-item" role="presentation">
            <button class="nav-link" id="pills-tab-stats" data-bs-toggle="pill"
                    data-bs-target="#pills-stats" type="button"
                    role="tab" aria-controls="pills-stats" aria-selected="false">
                <i class="fa-solid fa-chart-column"></i>
                Stats
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link active" id="pills-tab-users" data-bs-toggle="pill"
                    data-bs-target="#pills-users" type="button"
                    role="tab" aria-controls="pills-users" aria-selected="false">
                <i class="fa-solid fa-users"></i>
                Users
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link" id="pills-tab-versions" data-bs-toggle="pill"
                    data-bs-target="#pills-versions" type="button"
                    role="tab" aria-controls="pills-versions" aria-selected="false">
                <i class="fa-solid fa-code-commit"></i>
                Versions
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link" id="pills-tab-newsletters" data-bs-toggle="pill"
                    data-bs-target="#pills-newsletters" type="button"
                    role="tab" aria-controls="pills-newsletters" aria-selected="false">
                <i class="fa-solid fa-envelope-open-text"></i>
                Newsletters
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link" id="pills-tab-feedback" data-bs-toggle="pill"
                    data-bs-target="#pills-feedback" type="button"
                    role="tab" aria-controls="pills-feedback" aria-selected="false">
                <i class="fa-solid fa-message"></i>
                Feedback
            </button>
        </li>
    </ul>

    <div class="tab-content pt-2" id="v-pills-tabContent_admin">

        @include('app.sites.admin-panel.tabs.users')
        @include('app.sites.admin-panel.tabs.versions')
        @include('app.sites.admin-panel.tabs.newsletters')
        @include('app.sites.admin-panel.tabs.feedback')

    </div>
@endsection

@section('extra-scripts')

@endsection


