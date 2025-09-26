<li class="nav-item">
    <a class="nav-link{{ Route::is('home.changelog') ? ' active' : '' }}"
       href="{{ route('home.changelog') }}">
        <i class="fa-solid fa-timeline me-1"></i> Changelog
        @php
            $new = \Carbon\Carbon::createFromTimestamp(\App\Models\Changelog::getRunningVersion()->timestamp)->diffInHours() < 24;
            if (\Illuminate\Support\Facades\Cookie::has('changelog')) {
                $new = \App\Models\Changelog::getRunningVersion()->timestamp > \Illuminate\Support\Facades\Cookie::get('changelog');
            }
        @endphp

        @if ($new)
            <span class="badge rounded-pill text-bg-success ms-1">New</span>
        @endif

    </a>
</li>

{{--
<li class="nav-item">
    <a class="nav-link{{ Route::is('home.pricing') ? ' active' : '' }}"
       href="{{ route('home.pricing') }}"><i class="fa-solid fa-key me-1"></i> Licenses</a>
</li>
--}}
<li class="nav-item">
    <a class="nav-link{{ Route::is('home.docs') ? ' active' : '' }}"
       href="{{ route('home.docs') }}"><i class="fa-solid fa-book me-1"></i> Documentation
        @php
            $staticTimestamp = 1705846473;
                $new = \Carbon\Carbon::createFromTimestamp($staticTimestamp)->diffInHours() < 24;
                if (\Illuminate\Support\Facades\Cookie::has('docs_last_seen')) {
                    $new = $staticTimestamp > \Illuminate\Support\Facades\Cookie::get('docs_last_seen');
                }
        @endphp

        @if ($new)
            <span class="badge rounded-pill text-bg-success ms-1">New</span>
        @endif
    </a>
</li>
<li class="nav-item">
    <a class="nav-link{{ Route::is('home.pricing') ? ' active' : '' }}"
       href="https://github.com/loerting/branchlock_obf" target="_blank"><i class="fa-brands fa-github me-1"></i> Source Code</a>
</li>
