@extends('app.layout')

@section('meta')
    <title>Projects &bull; {{ config('app.name') }}</title>
@endsection

@section('content')

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
    @if(Session::has('success'))
        <div class="alert alert-success mb-3">
            {{ Session::get('success') }}
        </div>
    @endif

    <section id="projects" data-user-id="{{ auth()->user()->id }}" data-cooldown="{{auth()->user()->getPlan()['cooldown']}}">
        @if($projects->isEmpty())
            <div class="d-flex flex-column justify-content-center align-items-center" style="min-height: 70vh;">
                <div class="text-center">
                    <img src="{{ asset('img/no-projects.svg') }}" class="img-fluid mb-4" alt="Empty projects" width="200rem" height="auto">
                    <h6>You don't have any projects yet.</h6>
                    <button type="button" class="btn btn-primary d-block mx-auto mt-4" data-bs-toggle="modal"
                            data-bs-target="#addNewProjectModal">
                        <i class="fa-solid fa-plus me-1"></i> Create new project
                    </button>
                </div>
            </div>
        @else
            <div class="row row-cols-2 row-cols-lg-2 row-cols-xl-3 row-cols-xxl-4 g-3">
                @foreach($projects as $index => $project)
                    @php
                        $isNew = now()->diffInHours($project['created_at']) <= 12;
                    @endphp
                    <div class="col">
                        <a href="#" data-bs-toggle="modal" data-bs-target="#projectModal_{{ $project->id }}"
                           class="card project-card shadow-none rounded-4">
                            <div class="card-body p-4">
                                <div class="row">
                                    <div class="col-2 col-lg-3 d-none d-lg-block">
                                        <i class="{{ $project['android'] ? 'fa-brands fa-android' : 'fa-solid fa-cube' }} fa-3x text-primary-emphasis me-1"></i>
                                    </div>
                                    <div class="col-10 col-lg-9">
                                        <h5 class="truncated mb-0">{{ $project['name'] }}</h5>
                                        <p class="truncated text-muted">{{ $project['project_id'] }}</p>
                                    </div>
                                </div>
                                <h6 class="text-muted small"><i class="fa-solid fa-gears me-1"></i> {{ $project['date_updated'] }}</h6>
                                <h6 class="text-muted small mb-0">
                                    <i class="fa-regular fa-calendar-plus me-1"></i> {{ $project['date2'] }}
                                    @if($isNew)
                                        <span class="badge bg-success text-white ms-1">New</span>
                                    @endif
                                </h6>
                            </div>
                        </a>
                    </div>
                @endforeach

                <div class="col d-flex">
                    <a href="#" data-bs-toggle="modal" data-bs-target="#addNewProjectModal"
                       class="card project-card new-project text-center rounded-4 shadow-none w-100">
                        <div class="card-body text-muted p-4 d-flex align-items-center justify-content-center">
                            <i class="fa-solid fa-plus me-1"></i> New project
                        </div>
                    </a>
                </div>
            </div>
        @endif
    </section>

    @if(!$projects->isEmpty())
        @if(auth()->user()->getPlan()['demo_mode'])
            <div class="alert alert-warning rounded-4 mt-4" role="alert">
                <h6 class="alert-heading fw-bold">DEMO MODE NOTICE</h6>
                <p>Please note that the free demo plan is exclusively designed for trial purposes only.
                    It is intended to provide an opportunity for you to become acquainted with our product and its features.
                    Please be advised that the demo plan is not intended for commercial use.
                    Certain restrictions are imposed on the obfuscation process:</p>
                <ul class="mb-0">
                    <li>The maximum limit of included classes is 1500.</li>
                    <li>Obfuscation of sensitive data such as URLs, IP addresses, or similar information is disabled.</li>
                    <li>A cooldown period is enforced between obfuscation requests.</li>
                    <li>A watermark is added to the code.</li>
                </ul>
            </div>
        @endif
        @if(auth()->user()->role === \App\Models\User::ROLE_SANDBOX)
            <div class="alert alert-warning rounded-4 mt-4" role="alert">
                <h6 class="alert-heading fw-bold">SANDBOX ACCOUNT NOTICE</h6>
                <p class="mb-0">This account will be deleted in 1 hour.</p>
            </div>
        @endif
    @endif
@endsection

@section('modals')
    @include('app.sites.projects.obfuscator.partials.templates')
    @include('app.sites.projects.obfuscator.partials.annotations')
    @include('app.sites.projects.obfuscator.partials.ranges-docs')

    @include('app.sites.projects.add-modal')

    @foreach($projects as $index => $project)
        @include('app.sites.projects.rename-modal')
        @include('app.sites.projects.delete-modal')

        @include('app.sites.projects.obfuscator.project-view-modal')
    @endforeach
@endsection

@section('extra-scripts')
    @vite('resources/js/snippets/projects/projects.js')
@endsection
