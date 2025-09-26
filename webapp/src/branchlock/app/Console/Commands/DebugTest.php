<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;

class DebugTest extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'app:debug-test';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Just a test command to debug';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        // generate file within upload folder with timestamp
        $filename = 'debug-test-' . time() . '.txt';
        $filepath = storage_path('app/uploads/' . $filename);

        file_put_contents($filepath, 'Debug test file created at ' . date('Y-m-d H:i:s'));

        $this->info('Debug test file created successfully');
    }
}
