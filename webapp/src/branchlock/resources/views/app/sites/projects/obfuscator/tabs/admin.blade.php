<div class="tab-pane fade position-relative" id="pills-project_{{ $project->id }}" role="tabpanel" aria-labelledby="pills-tab-project_{{ $project->id }}"
     tabindex="0">

    <h6>Real-time Config <i class="fa-solid fa-circle-question ms-1 me-1"
                            data-bs-toggle="tooltip" data-bs-placement="right" data-bs-custom-class="custom-tooltip"
                            data-bs-title="For debugging purposes: this is automatically updated each time automatic project saving is triggered."></i>
    </h6>
    <div class="position-relative">
        <div class="form-group rounded-4" style="background: var(--side-menu-link-bg);">
            <div class="position-absolute top-0 end-0 p-3" style="z-index: 100;">
                <a role="button" class="btn btn-sm bg-light border-secondary-subtle rounded-3 text-muted opacity-75 copy-btn"
                   data-copy-target="admin-json-box_{{ $project->id }}" data-clean="false"
                   href="#">
                    <i class="fa-regular fa-copy fa-2x"></i>
                </a>
            </div>

            <pre class="py-3 px-2 font-monospace w-100 overflow-y-auto overflow-x-hidden json"
                 style="height: 65vh;" id="admin-json-box_{{ $project->id }}"></pre>
        </div>
    </div>


    <div class="form-check form-switch mt-3">
        <input class="form-check-input" type="checkbox" role="switch"
               id="server-config-full_{{ $project->id }}"
               name="server[config_full]"
            @checked($project['config']['config_full'] ?? false)>
        <label class="form-check-label small" for="server-config-full_{{ $project->id }}"
               data-bs-toggle="tooltip" data-bs-placement="right"
               data-bs-title="Display things that are only relevant for server-side configuration handling.">Show full config</label>
    </div>

    @if(auth()->user()['role'] === \App\Models\User::ROLE_ADMIN)
        <div class="form-check form-switch">
            <input class="form-check-input" type="checkbox" role="switch"
                   id="server-debug-mode_{{ $project->id }}"
                   name="server[debug_mode]"
                @checked($project['config']['debug_mode'] ?? false)>
            <label class="form-check-label small" for="server-debug-mode_{{ $project->id }}">Debug Mode</label>
        </div>

        <div class="form-check form-switch">
            <input class="form-check-input" type="checkbox" role="switch"
                   id="server-demo-mode_{{ $project->id }}"
                   name="server[demo_mode]"
                @checked($project['config']['demo_mode'] ?? false)>
            <label class="form-check-label small" for="server-demo-mode_{{ $project->id }}">Demo Mode</label>
        </div>

        <div class="form-check form-switch">
            <input class="form-check-input" type="checkbox" role="switch"
                   id="server-java-verbose_{{ $project->id }}"
                   name="server[java_verbose]"
                @checked($project['config']['java_verbose'] ?? false)>
            <label class="form-check-label small" for="server-java-verbose_{{ $project->id }}">Java verbose garbage collection</label>
        </div>
    @endif

</div>
