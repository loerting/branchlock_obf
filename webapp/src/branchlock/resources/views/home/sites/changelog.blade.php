@extends('home.layout')

@section('meta')
    <title>Changelog &bull; {{ config('app.name') }}</title>

    <meta name="description"
          content="We provide regular updates to ensure top security and compatibility. You can view our update history here.">
@endsection

@section('bg', 'bg-light')

@section('content')
    @include('home.partials.header')
    <section id="content" class="pb-5">
        <div class="container">
            <div class="row row-cols-1 g-4">

                @if($unpublished !== null)
                    @foreach($unpublished as $row)
                        <div class="col">
                            <div class="card shadow-none rounded-4 border-2">
                                <div class="card-body pb-2">
                                    <h5 class="card-title fw-bold"><i class="fa-solid fa-clock text-warning me-2"></i>
                                        {!! $row['version'] !!}
                                        <span class="badge rounded-pill text text-bg-warning float-end"
                                              data-bs-toggle="tooltip"
                                              data-bs-placement="top"
                                              data-bs-title="This update is still in progress.">Under development</span>
                                    </h5>
                                    <div class="card-text mt-3 blur no-select">
                                        <p>This version is still under development. A static placeholder text follows:</p>
                                        <ul>
                                            <li>We are doing our best to release the update as soon as possible.</li>
                                            <li>Please have a little more patience.</li>
                                            <li>Thank you for your interest in our product, we are pleased to offer you 10% coupon code for Branchlock:
                                                [CODE]
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>
                    @endforeach
                @endif


                @if($running !== null)
                    <div class="col">
                        <div class="card shadow-none rounded-4 border-2">
                            <div class="card-body pb-2">
                                <h5 class="card-title fw-bold">
                                    <span class="circle pulse bg-success d-inline-flex me-2"
                                          data-bs-toggle="tooltip"
                                          data-bs-placement="top"
                                          data-bs-title="Running"></span>
                                    {!! $running['version'] !!}
                                    <span class="badge rounded-pill text-bg-success float-end"
                                          data-bs-toggle="tooltip"
                                          data-bs-placement="top"
                                          data-bs-title="{{ $running['date2'] }}">{{ $running['date1'] }}</span>
                                </h5>
                                @if($running['content'] !== null)
                                    <div class="card-text mt-3">{!! $running['content'] !!}</div>
                                @endif
                            </div>
                        </div>
                    </div>
                @endif


                {{--<div class="text-secondary text-center py-2">
                    <i class="fa-solid fa-clock-rotate-left fa-2x"></i>
                </div>--}}


                @if($changelog !== null)
                    @foreach($changelog as $row)
                        <div class="col">
                            <div class="card shadow-none rounded-4 border-2">
                                <div class="card-body pb-2">
                                    <h5 class="card-title fw-bold"><i class="fa-solid fa-clock-rotate-left text-primary me-2"></i> {!! $row['version'] !!}
                                        @if($row['running'])
                                            <span class="circle pulse bg-success d-inline-flex ms-2"></span>
                                        @endif
                                        <span class="badge rounded-pill text-bg-primary float-end"
                                              data-bs-toggle="tooltip"
                                              data-bs-placement="top"
                                              data-bs-title="{{ $row['date2'] }}">{{ $row['date1'] }}</span>
                                    </h5>
                                    <!--h6 class="card-subtitle mb-2 text-muted">{{ $row['date2'] }}</h6-->
                                    <div class="card-text mt-3">{!! $row['content'] !!}</div>
                                </div>
                            </div>
                        </div>
                    @endforeach
                @endif
            </div>
        </div>
    </section>
@endsection
