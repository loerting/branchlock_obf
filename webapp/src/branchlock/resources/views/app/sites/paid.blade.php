@extends('app.layout')

@section('meta')
    <title>Thank you &bull; {{ config('app.name') }}</title>
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

    <div class="d-flex flex-column justify-content-center align-items-center" style="min-height: 70vh;">
        <div class="text-center">
            <img src="{{ asset('img/done.svg') }}" class="img-fluid mb-4" alt="Empty" width="250rem" height="auto">
            <h6>Thank you for your purchase!</h6>
            <h6 class="text-muted">Your license will be activated as soon as we have received the payment.</h6>
        </div>
    </div>

@endsection

@section('modals')

@endsection
