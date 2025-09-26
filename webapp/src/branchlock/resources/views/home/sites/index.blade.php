@extends('home.layout')

@section('meta')
    <title>Java Obfuscator & Optimizer &bull; {{ config('app.name') }}</title>
    <meta name="description"
          content="Online Java Obfuscator & Android with an easy-to-use interface. Make important strings and references illegible. Try the free demo and convince yourself.">
    <meta name="keywords"
          content="java obfuscator online, android, android obfuscator, java protection, java crack protection, Java 8, Obfuscation, Code, Encryption,
          Online, Web-based, Branchlock">
    <meta content="index, follow" name="robots">
    <meta name="googlebot" content="index, follow, max-snippet:-1, max-image-preview:large, max-video-preview:-1"/>
    <meta name="bingbot" content="index, follow, max-snippet:-1, max-image-preview:large, max-video-preview:-1"/>
    <meta property="article:section" content="overview">
    <link rel="canonical" href="https://branchlock.net">
    <meta name="twitter:card" content="summary">
    <meta name="twitter:title" content="Branchlock Java Obfuscator">
    <meta name="twitter:site" content="https://branchlock.net">
    <meta name="twitter:description"
          content="Online Java Obfuscator & Android with an easy-to-use interface. Make important strings and references illegible. Try the free demo and convince yourself.">
@endsection

