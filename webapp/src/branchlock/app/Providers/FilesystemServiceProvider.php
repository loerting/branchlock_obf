<?php

namespace App\Providers;

use Illuminate\Support\ServiceProvider;

class FilesystemServiceProvider extends ServiceProvider
{
    public function boot()
    {

        $filesystems = config('filesystems.disks');

        foreach ($filesystems as $disk => $config) {
            if ($config['driver'] === 'local') {
                $root = $config['root'] ?? storage_path('app/public');

                if (!is_dir($root)) {
                    mkdir($root, 0755, true);
                }
            }
        }
    }

    public function register()
    {
        //
    }
}
