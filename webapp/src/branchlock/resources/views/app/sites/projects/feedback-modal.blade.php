<div class="d-flex justify-content-center">
    <div class="position-fixed bottom-0 start-50 p-3">
        <button
            class="btn btn-sm bg-success-subtle text-success-emphasis rounded-4 me-1 animate__animated animate__pulse animate__infinite animate__slower"
            type="button" data-bs-toggle="modal" data-bs-target="#feedbackModal">
            <i class="fa-solid fa-message me-1"></i> Your opinion matters
        </button>
    </div>
</div>

<div class="modal fade" id="feedbackModal" tabindex="-1" aria-labelledby="feedbackModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-fullscreen-xl-down modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5" id="feedbackModalLabel">Shape Branchlock with Us</h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div class="text-center my-3 mb-4">
                    <img src="{{ asset('img/engineer.svg') }}" class="img-fluid mb-4" alt="Feedback" width="200rem" height="auto">
                    <h6 class="text-muted mx-5">
                        Your feedback plays a pivotal role in shaping Branchlock's future.
                        Let us know how we can improve.
                    </h6>
                </div>
                <div class="mb-3">
                    <label for="feedbackMessage" class="form-label">Your Message:</label>
                    <textarea class="form-control" id="feedbackMessage" name="feedbackMessage" rows="8" required style="resize: none;"
                              value="{{ old('feedbackMessage') }}"></textarea>
                </div>
                <div class="text-center">
                    <button type="button" class="btn btn-primary" id="send-feedback-button" data-route="{{ route('app.feedback') }}">
                        <i class="fa-solid fa-paper-plane me-1"></i> Send Message
                    </button>
                </div>
            </div>
            <div class="modal-footer">

            </div>
        </div>
    </div>
</div>
