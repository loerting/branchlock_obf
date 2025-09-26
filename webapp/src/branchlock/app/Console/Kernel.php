<?php

namespace App\Console;

use Illuminate\Console\Scheduling\Schedule;
use Illuminate\Foundation\Console\Kernel as ConsoleKernel;

class Kernel extends ConsoleKernel
{
    /**
     * Define the application's command schedule.
     *
     * @param \Illuminate\Console\Scheduling\Schedule $schedule
     * @return void
     */
    protected function schedule(Schedule $schedule)
    {
        $schedule->command('cache:prune-stale-tags')->hourly();

        $schedule->command('app:delete-old-files')->everyFifteenMinutes();

        $schedule->command('app:purge-sandbox-accounts')->everyFifteenMinutes();

        $schedule->command('app:purge-abandoned-projects')->everyFifteenMinutes();

        $schedule->command('app:purge-unverified-accounts')->everySixHours();

        $schedule->command('app:check-user-plans')->everyThreeHours();

        $schedule->command('auth:clear-resets')->everyFifteenMinutes();
    }

    /**
     * Register the commands for the application.
     *
     * @return void
     */
    protected function commands()
    {
        $this->load(__DIR__ . '/Commands');

        require base_path('routes/console.php');
    }
}
