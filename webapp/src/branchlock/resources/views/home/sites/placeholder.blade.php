@extends('home.layout')

@section('bg', 'bg-light')

@section('content')
    @include('home.partials.header')
    <div class="container">
        <div class="d-flex flex-column justify-content-center align-items-center" style="min-height: 70vh;">
            <div class="text-center">
                <img src="{{ asset('img/work-in-progress.svg') }}" class="img-fluid mb-4" alt="Empty" width="250rem" height="auto">
                <h6 class="text-muted">Coming Soon</h6>
            </div>
        </div>
    </div>
@endsection
