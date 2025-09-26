<div class="tab-pane fade" id="pills-process_{{ $project->id }}" role="tabpanel" aria-labelledby="pills-tab-process_{{ $project->id }}" tabindex="0">

    @if(!$project->android)
        <div class="d-flex justify-content-center">
            <div class="position-fixed p-3" style="bottom: 1.65rem; z-index: 1000;">
                <a role="button" class="btn btn-sm bg-primary-subtle text-primary-emphasis rounded-4 mt-3 disabled border-0 download-output"
                   data-project-id="{{ $project->id }}"
                   onclick="this.classList.add('disabled'); this.classList.remove('animate__animated', 'animate__bounce')"
                   href="{{ auth()->user()->role === \App\Models\User::ROLE_SANDBOX ? '#' : route('app.project.download', ['id' => $project->id]) }}"
                   @if(auth()->user()->role === \App\Models\User::ROLE_SANDBOX)
                       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-title="Sandbox accounts cannot download output files."
                @endif>
                <i class="fa-solid fa-circle-down me-1"></i> Download Output
                </a>
            </div>
        </div>
    @endif

    <h6>Real-time Log</h6>
    <div class="position-relative">
        <div class="form-group rounded-4" style="background: var(--side-menu-link-bg); overflow-wrap: break-word;">
            <div class="position-absolute top-0 end-0 p-3" style="z-index: 100;">
                <a role="button" class="btn btn-sm bg-light border-secondary-subtle rounded-3 text-muted opacity-75 copy-btn"
                   data-copy-target="temp-response-box_{{ $project->id }}" data-clean="true"
                   href="#">
                    <i class="fa-regular fa-copy fa-2x"></i>
                </a>
            </div>

            <pre class="py-3 px-2 font-monospace w-100 overflow-y-auto overflow-x-hidden" id="temp-response-box_{{ $project->id }}"
                 style="height: 70.9vh; white-space: pre-wrap;overflow-wrap: break-word;"></pre>
        </div>
    </div>

</div>
