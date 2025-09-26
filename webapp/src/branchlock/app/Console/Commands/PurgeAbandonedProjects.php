<?php

namespace App\Console\Commands;

use App\Models\Project;
use App\Models\User;
use Illuminate\Console\Command;

class PurgeAbandonedProjects extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'app:purge-abandoned-projects';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Purge abandoned projects';

    /**
     * Execute the console command.
     */
    public function handle()
    {
        $this->info('Purging abandoned projects...');

        $allProjects = Project::all();

        $allProjects->each(function ($project) {

            $user = User::find($project->user_id);
            $this->info('Checking project ' . $project->id . ' from user ' . $project->user_id);

            if (!$user) {
                $this->info('User does not exist, deleting project ' . $project->id);
                $project->deleteAll();
            }

        });
    }

}
