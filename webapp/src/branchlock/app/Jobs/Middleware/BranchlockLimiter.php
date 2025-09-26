<?php

namespace App\Jobs\Middleware;

use App\Events\OutputFileAvailable;
use App\Helpers\CustomHelper;
use App\Models\Project;
use App\Models\User;
use Closure;
use Illuminate\Support\Facades\App;
use Illuminate\Support\Facades\Redis;
use Illuminate\Support\Facades\Storage;

class BranchlockLimiter
{
    /**
     * Process the queued job.
     *
     * @param \Closure(object): void $next
     */
    public function handle(object $job, Closure $next): void
    {
        if ($job->sync) {
            $next($job);
            return;
        }

        $startTime = microtime(true);

        $jobCountKey = 'job:count:' . $job->userId;
        $projectLockKey = 'project:lock:' . $job->userId . ':' . $job->projectId;

        $userPlan = User::find($job->userId)->plan;

        if (Redis::exists($jobCountKey)) {
            try {
                $currentJobCount = Redis::get($jobCountKey);
                if ($currentJobCount >= User::getPlanVar($userPlan, 'concurrent_jobs')) {
                    $job->fail(new \Exception('Maximum concurrent jobs reached for your plan.'));
                    return;
                }
            } catch (\Throwable $exception) {
                report($exception);
            }
        }

        if (Redis::exists($projectLockKey)) {
            $job->fail(new \Exception('Project is locked.'));
            return;
        }

        try {
            Redis::incr($jobCountKey);

            Redis::expire($jobCountKey, 5 * 60); // if something goes wrong, we don't want to lock the user out forever

            Redis::set($projectLockKey, true);
            Redis::expire($projectLockKey, 5 * 60);

            $next($job);
        } catch (\Throwable $exception) {
            $job->fail($exception);
            Redis::decr($jobCountKey);
            report($exception);
        } finally {
            try {
                $project = Project::find($job->projectId);

                $endTime = microtime(true);
                $duration = $endTime - $startTime;

                $outputFile = CustomHelper::reverseFileName($project['jar']);
                $deleteJar = $project->config['delete_jar'] ?? false;
                $deleteLibs = $project->config['delete_libs'] ?? false;

                if ($deleteJar) {
                    Storage::disk('uploads')->delete($project->jar);
                    //$project->jar = null;
                }
                if ($deleteLibs) {
                    if (isset($project->libs)) {
                        foreach ($project->libs as $lib) {
                            if (Storage::disk('uploads')->exists($lib['file'])) {
                                Storage::disk('uploads')->delete($lib['file']);
                            }
                        }

                        $project->libs = [];
                    }
                }

                $project->save();

                if (Storage::disk('uploads')->exists($outputFile)) {
                    event(new OutputFileAvailable($job->userId, $job->projectId, $deleteJar, $deleteLibs));
                }

                Redis::del('project:lock:' . $job->userId . ':' . $job->projectId);

                $cooldown = User::getPlanVar($userPlan, 'cooldown') ?? 0;
                if ($cooldown > 0) {
                    Redis::expire($jobCountKey, $cooldown);
                }
            } catch (\Throwable $exception) {
                report($exception);
            }
        }
    }
}

