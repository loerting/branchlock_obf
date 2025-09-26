<?php

namespace App\Console\Commands;

use App\Models\User;
use Illuminate\Console\Command;

class PurgeUnverifiedAccounts extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'app:purge-unverified-accounts';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Command description';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $this->info('Purging unconfirmed accounts...');

        $accounts = User::where('auth_type', 'legacy')->where('status', 'unconfirmed')->get();

        foreach ($accounts as $account) {
            $this->info('Purging unconfirmed account ' . $account->id . '...');
            $account->delete();
        }

        $this->info('Done.');
    }
}
