<div class="modal fade" id="renameModal_{{ $project->id }}" tabindex="-1" aria-labelledby="renameModalLabel_{{ $project->id }}" aria-hidden="true">
    <div class="modal-dialog modal-fullscreen-xl-down modal-dialog-centered">
        <div class="modal-content">
            <form action="{{ route('app.project.rename', ['id' => $project['id']]) }}" method="POST">
                <div class="modal-header">
                    <h5 class="modal-title" id="renameModalLabel_{{ $project->id }}">Rename Project</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    @csrf
                    @method('PUT')
                    <div class="mb-3">
                        <label for="new-name{{ $project->id }}" class="form-label">New Project Name</label>
                        <input type="text" class="form-control" id="new-name{{ $project->id }}" name="new_name" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-target="#projectModal_{{ $project->id }}" data-bs-toggle="modal">Cancel
                    </button>
                    <button type="submit" class="btn btn-primary">Rename</button>
                </div>
            </form>
        </div>
    </div>
</div>
