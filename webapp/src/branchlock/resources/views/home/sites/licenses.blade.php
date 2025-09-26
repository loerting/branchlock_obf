@extends('home.layout')

@section('meta')
    <title>Licenses &bull; {{ config('app.name') }}</title>

    <meta name="description" content="Legal Notice">
@endsection

@section('bg', 'bg-light')

@section('content')
    @include('home.partials.header')
    <div class="container pb-5">
        <div class="alert alert-success" role="alert">
            <h4 class="alert-heading">Branchlock is Now Free!</h4>
            <p>Starting today, Branchlock is completely free for an unlimited time. Enjoy all Premium features without any cost, for as long as you like.</p>
        </div>
        {{--
        <div class="d-flex flex-column justify-content-center align-items-center">
            <ul class="nav nav-pills side-menu side-menu-horizontal bg-light no-select py-2 pt-0 mb-3"
                id="pills-tab_payment" role="tablist">
                <li class="nav-item" role="presentation">
                    <button class="nav-link active" id="pills-tab-yearly" data-bs-toggle="pill"
                            data-bs-target="#pills-yearly" type="button"
                            role="tab" aria-controls="pills-yearly" aria-selected="true">
                        <i class="fa-solid fa-calendar-days"></i>
                        Yearly billing
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="pills-tab-monthly" data-bs-toggle="pill"
                            data-bs-target="#pills-monthly" type="button"
                            role="tab" aria-controls="pills-monthly" aria-selected="false">
                        <i class="fa-solid fa-calendar-day"></i>
                        Monthly billing
                    </button>
                </li>
            </ul>

            <div class="tab-content" id="v-pills-tabContent">
                <div class="tab-pane fade show active" id="pills-yearly" role="tabpanel" aria-labelledby="pills-tab-yearly"
                     tabindex="0">
                    <div class="row row-cols-xxl-4 row-cols-xl-3 row-cols-md-2 row-cols-1 g-3">
                        @foreach($licenses as $backend_name => $license)
                            @if($license['limited'])
                                @continue
                            @endif
                            @include('home.partials.license')
                        @endforeach
                    </div>
                </div>

                <div class="tab-pane fade" id="pills-monthly" role="tabpanel" aria-labelledby="pills-tab-monthly"
                     tabindex="0">
                    <div class="row row-cols-xxl-4 row-cols-xl-3 row-cols-md-2 row-cols-1 g-3">
                        @foreach($licenses as $backend_name => $license)
                            @if($license['limited'])
                                @continue
                            @endif
                            @include('home.partials.license-monthly')
                        @endforeach
                    </div>
                </div>
            </div>
        </div>
        --}}


    </div>
@endsection
