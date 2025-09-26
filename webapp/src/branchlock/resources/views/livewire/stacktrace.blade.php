<div>

    <div class="mb-3">
        <label for="textarea" class="form-label">Stacktrace</label>
        <textarea wire:model="textarea" class="form-control" id="textarea" rows="15"></textarea>
    </div>
    <div class="mb-3">
        <label for="key" class="form-label">Decryption Key</label>
        <input wire:model="decryptionKey" type="text" class="form-control" id="key">
    </div>
    <button wire:click="decrypt" class="btn btn-primary">Decrypt</button>

</div>
