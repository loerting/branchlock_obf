<div class="tab-pane fade" id="pills-ranges_{{ $project->id }}" role="tabpanel" aria-labelledby="pills-tab-ranges_{{ $project->id }}" tabindex="0">

    <div class="d-flex justify-content-center">
        <div class="position-fixed p-3" style="bottom: 1.65rem; z-index: 1000;">
            <button class="btn btn-sm bg-primary-subtle text-primary-emphasis rounded-4"
                    data-bs-toggle="offcanvas" data-bs-target="#offcanvasRangesDocs" aria-controls="offcanvasRangesDocs"
                    type="button">
                <i class="fa-solid fa-book me-1"></i> Documentation
            </button>
        </div>
    </div>

    <h6>Obfuscation Ranges</h6>
    <div class="row row-cols-1">
        <div class="col">
            <div class="card shadow-none rounded-top-4 rounded-bottom-0">
                <div class="card-header rounded-4 border-0 fw-bold">
                    <i class="fa-solid fa-circle-minus text-danger me-1"></i> Excludes
                </div>
                <div class="card-body pt-2" style="min-height: 20vh;">
                    <a href="#exclude-task-collapse_general_{{ $project->id }}" data-bs-toggle="collapse"
                       class="btn btn-sm btn-outline-danger border-0 fw-bold {{ (sizeof($project['config']['general']['exclude'] ?? []) > 0) ? '' : 'd-none' }}"
                       id="exclude-task-opener_general_{{ $project->id }}">
                        <i class="fa-solid fa-bars fa-fw me-1"></i> General (All tasks)
                    </a>

                    <div class="collapse my-2" id="exclude-task-collapse_general_{{ $project->id }}">
                        <ul class="list-group small" id="exclude-task-list_general_{{ $project->id }}">
                            @foreach($project['config']['general']['exclude'] ?? [] as $exclude)
                                <li class="list-group-item d-flex justify-content-between align-items-center fw-bold">
                                    {{ $exclude === '**' ? '** (All classes)' : $exclude }}
                                    <a class="text-danger remove-range-btn" href="#" data-project-id="{{ $project->id }}"
                                       data-task-id="general" data-range-type="exclude"><i class="fa-solid fa-xmark"></i></a>
                                    <input class="d-none" type="hidden" name="general[exclude][]{{ $exclude }}" value="{{ $exclude }}">
                                </li>
                            @endforeach
                        </ul>
                    </div>
                    @foreach($tasks as $task)
                        <a href="#exclude-task-collapse_{{ $task->backend_name }}_{{ $project->id }}" data-bs-toggle="collapse"
                           class="btn btn-sm btn-outline-secondary border-0
                           {{ (sizeof($project['config']['tasks'][$task->backend_name]['exclude'] ?? []) > 0) ? '' : 'd-none' }}"
                           id="exclude-task-opener_{{ $task->backend_name }}_{{ $project->id }}">
                            <i class="{{ $task->icon }} fa-fw me-1"></i> {{ $task->frontend_name }}
                        </a>

                        <div class="collapse my-2" id="exclude-task-collapse_{{ $task->backend_name }}_{{ $project->id }}">
                            <ul class="list-group small" id="exclude-task-list_{{ $task->backend_name }}_{{ $project->id }}">
                                @foreach($project['config']['tasks'][$task->backend_name]['exclude'] ?? [] as $exclude)
                                    <li class="list-group-item d-flex justify-content-between align-items-center fw-bold">
                                        {{ $exclude === '**' ? '** (All classes)' : $exclude }}
                                        <a class="text-danger remove-range-btn" href="#" data-project-id="{{ $project->id }}"
                                           data-task-id="{{ $task->backend_name }}" data-range-type="exclude"><i class="fa-solid fa-xmark"></i></a>
                                        <input class="d-none" type="hidden" name="tasks[{{ $task->backend_name }}][exclude][]{{ $exclude }}"
                                               value="{{ $exclude }}">
                                    </li>
                                @endforeach
                            </ul>
                        </div>
                    @endforeach
                </div>
            </div>
        </div>

        <div class="col">
            <div class="card shadow-none bg-light rounded-0 border-top-0 border-bottom-0 my-0">
                <div class="card-body">
                    <div class="row g-3">
                        <div class="col-8">
                            <input type="text" class="form-control form-control-sm rounded-4 input-ranges" placeholder="com/example/**/package/*Factory"
                                   aria-label="class" id="input-ranges_{{ $project->id }}"
                                   data-bs-toggle="popover" data-bs-trigger="hover" data-bs-placement="top"
                                   data-bs-content='It is recommended to first exclude all classes using "**", then include the important classes.'>
                        </div>
                        <div class="col-4 d-grid">
                            <div class="btn-group">
                                <button type="button"
                                        class="btn btn-sm bg-danger-subtle border-0 fw-bold text-danger-emphasis rounded-start-4 rounded-end-0 dropdown-toggle"
                                        data-bs-toggle="dropdown"
                                        aria-expanded="false">Exclude
                                </button>
                                <div class="dropdown-menu scrollable-menu">
                                    <ul class="list-unstyled mb-0">
                                        <li><a class="dropdown-item fw-bold ranges-btn" href="#"
                                               data-range-type="exclude" data-project-id="{{ $project->id }}" data-task-id="general">
                                                <i class="fa-solid fa-bars text-primary fa-fw me-3"></i> General (All tasks)
                                            </a></li>
                                        @foreach($tasks as $task)
                                            <li><a class="dropdown-item ranges-btn" href="#"
                                                   data-range-type="exclude" data-project-id="{{ $project->id }}" data-task-id="{{ $task->backend_name }}">
                                                    <i class="{{ $task->icon }} text-primary fa-fw me-3"></i> {{ $task['frontend_name'] }}
                                                </a></li>
                                        @endforeach
                                    </ul>
                                </div>

                                <button type="button"
                                        class="btn btn-sm bg-success-subtle border-0 fw-bold text-success-emphasis rounded-end-4 rounded-start-0 dropdown-toggle"
                                        data-bs-toggle="dropdown"
                                        aria-expanded="false">Include
                                </button>
                                <div class="dropdown-menu scrollable-menu">
                                    <ul class="list-unstyled mb-0">
                                        <li><a class="dropdown-item fw-bold ranges-btn" href="#"
                                               data-range-type="include" data-project-id="{{ $project->id }}" data-task-id="general">
                                                <i class="fa-solid fa-bars text-primary fa-fw me-3"></i> General (All tasks)
                                            </a></li>
                                        @foreach($tasks as $task)
                                            <li><a class="dropdown-item ranges-btn" href="#"
                                                   data-range-type="include" data-project-id="{{ $project->id }}" data-task-id="{{ $task->backend_name }}">
                                                    <i class="{{ $task->icon }} text-primary fa-fw me-3"></i> {{ $task['frontend_name'] }}
                                                </a></li>
                                        @endforeach
                                    </ul>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col">
            <div class="card shadow-none rounded-bottom-4 rounded-top-0 mb-3">
                <div class="card-header rounded-4 border-0 fw-bold">
                    <i class="fa-solid fa-circle-plus text-success me-1"></i> Includes
                </div>
                <div class="card-body pt-2" style="min-height: 20vh;">
                    <a href="#include-task-collapse_general_{{ $project->id }}" data-bs-toggle="collapse"
                       class="btn btn-sm btn-outline-success border-0 fw-bold {{ (sizeof($project['config']['general']['include'] ?? []) > 0) ? '' : 'd-none' }}"
                       id="include-task-opener_general_{{ $project->id }}">
                        <i class="fa-solid fa-bars fa-fw me-1"></i> General (All tasks)
                    </a>

                    <div class="collapse my-2" id="include-task-collapse_general_{{ $project->id }}">
                        <ul class="list-group small" id="include-task-list_general_{{ $project->id }}">
                            @foreach($project['config']['general']['include'] ?? [] as $include)
                                <li class="list-group-item d-flex justify-content-between align-items-center fw-bold">
                                    {{ $include === '**' ? '** (All classes)' : $include }}
                                    <a class="text-danger remove-range-btn" href="#" data-project-id="{{ $project->id }}"
                                       data-task-id="general" data-range-type="include"><i class="fa-solid fa-xmark"></i></a>
                                    <input class="d-none" type="hidden" name="general[include][]{{ $include }}" value="{{ $include }}">
                                </li>
                            @endforeach
                        </ul>
                    </div>
                    @foreach($tasks as $task)
                        <a href="#include-task-collapse_{{ $task->backend_name }}_{{ $project->id }}" data-bs-toggle="collapse"
                           class="btn btn-sm btn-outline-secondary border-0
                           {{ (sizeof($project['config']['tasks'][$task->backend_name]['include'] ?? []) > 0) ? '' : 'd-none' }}"
                           id="include-task-opener_{{ $task->backend_name }}_{{ $project->id }}">
                            <i class="{{ $task->icon }} fa-fw me-1"></i> {{ $task->frontend_name }}
                        </a>

                        <div class="collapse my-2" id="include-task-collapse_{{ $task->backend_name }}_{{ $project->id }}">
                            <ul class="list-group small" id="include-task-list_{{ $task->backend_name }}_{{ $project->id }}">
                                @foreach($project['config']['tasks'][$task->backend_name]['include'] ?? [] as $include)
                                    <li class="list-group-item d-flex justify-content-between align-items-center fw-bold">
                                        {{ $include === '**' ? '** (All classes)' : $include }}
                                        <a class="text-danger remove-range-btn" href="#" data-project-id="{{ $project->id }}"
                                           data-task-id="{{ $task->backend_name }}" data-range-type="include"><i class="fa-solid fa-xmark"></i></a>
                                        <input class="d-none" type="hidden" name="tasks[{{ $task->backend_name }}][include][]{{ $include }}"
                                               value="{{ $include }}">
                                    </li>
                                @endforeach
                            </ul>
                        </div>
                    @endforeach
                </div>
            </div>
        </div>

    </div>
    <div class="form-check form-switch">
        <input class="form-check-input" type="checkbox" role="switch"
               id="server-disable-ranges_{{ $project->id }}"
               name="server[ranges_disabled]"
            @checked($project['config']['ranges_disabled'] ?? false)>
        <label class="form-check-label small" for="server-disable-ranges_{{ $project->id }}"
               data-bs-toggle="tooltip" data-bs-placement="right"
               data-bs-title="Temporarily deactivate all excludes/includes without deleting them.">Ignore Ranges</label>
    </div>

</div>
