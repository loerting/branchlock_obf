<div class="tab-pane fade position-relative" id="pills-general_{{ $project->id }}" role="tabpanel" aria-labelledby="pills-tab-general_{{ $project->id }}"
     tabindex="0">

    <h6>Global Settings</h6>
    <div class="col-lg-6">
        <div class="form-floating mb-3">
            <select class="form-select" id="server-bl-version_{{ $project->id }}" name="server[bl_version]" aria-label="Branchlock Version"
                @disabled(!auth()->user()->getPlan()['premium_tasks'])>
                <optgroup label="Latest">
                    @if(count($blVersions) > 0)
                        <option value="latest"
                            @selected("latest" === ($project['config']['bl_version'] ?? null))>
                            {{ $blVersions[0]['clean_name'] }}
                        </option>
                    @endif
                </optgroup>
                @if(count($blVersions) > 1)
                    <optgroup label="Previous">
                        @foreach($blVersions as $index => $version)
                            @if($index > 0)
                                <option value="{{ $version['name'] }}"
                                    @selected($version['name'] === ($project['config']['bl_version'] ?? null))>
                                    {{ $version['clean_name'] }}
                                </option>
                            @endif
                        @endforeach
                    </optgroup>
                @endif
            </select>
            <label for="server-bl-version_{{ $project->id }}">Branchlock Version</label>
        </div>

        @if(sizeof($generalSettings) > 0)
            @foreach($generalSettings as $backend_name => $setting)
                @if(($setting['android'] && $project->android) || ($setting['desktop'] && !$project->android))
                    @switch($setting['type'])
                        @case('checkbox')
                            <div class="form-check form-switch">
                                <input class="form-check-input" type="checkbox" role="switch"
                                       id="general-{{ $backend_name }}_{{ $project->id }}"
                                       name="general[{{ $backend_name }}]"
                                    @checked($project['config']['general'][$backend_name] ?? $setting['value'])
                                    @disabled($setting['premium'] && !auth()->user()->getPlan()['premium_tasks'])>
                                <label class="form-check-label small"
                                       for="general-{{ $backend_name }}_{{ $project->id }}">
                                    {{ $setting['frontend_name'] }}
                                </label>
                            </div>
                            @break

                        @case('text')
                            <div class="form-floating mb-3">
                                <input type="text" class="form-control"
                                       id="general-{{ $backend_name }}_{{ $project->id }}"
                                       name="general[{{ $backend_name }}]"
                                       value="{{ $project['config']['general'][$backend_name] ?? $setting['value'] }}"
                                    @disabled($setting['premium'] && !auth()->user()->getPlan()['premium_tasks'])>
                                <label for="general-{{ $backend_name }}_{{ $project->id }}">{{ $setting['frontend_name'] }}</label>
                            </div>
                            @break

                        @case('select')
                            <div class="form-floating mb-3">
                                <select class="form-select"
                                        id="general-{{ $backend_name }}_{{ $project->id }}"
                                        name="general[{{ $backend_name }}]"
                                        aria-label="{{ $setting['frontend_name'] }}"
                                    @disabled($setting['premium'] && !auth()->user()->getPlan()['premium_tasks'])>
                                    @foreach($setting['options'] as $value => $name)
                                        <option value="{{ $value }}"
                                            @selected($value === ($project['config']['general'][$backend_name] ?? null) ?? $setting['value'])>
                                            {{ $name }}
                                        </option>
                                    @endforeach
                                </select>
                                <label for="general-{{ $backend_name }}_{{ $project->id }}">{{ $setting['frontend_name'] }}</label>
                            </div>
                            @break

                        @default
                            <p class="text-danger mb-0">Unknown field type</p>
                    @endswitch
                @endif
            @endforeach
        @endif

    </div>

    <button type="button" data-bs-target="#deleteModal_{{ $project->id }}" data-bs-toggle="modal" class="btn btn-sm btn-outline-danger border-0 mt-3">
        <i class="fa-solid fa-trash-can me-1"></i> Delete Project
    </button>

</div>
