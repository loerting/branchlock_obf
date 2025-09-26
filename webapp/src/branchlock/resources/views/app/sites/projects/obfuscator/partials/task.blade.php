<div class="card mb-2 shadow-none">
    <div class="card-body">
        <div class="d-flex align-items-center justify-content-between">
            <label class="fw-bold mb-0 ms-1{{ $task->premium && !auth()->user()->getPlan()['premium_tasks'] ? ' text-muted' : '' }}"
                   for="{{ $task->id }}_{{ $project->id }}">
                <i class="{{ $task->icon }} text-primary fa-fw me-3"></i>
                @php
                    $badgeColor = match ($task->performance_cost) {
                        'CLOSE_TO_ZERO' => 'success',
                        'MINIMAL' => 'warning',
                        'NOTICEABLE' => 'danger',
                    };

                    $performance_cost = str_replace('_', ' ', $task->performance_cost);
                @endphp
                <span class="pe-1" data-bs-toggle="popover"
                      data-bs-trigger="hover" data-bs-toggle="tooltip" data-bs-placement="right" data-bs-html="true"
                      data-bs-content="{{ $task->description_long }}
                      <p class='fw-bold mb-0 mt-3'>Performance cost:</p><span class='badge text-bg-{{$badgeColor}}'>{{ $performance_cost }}</span>">
                    {{ $task->frontend_name }}
                </span>
                @if($task->experimental)
                    <span class="badge bg-warning text-dark">Experimental</span>
                @endif
            </label>

            <div class="form-check form-switch form-switch-md mb-0">
                <input class="form-check-input task-switch" type="checkbox" role="switch"
                       name="tasks[{{ $task->backend_name }}][enabled]"
                       id="{{ $task->id }}_{{ $project->id }}"
                       data-task-name="{{ $task->backend_name }}"
                    @checked($project['config']['tasks'][$task->backend_name]['enabled'] ?? false)
                    @disabled($task->premium && !auth()->user()->getPlan()['premium_tasks'])>
            </div>
        </div>

        @if(sizeof($task->settings) > 0)
            <div class="collapse {{($project['config']['tasks'][$task->backend_name]['enabled'] ?? false) ? 'show' : ''}} task-settings_{{ $project->id }}"
                 id="collapse_{{ $task->id }}_{{ $project->id }}">
                <div class="col-lg-7 mt-2 ms-5">
                    @foreach($task->settings as $val => $setting)
                        @switch($setting['type'])
                            @case('boolean')
                                <div class="form-check form-switch">
                                    <input class="form-check-input" type="checkbox" role="switch"
                                           id="{{ $val }}-{{ $task->id }}_{{ $project->id }}"
                                           name="tasks[{{ $task->backend_name }}][{{ $val }}]"
                                           data-setting-name="{{ $val }}"
                                           data-setting-task="{{ $task->backend_name }}"
                                        @checked($project['config']['tasks'][$task->backend_name][$val] ?? $setting['value'])
                                        @disabled($task->premium && !auth()->user()->getPlan()['premium_tasks'])>
                                    <label class="form-check-label small"
                                           for="{{ $val }}-{{ $task->id }}_{{ $project->id }}">
                                        {{ $setting['frontend_name'] }}
                                    </label>
                                </div>
                                @break

                            @case('string')
                                <div class="mb-3">
                                    <label class="form-label small"
                                           for="{{ $val }}-{{ $task->id }}_{{ $project->id }}">
                                        {{ $setting['frontend_name'] }}</label>
                                    <input type="text" class="form-control form-control-sm"
                                           id="{{ $val }}-{{ $task->id }}_{{ $project->id }}"
                                           name="tasks[{{ $task->backend_name }}][{{ $val }}]"
                                           value="{{ $project['config']['tasks'][$task->backend_name][$val] ?? $setting['value'] }}"
                                           data-setting-name="{{ $val }}"
                                           data-setting-task="{{ $task->backend_name }}"
                                        @disabled($task->premium && !auth()->user()->getPlan()['premium_tasks'])>
                                </div>
                                @break

                            @case('password')
                                <div class="mb-3">
                                    <label class="form-label small"
                                           for="{{ $val }}-{{ $task->id }}_{{ $project->id }}">
                                        {{ $setting['frontend_name'] }}</label>
                                    <input type="password" class="form-control form-control-sm"
                                           id="{{ $val }}-{{ $task->id }}_{{ $project->id }}"
                                           name="tasks[{{ $task->backend_name }}][{{ $val }}]"
                                           value="{{ $project['config']['tasks'][$task->backend_name][$val] ?? $setting['value'] }}"
                                           data-setting-name="{{ $val }}"
                                           data-setting-task="{{ $task->backend_name }}"
                                        @disabled($task->premium && !auth()->user()->getPlan()['premium_tasks'])>
                                </div>
                                @break

                            @case('number')
                                <div class="mb-3">
                                    <label class="form-label small"
                                           for="{{ $val }}-{{ $task->id }}_{{ $project->id }}">
                                        {{ $setting['frontend_name'] }}</label>
                                    <input type="number" class="form-control form-control-sm"
                                           id="{{ $val }}-{{ $task->id }}_{{ $project->id }}"
                                           name="tasks[{{ $task->backend_name }}][{{ $val }}]"
                                           value="{{ $project['config']['tasks'][$task->backend_name][$val] ?? $setting['value'] }}"
                                           data-setting-name="{{ $val }}"
                                           data-setting-task="{{ $task->backend_name }}"
                                        @disabled($task->premium && !auth()->user()->getPlan()['premium_tasks'])>
                                </div>
                                @break

                            @case('float')
                                <label for="{{ $val }}-{{ $task->id }}_{{ $project->id }}" class="form-label">{{ $setting['frontend_name'] }}</label>
                                <input type="range" class="form-range" min="0" max="9" id="{{ $val }}-{{ $task->id }}_{{ $project->id }}"
                                       name="tasks[{{ $task->backend_name }}][{{ $val }}]"
                                       @php
                                           $value = $project['config']['tasks'][$task->backend_name][$val] ?? $setting['value'];
                                           $formattedValue = ceil($value * 10);
                                       @endphp
                                       value="{{ $formattedValue }}"
                                       data-setting-name="{{ $val }}"
                                       data-setting-task="{{ $task->backend_name }}"
                                    @disabled($task->premium && !auth()->user()->getPlan()['premium_tasks'])>
                                @break

                            @default
                                <p class="text-danger mb-0">Unknown field type</p>

                        @endswitch
                    @endforeach
                </div>
            </div>
        @endif
    </div>
</div>
