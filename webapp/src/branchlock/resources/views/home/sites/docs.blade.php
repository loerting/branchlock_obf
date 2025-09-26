@extends('home.layout')

@section('meta')
    <title>Documentation &bull; {{ config('app.name') }}</title>

    <meta name="description" content="Documentation">
@endsection

@section('bg', 'bg-light')

@section('content')
    @include('home.partials.header')
    <div class="container pb-5">

        <div class="card shadow-none rounded-4 h-100 docs">
            <div class="card-body">
                <div class="row">
                    <div class="col-12 col-md-4 mb-5 mb-md-0">
                        <nav id="navbar-docs" class="h-100 flex-column align-items-stretch pe-4 border-end">
                            <nav class="nav nav-pills flex-column">
                                @foreach ($docs as $index => $item)
                                    @include('home.partials.docs-nav_item', ['item' => $item])
                                @endforeach
                            </nav>
                        </nav>
                    </div>

                    <div class="col-12 col-md-8 overflow-auto h-100">
                        <div data-bs-target="#navbar-docs" data-bs-offset="0" tabindex="0">
                            <div class="tab-content mt-1">

                                @foreach ($docs as $index => $item)
                                    @include('home.partials.docs-content_item', ['item' => $item])
                                @endforeach
                            </div>

                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>
@endsection
