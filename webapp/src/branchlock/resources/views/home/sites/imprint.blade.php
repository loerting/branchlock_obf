@extends('home.layout')

@section('meta')
    <title>Legal Notice &bull; {{ config('app.name') }}</title>

    <meta name="description" content="Legal Notice">
@endsection

@section('bg', 'bg-light')

@section('content')
    @include('home.partials.header')
    <div class="container pb-5">
        <div class="row justify-content-center">
            <div class="col-12">
                <h3 class="fw-semibold mb-4">Impressum</h3>

                <h5 class="fw-semibold mb-3">Verantwortlich für den Inhalt nach § 55 Abs. 2 RStV:</h5>

                <p>Leonhard Kohl-Lörting<br>
                    Murstraße 66<br>
                    6063 Rum<br>
                    Österreich</p>
                <p>E-Mail: legal@branchlock.net<br>
                    Telefon: +4367762494264</p>

                <h5 class="fw-semibold mb-3 mt-5">Aufsichtsbehörde:</h5>

                <p>Zuständige Aufsichtsbehörde: Bezirkshauptmannschaft Innsbruck</p>

            </div>
        </div>
    </div>
@endsection
