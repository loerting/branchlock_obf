<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\Storage;

class DeleteOldFiles extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'app:delete-old-files';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Delete all uploaded user files/configs older than 6 hours';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $files = Storage::disk('uploads')->allFiles();
        $maxStoreTime = Carbon::now()->subHours(config('settings.max_storage_time'))->timestamp;

        foreach ($files as $file) {
            $fileTimestamp = Storage::disk('uploads')->lastModified($file);

            if ($fileTimestamp < $maxStoreTime) {
                unlink(Storage::disk('uploads')->path($file));
            }
        }

        $this->info('All files processed');
    }
}
