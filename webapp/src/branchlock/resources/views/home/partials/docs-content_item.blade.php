<div class="tab-pane fade{{ $index === 'introduction' ? ' active show' : '' }}" id="{{ Str::slug($item['title']) }}" role="tabpanel"
     aria-labelledby="{{ Str::slug($item['title']) }}-tab" tabindex="0">
    <div class="pe-2">
        @if (isset($item['title']))
            <h4>{{ $item['title'] }}</h4>
        @endif

        @if (isset($item['content']))
            <div class="text-muted">
                {!! $item['content'] !!}
            </div>
        @endif

        @if (isset($item['subtitles']) && count($item['subtitles']) > 0)
            @foreach ($item['subtitles'] as $subtitle)
                @if (isset($subtitle['title']))
                    <div id="{{ Str::slug($subtitle['title']) }}" class="ms-3">
                        <h5 class="mt-4">{{ $subtitle['title'] }}</h5>

                        @if (isset($subtitle['content']))
                            <div class="text-muted">
                                {!! $subtitle['content'] !!}
                            </div>
                        @endif
                    </div>
                @endif
            @endforeach
        @endif
    </div>
</div>