@section('content')
    <section id="header" class="bg-pattern-wavy pt-5">
        <div class="container py-5 mb-3">

            <div class="alert alert-success" role="alert">
                <h4 class="alert-heading">Branchlock is Now Free!</h4>
                <p>Starting today, Branchlock is completely free and open source for an unlimited time. Enjoy all Premium features without any cost, for as long as you like.</p>
            </div>

            <div class="row py-4">
                <div class="col-md-7">
                    <h1 class="title fw-me mb-4">Where Code Protection meets Performance Optimization</h1>
                    <h5 class="text-muted mb-3">
                        Our service caters to individuals and businesses looking to both secure their code and fine-tune
                        their applications for seamless
                        production.
                    </h5>
                    <h5 class="text-muted">
                        We offer a sophisticated, web-based toolkit that enables you to safeguard and enhance your Java
                        applications, whether they run on
                        servers, desktops, or Android platforms.
                    </h5>
                    <div class="d-flex flex-column flex-md-row lead mt-4">
                        @if(auth()->check())
                            <a href="{{ route('app.projects') }}" class="btn btn-lg btn-primary"><i
                                    class="fa-solid fa-arrow-up-right-from-square me-1"></i> Web application</a>
                        @else
                            <a href="{{ route('login.show') }}" class="btn btn-lg btn-primary">Try now for free
                                &raquo;</a>
                        @endif
                    </div>
                </div>
                <div class="col justify-content-md-end text-center d-none d-lg-block">
                    <img src="{{ url('img/logo-xl.svg') }}" width="340px" height="auto" class="logo"
                         alt="Branchlock logo">
                </div>
            </div>
        </div>
        <svg class="d-none d-md-block" style="transform:rotate(0deg); transition: 0.3s; bottom: 1rem;"
             viewBox="0 0 1440 160" version="1.1"
             xmlns="http://www.w3.org/2000/svg">
            <defs>
                <linearGradient id="sw-gradient-0" x1="0" x2="0" y1="1" y2="0">
                    <stop stop-color="var(--bs-body-bg)" offset="0%"></stop>
                    <stop stop-color="var(--bs-body-bg)" offset="100%"></stop>
                </linearGradient>
            </defs>
            <path style="transform:translate(0, 0px); opacity:1" fill="url(#sw-gradient-0)"
                  d="M0,0L34.3,8C68.6,16,137,32,206,53.3C274.3,75,343,101,411,101.3C480,101,549,75,617,64C685.7,53,754,59,823,72C891.4,85,960,107,1029,98.7C1097.1,91,1166,53,1234,56C1302.9,59,1371,101,1440,120C1508.6,139,1577,133,1646,114.7C1714.3,96,1783,64,1851,56C1920,48,1989,64,2057,80C2125.7,96,2194,112,2263,122.7C2331.4,133,2400,139,2469,138.7C2537.1,139,2606,133,2674,112C2742.9,91,2811,53,2880,56C2948.6,59,3017,101,3086,109.3C3154.3,117,3223,91,3291,88C3360,85,3429,107,3497,112C3565.7,117,3634,107,3703,109.3C3771.4,112,3840,128,3909,125.3C3977.1,123,4046,101,4114,93.3C4182.9,85,4251,91,4320,96C4388.6,101,4457,107,4526,112C4594.3,117,4663,123,4731,114.7C4800,107,4869,85,4903,74.7L4937.1,64L4937.1,160L4902.9,160C4868.6,160,4800,160,4731,160C4662.9,160,4594,160,4526,160C4457.1,160,4389,160,4320,160C4251.4,160,4183,160,4114,160C4045.7,160,3977,160,3909,160C3840,160,3771,160,3703,160C3634.3,160,3566,160,3497,160C3428.6,160,3360,160,3291,160C3222.9,160,3154,160,3086,160C3017.1,160,2949,160,2880,160C2811.4,160,2743,160,2674,160C2605.7,160,2537,160,2469,160C2400,160,2331,160,2263,160C2194.3,160,2126,160,2057,160C1988.6,160,1920,160,1851,160C1782.9,160,1714,160,1646,160C1577.1,160,1509,160,1440,160C1371.4,160,1303,160,1234,160C1165.7,160,1097,160,1029,160C960,160,891,160,823,160C754.3,160,686,160,617,160C548.6,160,480,160,411,160C342.9,160,274,160,206,160C137.1,160,69,160,34,160L0,160Z"></path>
        </svg>
    </section>

    <noscript>
        <div class="container alert alert-danger mb-4">
            <p class="fw-bold">Whoops! Something went wrong:</p>
            <ul class="m-0">
                This page requires JavaScript to work properly.
            </ul>
        </div>
    </noscript>

    <section id="interactive-demo" class="d-none d-lg-block" data-demo-route="{{ route('demo') }}">
        <div class="container position-relative py-4">
            <div class="card bg-light rounded-4 shadow-none">
                <div class="card-header rounded-top-4 d-flex align-items-center justify-content-between">
                    <ul class="nav nav-pills side-menu side-menu-horizontal fw-bold" id="pills-tab" role="tablist">
                        <li class="nav-item" role="presentation">
                            <button class="nav-link active" id="tab-demo-code" data-bs-toggle="pill"
                                    data-bs-target="#demo-code" type="button" role="tab"
                                    aria-controls="demo-code" aria-selected="true"><i
                                    class="fa-solid fa-file-code text-primary"></i>
                                InteractiveDemo.java
                            </button>
                        </li>
                    </ul>
                    <div class="spinner-border text-primary float-end" id="interactive-demo-spinner" role="status"
                         style="display: none;">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
                <div class="card-body overflow-auto" style="height: 75vh;">
                    <div class="tab-content m-0" id="pills-tabContent">
                        <div class="tab-pane fade show active" id="demo-code" role="tabpanel"
                             aria-labelledby="tab-demo-code" tabindex="0">
                            <pre><code class="java text-left overflow-hidden"
                                       id="interactive-demo-code">{{ $demoCode }}</code></pre>
                        </div>
                    </div>

                    <div class="position-absolute" style="width: 35rem; top: 3rem; right: 1.5rem;">
                        <div class="position-sticky pt-3"
                             style="z-index: 3; top: 0; right: 0; float: right; width: 100%;">
                            <div class="card shadow-none rounded-4 bl-id-control-panel">
                                <div class="card-header rounded-4 border-0 fw-bold">
                                    <i class="fa-solid fa-wand-magic-sparkles text-primary me-1"></i>
                                    <a href="#demo-control-panel" data-bs-toggle="collapse" class="text-body">
                                        Branchlock <i class="fas fa-caret-down ms-1"></i>
                                    </a>
                                    <a class="btn btn-sm bg-primary-subtle rounded-4 p-0 px-2 float-end toggle-all"
                                       type="button">
                                        Toggle all
                                    </a>
                                </div>
                                <div class="collapse show" id="demo-control-panel">
                                    <div class="card-body pt-1">
                                        <div class="row py-1 row-cols-1 row-cols-lg-2 g-3">
                                            @foreach($demoTasksGroups as $group)
                                                <div class="col">
                                                    @foreach($group as $task)
                                                        <div class="form-check form-switch form-switch-md">
                                                            <input class="form-check-input task-switch" type="checkbox"
                                                                   id="{{ $task->backend_name }}">
                                                            <label class="form-check-label"
                                                                   for="{{ $task->backend_name }}"
                                                                   data-bs-toggle="tooltip" data-bs-placement="right"
                                                                   data-bs-html="true"
                                                                   data-bs-custom-class="custom-tooltip"
                                                                   data-bs-title="{{ $task->description_long }}">
                                                                {{ $task->frontend_name }}</label>
                                                        </div>
                                                    @endforeach
                                                </div>
                                            @endforeach
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
                <div class="card-footer d-none d-xl-block">
                    <p class="text-muted small mb-0"><i class="fa-solid fa-circle-info me-2"></i> Branchlock is applied
                        to this sample code in real time,
                        the result is then decompiled using CFR 0.152 by Lee Benfield, which is used by most modern
                        reverse engineering tools.</p>
                </div>
            </div>
        </div>
    </section>

    <section id="strengths" class="bg-light">
        <svg style="transform:rotate(180deg); transition: 0.3s" viewBox="0 0 1440 160" version="1.1"
             xmlns="http://www.w3.org/2000/svg">
            <defs>
                <linearGradient id="sw-gradient-0" x1="0" x2="0" y1="1" y2="0">
                    <stop stop-color="rgba(255, 255, 255, 1)" offset="0%"></stop>
                    <stop stop-color="rgba(255, 255, 255, 1)" offset="100%"></stop>
                </linearGradient>
            </defs>
            <path style="transform:translate(0, 0px); opacity:1" fill="url(#sw-gradient-0)"
                  d="M0,0L34.3,13.3C68.6,27,137,53,206,58.7C274.3,64,343,48,411,45.3C480,43,549,53,617,69.3C685.7,85,754,107,823,104C891.4,101,960,75,1029,66.7C1097.1,59,1166,69,1234,82.7C1302.9,96,1371,112,1440,112C1508.6,112,1577,96,1646,74.7C1714.3,53,1783,27,1851,24C1920,21,1989,43,2057,66.7C2125.7,91,2194,117,2263,125.3C2331.4,133,2400,123,2469,98.7C2537.1,75,2606,37,2674,40C2742.9,43,2811,85,2880,104C2948.6,123,3017,117,3086,120C3154.3,123,3223,133,3291,120C3360,107,3429,69,3497,61.3C3565.7,53,3634,75,3703,93.3C3771.4,112,3840,128,3909,114.7C3977.1,101,4046,59,4114,58.7C4182.9,59,4251,101,4320,112C4388.6,123,4457,101,4526,85.3C4594.3,69,4663,59,4731,64C4800,69,4869,91,4903,101.3L4937.1,112L4937.1,160L4902.9,160C4868.6,160,4800,160,4731,160C4662.9,160,4594,160,4526,160C4457.1,160,4389,160,4320,160C4251.4,160,4183,160,4114,160C4045.7,160,3977,160,3909,160C3840,160,3771,160,3703,160C3634.3,160,3566,160,3497,160C3428.6,160,3360,160,3291,160C3222.9,160,3154,160,3086,160C3017.1,160,2949,160,2880,160C2811.4,160,2743,160,2674,160C2605.7,160,2537,160,2469,160C2400,160,2331,160,2263,160C2194.3,160,2126,160,2057,160C1988.6,160,1920,160,1851,160C1782.9,160,1714,160,1646,160C1577.1,160,1509,160,1440,160C1371.4,160,1303,160,1234,160C1165.7,160,1097,160,1029,160C960,160,891,160,823,160C754.3,160,686,160,617,160C548.6,160,480,160,411,160C342.9,160,274,160,206,160C137.1,160,69,160,34,160L0,160Z"></path>
        </svg>
        <div class="container py-4">
            <h3 class="pb-2">Our Strengths</h3>
            <div class="row g-4 py-5 row-cols-1 row-cols-lg-3">
                <div class="feature col">
                    <i aria-hidden="true" class="fas fa-shield-alt text-primary fa-2x mb-3"></i>
                    <h4>Security</h4>
                    <p>Leveraging our extensive experience, we've developed the most robust methods and algorithms to
                        ensure the highest level of security
                        for your Java, Kotlin, and Android applications. Our technology, Branchlock, is designed to
                        hinder decompilers, making them a
                        futile effort.</p>
                </div>
                <div class="feature col">
                    <i aria-hidden="true" class="fas fa-toggle-on text-primary fa-2x mb-3"></i>
                    <h4>Easy-to-use</h4>
                    <p>Experience the simplicity of Branchlock's user-friendly interface. Encrypt your program without
                        the need for complex configuration
                        files or extensive documentation. We've designed Android and Java obfuscation to be as
                        straightforward as possible.</p>
                </div>
                <div class="feature col">
                    <i aria-hidden="true" class="fas fa-wrench text-primary fa-2x mb-3"></i>
                    <h4>Updates</h4>
                    <p>Stay ahead with our automatic updates. We handle the server-side updates, so you don't need to
                        worry about downloading anything.
                        Regular updates ensure our top-notch security and promptly address any bugs you encounter.</p>
                </div>
                <div class="feature col">
                    <i aria-hidden="true" class="fas fa-tachometer-alt text-primary fa-2x mb-3"></i>
                    <h4>Performance</h4>
                    <p>Our obfuscator is online, eliminating the need for powerful hardware. Encrypt a 20MB file in
                        under half a minute with our
                        service.</p>
                </div>
                <div class="feature col">
                    <i aria-hidden="true" class="fas fa-hand-holding-usd text-primary fa-2x mb-3"></i>
                    <h4>Affordable</h4>
                    <p>We understand the cost associated with quality obfuscators. That's why we've made our product
                        accessible to small developers,
                        maintaining a high level of security.</p>
                </div>
                <div class="feature col">
                    <i aria-hidden="true" class="fas fa-question-circle text-primary fa-2x mb-3"></i>
                    <h4>Support</h4>
                    <p>Our dedicated support team is available to assist you. If you have any questions or concerns,
                        don't hesitate to reach out to us.</p>
                </div>
            </div>
        </div>
    </section>

    <section id="features" class="">
        <svg style="transform:rotate(180deg); transition: 0.3s" viewBox="0 0 1440 160" version="1.1"
             xmlns="http://www.w3.org/2000/svg">
            <defs>
                <linearGradient id="sw-gradient-2" x1="0" x2="0" y1="1" y2="0">
                    <stop stop-color="var(--bs-light)" offset="0%"></stop>
                    <stop stop-color="var(--bs-light)" offset="100%"></stop>
                </linearGradient>
            </defs>
            <path style="transform:translate(0, 0px); opacity:1" fill="url(#sw-gradient-2)"
                  d="M0,64L34.3,69.3C68.6,75,137,85,206,85.3C274.3,85,343,75,411,72C480,69,549,75,617,88C685.7,101,754,123,823,114.7C891.4,107,960,69,1029,64C1097.1,59,1166,85,1234,85.3C1302.9,85,1371,59,1440,50.7C1508.6,43,1577,53,1646,53.3C1714.3,53,1783,43,1851,53.3C1920,64,1989,96,2057,101.3C2125.7,107,2194,85,2263,80C2331.4,75,2400,85,2469,85.3C2537.1,85,2606,75,2674,80C2742.9,85,2811,107,2880,114.7C2948.6,123,3017,117,3086,120C3154.3,123,3223,133,3291,117.3C3360,101,3429,59,3497,53.3C3565.7,48,3634,80,3703,93.3C3771.4,107,3840,101,3909,106.7C3977.1,112,4046,128,4114,120C4182.9,112,4251,80,4320,64C4388.6,48,4457,48,4526,40C4594.3,32,4663,16,4731,13.3C4800,11,4869,21,4903,26.7L4937.1,32L4937.1,160L4902.9,160C4868.6,160,4800,160,4731,160C4662.9,160,4594,160,4526,160C4457.1,160,4389,160,4320,160C4251.4,160,4183,160,4114,160C4045.7,160,3977,160,3909,160C3840,160,3771,160,3703,160C3634.3,160,3566,160,3497,160C3428.6,160,3360,160,3291,160C3222.9,160,3154,160,3086,160C3017.1,160,2949,160,2880,160C2811.4,160,2743,160,2674,160C2605.7,160,2537,160,2469,160C2400,160,2331,160,2263,160C2194.3,160,2126,160,2057,160C1988.6,160,1920,160,1851,160C1782.9,160,1714,160,1646,160C1577.1,160,1509,160,1440,160C1371.4,160,1303,160,1234,160C1165.7,160,1097,160,1029,160C960,160,891,160,823,160C754.3,160,686,160,617,160C548.6,160,480,160,411,160C342.9,160,274,160,206,160C137.1,160,69,160,34,160L0,160Z"></path>
        </svg>
        <div class="container py-4">
            <h3 class="pb-2">Features</h3>
            <div class="row row-cols-1 row-cols-sm-2 row-cols-md-3 row-cols-lg-4 g-4 pt-5 pb-2">
                @foreach($tasks as $task)
                    <div class="col d-flex align-items-start">
                        <div class="row">
                            <div class="col-2 mt-2">
                                <i aria-hidden="true" class="{{ $task->icon }} text-primary fa-2x"></i>
                            </div>
                            <div class="col-10">
                                <p class="fw-bold mb-0">{{ $task->frontend_name }}</p>
                                <p>{{ $task->description }}</p>
                            </div>
                        </div>
                    </div>
                @endforeach
            </div>
        </div>
    </section>

    <section id="pre-footer" class="bg-light">
        <svg style="transform:rotate(180deg); transition: 0.3s" viewBox="0 0 1440 160" version="1.1"
             xmlns="http://www.w3.org/2000/svg">
            <defs>
                <linearGradient id="sw-gradient-0" x1="0" x2="0" y1="1" y2="0">
                    <stop stop-color="rgba(255, 255, 255, 1)" offset="0%"></stop>
                    <stop stop-color="rgba(255, 255, 255, 1)" offset="100%"></stop>
                </linearGradient>
            </defs>
            <path style="transform:translate(0, 0px); opacity:1" fill="url(#sw-gradient-0)"
                  d="M0,0L34.3,8C68.6,16,137,32,206,53.3C274.3,75,343,101,411,101.3C480,101,549,75,617,64C685.7,53,754,59,823,72C891.4,85,960,107,1029,98.7C1097.1,91,1166,53,1234,56C1302.9,59,1371,101,1440,120C1508.6,139,1577,133,1646,114.7C1714.3,96,1783,64,1851,56C1920,48,1989,64,2057,80C2125.7,96,2194,112,2263,122.7C2331.4,133,2400,139,2469,138.7C2537.1,139,2606,133,2674,112C2742.9,91,2811,53,2880,56C2948.6,59,3017,101,3086,109.3C3154.3,117,3223,91,3291,88C3360,85,3429,107,3497,112C3565.7,117,3634,107,3703,109.3C3771.4,112,3840,128,3909,125.3C3977.1,123,4046,101,4114,93.3C4182.9,85,4251,91,4320,96C4388.6,101,4457,107,4526,112C4594.3,117,4663,123,4731,114.7C4800,107,4869,85,4903,74.7L4937.1,64L4937.1,160L4902.9,160C4868.6,160,4800,160,4731,160C4662.9,160,4594,160,4526,160C4457.1,160,4389,160,4320,160C4251.4,160,4183,160,4114,160C4045.7,160,3977,160,3909,160C3840,160,3771,160,3703,160C3634.3,160,3566,160,3497,160C3428.6,160,3360,160,3291,160C3222.9,160,3154,160,3086,160C3017.1,160,2949,160,2880,160C2811.4,160,2743,160,2674,160C2605.7,160,2537,160,2469,160C2400,160,2331,160,2263,160C2194.3,160,2126,160,2057,160C1988.6,160,1920,160,1851,160C1782.9,160,1714,160,1646,160C1577.1,160,1509,160,1440,160C1371.4,160,1303,160,1234,160C1165.7,160,1097,160,1029,160C960,160,891,160,823,160C754.3,160,686,160,617,160C548.6,160,480,160,411,160C342.9,160,274,160,206,160C137.1,160,69,160,34,160L0,160Z"></path>
        </svg>

        <div class="container py-5 pb-2">
            <h3 class="pb-2">Frequently Asked Questions</h3>
            <div class="accordion mt-3 rounded-4 mb-4 pb-2" id="faq">
                @foreach($faqs as $faq)
                    <div class="accordion-item">
                        <h2 class="accordion-header" id="faq-{{ $loop->iteration }}">
                            <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                    data-bs-target="#collapse-{{ $loop->iteration }}"
                                    aria-expanded="false"
                                    aria-controls="collapse-{{ $loop->iteration }}">
                                {{ $faq['question'] }}
                            </button>
                        </h2>
                        <div id="collapse-{{ $loop->iteration }}" class="accordion-collapse collapse"
                             aria-labelledby="faq-{{ $loop->iteration }}"
                             data-bs-parent="#faq">
                            <div class="accordion-body">
                                {!! $faq['answer'] !!}
                            </div>
                        </div>
                    </div>
                @endforeach
            </div>

            <div class="text-center d-none overflow-hidden" id="socials">
                <div class="trustpilot-widget text-right" data-locale="en-US"
                     data-template-id="5419b6a8b0d04a076446a9ad"
                     data-businessunit-id="5fd74a7f6d19ed00011637ca" data-style-height="48px" data-style-width="100%"
                     data-theme="light"
                     style="position: relative;">
                    <iframe title="Customer reviews powered by Trustpilot" loading="auto"
                            src="https://widget.trustpilot.com/trustboxes/5419b6a8b0d04a076446a9ad/index.html?templateId=5419b6a8b0d04a076446a9ad&amp;businessunitId=5fd74a7f6d19ed00011637ca#locale=en-US&amp;styleHeight=48px&amp;styleWidth=100%25&amp;theme=light"
                            style="position: relative; height: 48px; width: 100%; border-style: none; display: block; overflow: hidden;"></iframe>
                </div>
            </div>
        </div>
    </section>
@endsection

@section('extra-scripts')
    <script type="text/javascript" src="https://widget.trustpilot.com/bootstrap/v5/tp.widget.bootstrap.min.js"
            async=""></script>
    @vite('resources/js/snippets/index.js')
@endsection
