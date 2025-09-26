<div>

    @if(count($versions) > 0)
        @foreach($versions as $index => $version)
            <div class="card shadow-none mb-3">
                <div class="card-body">
                    <h6 class="mb-0">
                        @if($index == 0)
                            <span class="circle pulse bg-success d-inline-flex me-2"
                                  data-bs-toggle="tooltip"
                                  data-bs-placement="top"
                                  data-bs-title="Running"></span>
                        @else
                            <i class="fa-solid fa-clock-rotate-left text-primary me-2"></i>
                        @endif
                        <span class="text-primary-emphasis fw-semibold">{{ $version['version'] }}</span>
                        <span class="text-muted small ms-1">({{ $version['name'] }})</span>

                        <!--a class="text-danger float-end" href="#">
                            <i class="fa-solid fa-trash-can"></i>
                        </a-->
                    </h6>
                </div>
            </div>
        @endforeach
    @endif

</div>
