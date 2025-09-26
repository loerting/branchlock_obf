@extends('app.layout')

@section('meta')
    <title>Stacktrace &bull; {{ config('app.name') }}</title>
@endsection

@section('content')
    @livewire('stacktrace')
@endsection
