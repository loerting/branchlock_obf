@if (isset($item['title']))
    <a class="nav-link rounded-5 fw-bold{{ $index === 'introduction' ? ' active' : '' }}" data-bs-toggle="tab" data-bs-target="#{{ Str::slug($item['title']) }}"
       href="#"><i class="fa-solid fa-arrow-right fa-fw me-1"></i> {{ $item['title'] }}</a>
@endif

@if (isset($item['subtitles']) && count($item['subtitles']) > 0)
    <nav class="nav nav-pills flex-column">
        @foreach ($item['subtitles'] as $subtitle)
            @if (isset($subtitle['title']))
                <a class="nav-link text-muted ms-5">{{ $subtitle['title'] }}</a>
            @endif
        @endforeach
    </nav>
@endif
