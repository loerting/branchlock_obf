<div class="tab-pane fade show active" id="pills-file_{{ $project->id }}" role="tabpanel"
     aria-labelledby="pills-tab-file_{{ $project->id }}" tabindex="0">

    <div class="mb-4">
        <h6>Input Java File</h6>
        <div class="card shadow-none {{ !isset($project->jar) ? 'd-none' : '' }}" data-project-id="{{ $project->id }}">
            <div class="card-body">
                <h6 class="mb-0"><span class="text-primary-emphasis fw-semibold">{{ $project->jar_original_name }}</span>
                    <a class="text-danger float-end jar-delete-btn" href="#" data-project-id="{{ $project->id }}">
                        <i class="fa-solid fa-trash-can"></i>
                    </a>
                    <span class="text-muted float-end me-3 small size">{{ $project->jar_size }}</span>
                </h6>
            </div>
        </div>

        <div data-action="{{ route('app.project.jar.upload', ['id' => $project->id]) }}"
             data-max-file-size="{{auth()->user()->getPlan()['maxFilesize']}}"
             data-csrf-token="{{ csrf_token() }}"
             class="dropzone text-muted dropzone-container-jar {{ isset($project->jar) ? 'd-none' : '' }}"
             data-project-id="{{ $project->id }}">
        </div>
    </div>


    <div class="mb-4">
        <a href="#libs-list_{{ $project->id }}" data-bs-toggle="collapse" class="text-body">
            <h6>Dynamic Libraries (<span
                    id="lib-list-count_{{ $project->id }}">{{ isset($project->libs) ? count($project->libs) : '0' }}</span>)
                <i class="fa-solid fa-circle-question ms-1 me-1"
                   data-bs-toggle="tooltip" data-bs-placement="right" data-bs-custom-class="custom-tooltip"
                   data-bs-title="Classes that are provided as a library but are also present in the input file will be excluded automatically."></i>
                <i class="fas fa-caret-down"></i></h6>
        </a>

        <div class="collapse my-2" id="libs-list_{{ $project->id }}">
            <div data-action="{{ route('app.project.lib.upload', ['id' => $project->id]) }}"
                 data-max-file-size="{{auth()->user()->getPlan()['maxFilesize']}}"
                 data-csrf-token="{{ csrf_token() }}"
                 class="dropzone text-muted dropzone-container-lib mb-3"
                 data-project-id="{{ $project->id }}">
            </div>

            <button type="button" class="btn btn-sm btn-outline-danger border-0 mb-3 libs-delete-btn"
                    data-project-id="{{ $project->id }}">
                <i class="fa-solid fa-trash-can me-1"></i> Delete all libraries
            </button>

            <ul class="list-group" id="lib-list_{{ $project->id }}">
                @if(isset($project->libs))
                    @foreach($project->libs as $lib)
                        <li class="list-group-item">
                            <span class="text-primary-emphasis fw-semibold">{{ $lib['name'] }}</span>
                            <a class="text-danger float-end lib-delete-btn" href="#"
                               data-project-id="{{ $project->id }}" data-lib-file="{{ $lib['file'] }}">
                                <i class="fa-solid fa-trash-can"></i>
                            </a>
                            <span class="text-muted float-end me-3 small size">{{ $lib['size'] }}</span>
                        </li>
                    @endforeach
                @endif
            </ul>
        </div>
    </div>

    <div class="mt-5 mb-4">
        <a href="#filesAdvancedSettings_{{ $project->id }}" data-bs-toggle="collapse" class="small text-body">
            Advanced Settings <i class="fas fa-caret-down ms-1"></i>
        </a>

        <div class="collapse show my-2" id="filesAdvancedSettings_{{ $project->id }}">
            <p class="small text-muted">
                For your convenience, we store your uploaded files to prevent re-uploading for multiple obfuscation tasks.
                After completing obfuscation and ensuring satisfaction, you can manually delete files. If not, they're securely
                stored for up to {{ config('settings.max_storage_time') }} hours and auto-deleted. However, if you'd like your files to be deleted
                immediately after the process, you can enable this option here.
            </p>

            <div class="form-check form-switch">
                <input class="form-check-input" type="checkbox" role="switch"
                       id="server-jar-delete_{{ $project->id }}"
                       name="server[delete_jar]"
                    @checked($project['config']['delete_jar'] ?? false)>
                <label class="form-check-label small" for="server-jar-delete_{{ $project->id }}">
                    Remove input jar file from server immediately after obfuscation
                </label>
            </div>

            <div class="form-check form-switch">
                <input class="form-check-input" type="checkbox" role="switch"
                       id="server-libs-delete_{{ $project->id }}"
                       name="server[delete_libs]"
                    @checked($project['config']['delete_libs'] ?? false)>
                <label class="form-check-label small" for="server-libs-delete_{{ $project->id }}">
                    Remove libraries from server immediately after obfuscation
                </label>
            </div>
        </div>
    </div>

    <div class="mb-4">
        <a href="#filesIDIntegration_{{ $project->id }}" data-bs-toggle="collapse" class="small text-body">
            IDE Integration <i class="fas fa-caret-down ms-1"></i>
        </a>

        <div class="collapse my-4" id="filesIDIntegration_{{ $project->id }}">
            @include('app.sites.projects.obfuscator.partials.desktop-plugin-guide')
        </div>
    </div>

</div>
