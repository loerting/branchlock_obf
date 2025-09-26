<div>
    <div class="row mb-3">
        <div class="col">
            <input wire:model.live="title" type="text" class="form-control" aria-label="Title" placeholder="Subject">
            @error('title')
            <span class="text-danger">{{ $message }}</span>
            @enderror

            <textarea wire:model="body" wire:input="renderBodyPreview" class="form-control mt-3"
                      placeholder="Message (Markdown is supported)" rows="20" aria-label="Message"
                      style="min-height: 10rem; max-height: 38rem;"></textarea>
            @error('body')
            <span class="text-danger">{{ $message }}</span>
            @enderror
        </div>
        <div class="col">
            <div class="card shadow-none h-100">
                <div class="card-header">
                    {{ $title }}
                </div>
                <div class="card-body">
                    {!! $htmlBody !!}
                </div>
                <div class="card-footer">

                </div>
            </div>
        </div>
    </div>

    <p class="mt-4">Who should receive this message:</p>
    <div class="form-check form-switch">
        <input wire:model="admins" class="form-check-input" type="checkbox" role="switch" id="emailAdmins">
        <label class="form-check-label" for="emailAdmins">Administrators</label>
    </div>
    <div class="form-check form-switch">
        <input wire:model="licensedUsers" class="form-check-input" type="checkbox" role="switch" id="emailLicensedUsers">
        <label class="form-check-label" for="emailLicensedUsers">Licensed Users</label>
    </div>
    <div class="form-check form-switch">
        <input wire:model="DemoUsers" class="form-check-input" type="checkbox" role="switch" id="emailDemoUsers">
        <label class="form-check-label" for="emailDemoUsers">Demo Users</label>
    </div>
    <div class="form-check form-switch mt-4">
        <input wire:model="onlyNoDateUsers" class="form-check-input" type="checkbox" role="switch" id="emailNoDateUsers">
        <label class="form-check-label" for="emailNoDateUsers">Restrict to users who have not logged in until now</label>
    </div>


    <button wire:click="send" type="button" class="btn btn-primary mt-4" wire:loading.attr="disabled">
        <span wire:loading.remove wire:target="send">Send</span>
        <span wire:loading wire:target="send">Sending... <i class="fas fa-spinner fa-spin"></i></span>
    </button>

</div>
