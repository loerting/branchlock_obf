<?php

namespace App\Console\Commands;

use App\Models\User;
use Illuminate\Console\Command;

class PurgeSandboxAccounts extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'app:purge-sandbox-accounts';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Purge sandbox accounts after 1h.';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $this->info('Purging sandbox accounts...');

        $sandboxAccounts = User::where('auth_type', 'sandbox')->get();

        foreach ($sandboxAccounts as $sandboxAccount) {
            $createdAt = $sandboxAccount->created_at;
            $now = now();
            $diff = $now->diffInMinutes($createdAt);

            if ($diff > 60) {
                $this->info('Purging sandbox account ' . $sandboxAccount->id . '...');
                $sandboxAccount->delete();
            }
        }

        $this->info('Done.');
    }
}
