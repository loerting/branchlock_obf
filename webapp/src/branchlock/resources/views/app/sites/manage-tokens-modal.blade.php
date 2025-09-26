<div class="modal fade" id="manageTokensModal" tabindex="-1" aria-labelledby="manageTokensModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-fullscreen-xl-down modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="manageTokensModalLabel">Bearer token</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                @php
                    $tokenCount = \Laravel\Sanctum\PersonalAccessToken::where('tokenable_id', auth()->user()->id)->count();
                @endphp
                <div id="token-area">
                    @if($tokenCount === 0)
                        <p class="text-muted mb-0">You haven't generated a token yet.</p>
                    @else
                        <p class="text-muted mb-0">The API token is displayed only once during generation. If you lose it, you can generate a new one, but
                            keep in mind the old one will be deactivated.</p>
                    @endif
                </div>
            </div>
            <div class="modal-footer">
                <button type="submit" class="btn btn-primary" id="generate-token">Generate Token</button>
            </div>
        </div>
    </div>
</div>
