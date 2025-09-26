<div class="tab-pane fade" id="pills-config_{{ $project->id }}" role="tabpanel" aria-labelledby="pills-tab-config_{{ $project->id }}"
     tabindex="0">

    <div class="d-flex justify-content-center">
        <div class="position-fixed p-3" style="bottom: 1.65rem; z-index: 1000;">
            <button class="btn btn-sm bg-primary-subtle text-primary-emphasis rounded-4 border-0 me-1 toggle-all" type="button"
                    data-project-id="{{ $project->id }}">
                <i class="fa-solid fa-toggle-on me-1"></i> Toggle all
            </button>
            <button class="btn btn-sm bg-danger-subtle text-danger-emphasis rounded-4 me-1 reset-tasks" type="button"
                    data-project-id="{{ $project->id }}"
                    data-route="{{ route('app.project.tasks.reset', ['id' => $project->id]) }}">
                <i class="fa-solid fa-delete-left me-1"></i> Reset all
            </button>
            <button class="btn btn-sm bg-success-subtle text-success-emphasis rounded-4 me-1"
                    data-bs-toggle="offcanvas" data-bs-target="#offcanvasScrolling" aria-controls="offcanvasScrolling" type="button">
                <i class="fa-solid fa-box me-1"></i> Templates
            </button>
            <button class="btn btn-sm bg-secondary-subtle text-secondary-emphasis rounded-4 me-1" type="button"
                    data-bs-toggle="offcanvas" data-bs-target="#offcanvasAnnotations" aria-controls="offcanvasAnnotations">
                <i class="fa-solid fa-at me-1"></i> Annotations
            </button>
        </div>
    </div>

    <div class="row row-cols-1">
        @php
            $tasksType = (!$project->android) ? $desktopTasks : $androidTasks;
        @endphp
        @foreach($tasksType as $c => $group)
            <div class="col mb-4">
                <h6>{{ $c }} Tasks</h6>
                <div class="row row-cols-1 row-cols-lg-1">
                    <div class="col">
                        @foreach($group as $task)
                            @include('app.sites.projects.obfuscator.partials.task')
                        @endforeach
                    </div>
                </div>
            </div>
        @endforeach
    </div>


</div>
