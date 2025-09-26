<div class="col">
    <div class="card shadow-none border-2 rounded-4 h-100">

        <div class="card-body text-center d-flex flex-column">
            <div class="">
                <h5 class="mb-3 text-{{ $license['color'] }}-emphasis">{{ $license['name'] }}</h5>
                <p>{{ $license['description'] }}</p>

                <div class="text-center">
                    @if($license['demo_mode'])
                        <h4 class="mt-2 mb-0">€{{ $license['price_month'] }}</h4>
                        <h6 class="mt-3 mb-0">Free of charge</h6>
                    @else
                        <h4 class="mt-2 mb-0">€{{ number_format($license['price_month'], 2) }} <span class="text-muted">/month</span></h4>
                        <h6 class="mt-3 mb-0">€{{ number_format($license['price'], 2) }} billed annually</h6>
                    @endif
                </div>
            </div>

            <div class="my-auto text-center align-items-center">
                @if(auth()->check())
                    @if(auth()->user()->getPlan()['backend_name'] === $backend_name)
                        <div class="bg-primary-subtle text-primary rounded-4 p-3 mt-3">
                            <i class="fa-solid fa-check-to-slot me-2"></i>
                            <span class="fw-semibold">Your current license</span>
                        </div>
                    @else
                        @if(auth()->user()->getPlan()['tier'] < $license['tier'])
                            <a href="{{ route('app.license.get', ['license' => $backend_name, 'duration' => 'yearly']) }}" class="btn btn-{{ $license['color'] }} rounded-4 mt-3">
                                <i class="fa-solid fa-caret-right me-1"></i> Upgrade</a>
                        @else
                            <div class="bg-warning-subtle text-warning-emphasis rounded-4 p-3 mt-3">
                                <i class="fa-solid fa-triangle-exclamation me-2"></i>
                                <span class="fw-semibold">Downgrade not possible</span>
                            </div>
                        @endif
                    @endif
                @else
                    <a href="{{ route('login.show', ['plan' => $backend_name]) }}" class="btn btn-{{ $license['color'] }} rounded-4 mt-3">
                        <i class="fa-solid fa-caret-right me-1"></i> Get Started</a>
                @endif
            </div>
        </div>

        <div class="card-footer rounded-bottom-4 border-0 p-0">
            @if($loop->iteration % 2 == 0)
                <svg style="transform: scaleX(-1) rotate(180deg); transition: 0.3s" viewBox="0 0 1440 160" version="1.1"
                     xmlns="http://www.w3.org/2000/svg">
                    <defs>
                        <linearGradient id="sw-gradient-{{ $backend_name }}" x1="0" x2="0" y1="1" y2="0">
                            <stop stop-color="var(--bs-body-bg)" offset="0%"></stop>
                            <stop stop-color="var(--bs-body-bg)" offset="100%"></stop>
                        </linearGradient>
                    </defs>
                    <path style="transform:translate(0, 0px); opacity:1" fill="url(#sw-gradient-{{ $backend_name }})"
                          d="M0,0L34.3,13.3C68.6,27,137,53,206,58.7C274.3,64,343,48,411,45.3C480,43,549,53,617,69.3C685.7,85,754,107,823,104C891.4,101,960,75,1029,66.7C1097.1,59,1166,69,1234,82.7C1302.9,96,1371,112,1440,112C1508.6,112,1577,96,1646,74.7C1714.3,53,1783,27,1851,24C1920,21,1989,43,2057,66.7C2125.7,91,2194,117,2263,125.3C2331.4,133,2400,123,2469,98.7C2537.1,75,2606,37,2674,40C2742.9,43,2811,85,2880,104C2948.6,123,3017,117,3086,120C3154.3,123,3223,133,3291,120C3360,107,3429,69,3497,61.3C3565.7,53,3634,75,3703,93.3C3771.4,112,3840,128,3909,114.7C3977.1,101,4046,59,4114,58.7C4182.9,59,4251,101,4320,112C4388.6,123,4457,101,4526,85.3C4594.3,69,4663,59,4731,64C4800,69,4869,91,4903,101.3L4937.1,112L4937.1,160L4902.9,160C4868.6,160,4800,160,4731,160C4662.9,160,4594,160,4526,160C4457.1,160,4389,160,4320,160C4251.4,160,4183,160,4114,160C4045.7,160,3977,160,3909,160C3840,160,3771,160,3703,160C3634.3,160,3566,160,3497,160C3428.6,160,3360,160,3291,160C3222.9,160,3154,160,3086,160C3017.1,160,2949,160,2880,160C2811.4,160,2743,160,2674,160C2605.7,160,2537,160,2469,160C2400,160,2331,160,2263,160C2194.3,160,2126,160,2057,160C1988.6,160,1920,160,1851,160C1782.9,160,1714,160,1646,160C1577.1,160,1509,160,1440,160C1371.4,160,1303,160,1234,160C1165.7,160,1097,160,1029,160C960,160,891,160,823,160C754.3,160,686,160,617,160C548.6,160,480,160,411,160C342.9,160,274,160,206,160C137.1,160,69,160,34,160L0,160Z"></path>
                </svg>
            @else
                <svg style="transform: rotate(180deg);" viewBox="0 0 1440 160" version="1.1" xmlns="http://www.w3.org/2000/svg">
                    <defs>
                        <linearGradient id="sw-gradient-0" x1="0" x2="0" y1="1" y2="0">
                            <stop stop-color="var(--bs-body-bg)" offset="0%"></stop>
                            <stop stop-color="var(--bs-body-bg)" offset="100%"></stop>
                        </linearGradient>
                    </defs>
                    <path style="transform:translate(0, 0px); opacity:1" fill="url(#sw-gradient-0)"
                          d="M0,0L34.3,13.3C68.6,27,137,53,206,58.7C274.3,64,343,48,411,45.3C480,43,549,53,617,69.3C685.7,85,754,107,823,104C891.4,101,960,75,1029,66.7C1097.1,59,1166,69,1234,82.7C1302.9,96,1371,112,1440,112C1508.6,112,1577,96,1646,74.7C1714.3,53,1783,27,1851,24C1920,21,1989,43,2057,66.7C2125.7,91,2194,117,2263,125.3C2331.4,133,2400,123,2469,98.7C2537.1,75,2606,37,2674,40C2742.9,43,2811,85,2880,104C2948.6,123,3017,117,3086,120C3154.3,123,3223,133,3291,120C3360,107,3429,69,3497,61.3C3565.7,53,3634,75,3703,93.3C3771.4,112,3840,128,3909,114.7C3977.1,101,4046,59,4114,58.7C4182.9,59,4251,101,4320,112C4388.6,123,4457,101,4526,85.3C4594.3,69,4663,59,4731,64C4800,69,4869,91,4903,101.3L4937.1,112L4937.1,160L4902.9,160C4868.6,160,4800,160,4731,160C4662.9,160,4594,160,4526,160C4457.1,160,4389,160,4320,160C4251.4,160,4183,160,4114,160C4045.7,160,3977,160,3909,160C3840,160,3771,160,3703,160C3634.3,160,3566,160,3497,160C3428.6,160,3360,160,3291,160C3222.9,160,3154,160,3086,160C3017.1,160,2949,160,2880,160C2811.4,160,2743,160,2674,160C2605.7,160,2537,160,2469,160C2400,160,2331,160,2263,160C2194.3,160,2126,160,2057,160C1988.6,160,1920,160,1851,160C1782.9,160,1714,160,1646,160C1577.1,160,1509,160,1440,160C1371.4,160,1303,160,1234,160C1165.7,160,1097,160,1029,160C960,160,891,160,823,160C754.3,160,686,160,617,160C548.6,160,480,160,411,160C342.9,160,274,160,206,160C137.1,160,69,160,34,160L0,160Z"></path>

                </svg>
            @endif

            <ul class="list-unstyled p-3 pt-1 pb-0">
                @foreach($license['features'] as $name => $feature)
                    @php
                        $iconClass = 'fa-ban text-danger';
                        $value = $feature;

                        switch (gettype($feature)) {
                            case 'boolean':
                                $iconClass = $feature ? 'fa-check text-'.$license['color'] : 'fa-ban text-danger';
                                break;
                            case 'integer':
                            case 'string': case 'double':
                                $iconClass = 'fa-info text-'.$license['color'];
                                break;
                        }
                    @endphp

                    <li class="mb-2">
                        <i class="fa-solid {{ $iconClass }} me-2 fa-fw"></i>
                        {{ $name }}
                    </li>
            @endforeach
        </div>
    </div>
</div>
