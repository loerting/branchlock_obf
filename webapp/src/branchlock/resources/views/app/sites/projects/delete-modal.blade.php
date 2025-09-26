<div class="modal fade" id="deleteModal_{{ $project->id }}" tabindex="-1" aria-labelledby="deleteModalLabel_{{ $project->id }}" aria-hidden="true">
    <div class="modal-dialog modal-fullscreen-xl-down modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="deleteModalLabel_{{ $project->id }}">Confirm Deletion</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                Are you sure you want to delete <span class="fw-bold">{{ $project['name'] }}<br><span class="text-muted">id: {{ $project['project_id'] }}</span></span><br>
                This will delete all files and settings associated with this project.
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-target="#projectModal_{{ $project->id }}" data-bs-toggle="modal">Cancel</button>
                <form action="{{ route('app.project.delete', ['id' => $project['id']]) }}" method="POST">
                    @csrf
                    @method('DELETE')
                    <button type="submit" class="btn btn-danger">Delete</button>
                </form>
            </div>
        </div>
    </div>
</div>
