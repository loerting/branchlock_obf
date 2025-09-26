<div class="modal fade" id="projectModal_{{ $project->id }}" tabindex="-1"
     aria-labelledby="projectModalLabel_{{ $project->id }}" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-fullscreen-xl-down modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content h-100">
            <div class="modal-header">
                <h5 class="modal-title text-primary-emphasis" id="projectModalLabel_{{ $project->id }}">
                    <i class="{{ $project['android'] ? 'fa-brands fa-android' : 'fa-solid fa-cube' }} me-1"></i> {{ $project['name'] }}
                    <a href="#" data-bs-target="#renameModal_{{ $project->id }}" data-bs-toggle="modal"><i
                            class="fa-solid fa-pencil ms-1 text-muted small opacity-75"></i></a>
                    <span style="border-left: 1px solid #ccc; margin: 0 10px; padding-left: 10px;"
                          class="text-muted small">
                                id: <span id="project-id_{{ $project->id }}">{{ $project['project_id'] }}</span>
                        <a href="javascript:void(0);" class="copy-btn" data-copy-target="project-id_{{ $project->id }}"
                           data-clean="false">
                            <i class="fa-regular fa-copy ms-1 text-muted small opacity-75"></i></a>
                            </span>
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body bg-light py-3" style="height: 0;min-height:0" data-simplebar>
                <div class="col-md-12 col-lg-10 mx-auto h-100">
                    <ul class="nav nav-pills side-menu side-menu-horizontal bg-light no-select py-2 pt-1"
                        id="pills-tab_{{ $project->id }}" role="tablist">
                        @if(!$project->android)
                            <li class="nav-item" role="presentation">
                                <button class="nav-link active" id="pills-tab-file_{{ $project->id }}"
                                        data-bs-toggle="pill"
                                        data-bs-target="#pills-file_{{ $project->id }}" type="button"
                                        role="tab" aria-controls="pills-file_{{ $project->id }}" aria-selected="true"><i
                                        class="fa-solid fa-folder-tree"></i>
                                    Files
                                </button>
                            </li>
                        @else
                            <li class="nav-item" role="presentation">
                                <button class="nav-link active" id="pills-tab-file_{{ $project->id }}"
                                        data-bs-toggle="pill"
                                        data-bs-target="#pills-file_{{ $project->id }}" type="button"
                                        role="tab" aria-controls="pills-file_{{ $project->id }}" aria-selected="true"><i
                                        class="fa-solid fa-list-check"></i>
                                    Setup
                                </button>
                            </li>
                        @endif
                        <li class="nav-item" role="presentation">
                            <button class="nav-link" id="pills-tab-config_{{ $project->id }}" data-bs-toggle="pill"
                                    data-bs-target="#pills-config_{{ $project->id }}" type="button"
                                    role="tab" aria-controls="pills-config_{{ $project->id }}" aria-selected="false"><i
                                    class="fa-solid fa-toggle-on"></i>
                                Tasks
                            </button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link" id="pills-tab-general_{{ $project->id }}" data-bs-toggle="pill"
                                    data-bs-target="#pills-general_{{ $project->id }}" type="button"
                                    role="tab" aria-controls="pills-general_{{ $project->id }}" aria-selected="false"><i
                                    class="fa-solid fa-sliders"></i>
                                Settings
                            </button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link" id="pills-tab-ranges_{{ $project->id }}" data-bs-toggle="pill"
                                    data-bs-target="#pills-ranges_{{ $project->id }}" type="button"
                                    role="tab" aria-controls="pills-ranges_{{ $project->id }}" aria-selected="false"><i
                                    class="fas fa-signs-post"></i>
                                Ranges
                            </button>
                        </li>
                        @if(!$project->android)
                            <li class="nav-item ms-auto" role="presentation">
                                <button type="button" class="btn btn-primary obfuscate-btn"
                                        data-project-id="{{ $project->id }}">
                                    <i class="fa-solid fa-play me-1"></i> Transform
                                </button>
                            </li>
                        @endif
                        <li class="nav-item {{ $project->android ? 'ms-auto' : '' }}" role="presentation">
                            <button class="nav-link" id="pills-tab-process_{{ $project->id }}" data-bs-toggle="pill"
                                    data-bs-target="#pills-process_{{ $project->id }}" type="button"
                                    role="tab" aria-controls="pills-process_{{ $project->id }}" aria-selected="false"><i
                                    class="fa-solid fa-terminal"></i>
                                Process
                            </button>
                        </li>
                        @if(auth()->user()->role === \App\Models\User::ROLE_ADMIN)
                            <li class="nav-item" role="presentation">
                                <button class="nav-link" id="pills-tab-project_{{ $project->id }}" data-bs-toggle="pill"
                                        data-bs-target="#pills-project_{{ $project->id }}" type="button"
                                        role="tab" aria-controls="pills-project_{{ $project->id }}"
                                        aria-selected="false"><i
                                        class="fa-solid fa-bug"></i>
                                </button>
                            </li>
                        @endif
                    </ul>

                    <form class="project" id="project-form_{{ $project->id }}"
                          data-project-id="{{ $project->id }}"
                          data-project-name="{{ $project->name }}"
                          data-project-android="{{ $project->android ? 'true' : 'false' }}">
                        @csrf
                        <div class="tab-content" id="v-pills-tabContent_{{ $project->id }}">

                            @includeUnless($project->android, 'app.sites.projects.obfuscator.tabs.files')
                            @if($project->android)
                                @include('app.sites.projects.obfuscator.tabs.android')
                            @endif
                            @include('app.sites.projects.obfuscator.tabs.config')
                            @include('app.sites.projects.obfuscator.tabs.general')
                            @include('app.sites.projects.obfuscator.tabs.ranges')
                            @include('app.sites.projects.obfuscator.tabs.process')
                            @include('app.sites.projects.obfuscator.tabs.admin')

                        </div>
                    </form>

                </div>

            </div>
            <div class="modal-footer">
                <div class="mx-auto">

                </div>
            </div>

        </div>
    </div>
</div>
