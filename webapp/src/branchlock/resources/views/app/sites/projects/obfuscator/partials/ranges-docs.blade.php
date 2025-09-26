<div class="offcanvas offcanvas-end my-auto border-0 shadow rounded-start-4 w-50" style="z-index: 10000;" data-bs-backdrop="true"
     tabindex="-1" id="offcanvasRangesDocs"
     aria-labelledby="offcanvasRangesDocsLabel">
    <div class="offcanvas-header">
        <h5 class="offcanvas-title" id="offcanvasRangesDocsLabel"><i class="fa-solid fa-book me-1"></i> Class Exclusion and Inclusion</h5>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body">
        @foreach($rangesDocs as $block)
            <p class="text-muted">{{ $block['content'] }}</p>

            @foreach($block['subtitles'] as $item)
                <div class="card shadow-none bg-primary-subtle mt-4 rounded-4 border-0">
                    <h5 class="card-header text-bg-primary border-0 rounded-4">
                        {{ $item['title'] }}
                    </h5>
                    <div class="card-body pb-0">
                        {!! $item['content'] !!}
                    </div>
                </div>
            @endforeach

        @endforeach
    </div>
</div>
