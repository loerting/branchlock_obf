<div class="modal fade" id="addNewProjectModal" tabindex="-1" aria-labelledby="addNewProjectModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-fullscreen-xl-down modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5" id="addNewProjectModalLabel">Add a new project</h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form method="post" action="{{ route('app.project.add') }}">
                @csrf
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="newProjectName" class="form-label">Name</label>
                        <input type="text" class="form-control" id="newProjectName" name="name" placeholder="My new App" autocomplete="on">
                    </div>
                    <div class="mb-3">
                        <label for="newProjectId" class="form-label">Project id</label>
                        <input type="text" class="form-control" id="newProjectId" name="project_id" aria-describedby="projectIdHelp"
                               placeholder="my-new-app">
                        <div id="projectIdHelp" class="form-text">
                            Alphanumeric characters and dashes are allowed. Dashes can be used as replacements for spaces.
                        </div>
                    </div>
                    <div class="form-check form-switch form-switch-md">
                        <input class="form-check-input" type="checkbox" role="switch" id="newProjectAndroid" name="android">
                        <label class="form-check-label" for="newProjectAndroid">Android</label>
                    </div>
                </div>
                <div class="modal-footer">
                        <span class="d-inline-block" tabindex="0" data-bs-toggle="popover"
                              data-bs-trigger="{{ Auth()->user()->maxProjectsReached() ? 'hover focus' : 'manual' }}"
                              data-bs-content="The maximum number of projects allowed for your current plan has been reached.
                              You'll need to delete one project before creating a new one.">
                        <button type="submit" class="btn btn-primary"
                            @disabled(Auth()->user()->maxProjectsReached())>
                            Add project
                        </button>
                        </span>
                </div>
            </form>
        </div>
    </div>
</div>
