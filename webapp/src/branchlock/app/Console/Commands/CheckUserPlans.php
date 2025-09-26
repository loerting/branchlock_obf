<?php

namespace App\Console\Commands;

use App\Models\User;
use Illuminate\Console\Command;

class CheckUserPlans extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'app:check-user-plans';

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
        $this->info('Checking user plans...');

        $users = User::all();

        foreach ($users as $user) {
            if ($user->purchase_end_date && $user->purchase_end_date < now()) {
                $this->info('User ' . $user->id . ' plan has expired.');
                $user->purchase_end_date = null;
                $user->plan = 'free';
                $user->save();
            }
        }

        $this->info('Done.');
    }
}
