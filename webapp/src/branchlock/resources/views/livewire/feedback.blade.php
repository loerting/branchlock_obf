<div>
    @foreach($feedbacks as $feedback)
        <div class="card shadow-none mb-3">
            <div class="card-body">
                <h6 class="mb-0">
                    <span class="text-primary-emphasis fw-semibold">{{ $feedback->user_email }}</span>
                    <span class="text-muted small ms-1">{{ $feedback->date }}</span>
                </h6>
            </div>
            <div class="card-footer">
                <textarea class="form-control" readonly aria-label="Message" style="width: 100%;height: 20vh;">{{ $feedback->message }}</textarea>
            </div>
        </div>
    @endforeach
</div>
