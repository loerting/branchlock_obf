<?php

namespace App\Http\Controllers;

use App\Branchlock;
use App\BranchlockRunType;
use App\Models\Project;
use App\Models\User;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Redis;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Validator;

class ObfuscatorController extends Controller
{

    public function process(Request $request, $project_id): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'tasks' => 'nullable|array|max:255',
            'tasks.*' => 'nullable|max:255',

            'general' => 'nullable|array|max:255',
            'general.*' => 'nullable|max:255',

            'server' => 'nullable|array|max:255',
            'server.*' => 'nullable|max:255',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'error' => true,
                'status' => 'error',
                'message' => 'Invalid request',
            ], 400);
        }

        $validated = $validator->safe()->only(['tasks', 'general', 'server'], []);

        $project = Project::findOrFail($project_id);


        if (!$this->isJarFileValid($project)) {
            return response()->json([
                'error' => true,
                'status' => 'error',
                'message' => 'Input file not found',
            ], 400);
        }

        $limit = ObfuscatorController::isUserRateLimited($project->id);
        if ($limit['error']) {
            return response()->json([
                'error' => true,
                'status' => 'error',
                'message' => $limit['message'],
            ], 429);
        }

        $config = $validated;

        if ($config['server']['ranges_disabled'] ?? false) {
            Branchlock::removeRanges($config);
        }

        $bl = new Branchlock(BranchlockRunType::WEB_OBFUSCATION);
        if (auth()->user()['plan'] !== User::PLAN_FREE && isset($config['server']['bl_version']) && $config['server']['bl_version'] !== 'latest') {
            $bl->setBlVersion($config['server']['bl_version']);
        }
        $bl->setProjectId($project->id);
        $bl->setInput($project->jar);
        $bl->setLibraries($project->libs);
        $bl->setDemo(auth()->user()['role'] === User::ROLE_ADMIN ? $config['server']['demo_mode'] ?? false : auth()->user()->getPlan()['demo_mode']);
        $bl->setDebug(auth()->user()['role'] === User::ROLE_ADMIN ? $config['server']['debug_mode'] ?? false : false);
        $bl->setJavaDebug(auth()->user()['role'] === User::ROLE_ADMIN ? $config['server']['java_verbose'] ?? false : false);
        $bl->setConfig($config['tasks'], $config['general']);
        $response = $bl->run();


        return response()->json([
            'error' => false,
            'status' => $response['status'],
            'message' => $response['message'],
            'cooldown' => auth()->user()->getPlan()['cooldown'],
            //'config' => $response['json'],
        ]);
    }

    private function isJarFileValid(Project $project): bool
    {
        return isset($project->jar) && Storage::disk('uploads')->exists($project->jar);
    }

    public static function isUserRateLimited(string $projectId): array
    {
        $projectLockKey = Redis::get('project:lock:' . Auth::id() . ':' . $projectId);
        $currentJobCount = Redis::get('job:count:' . Auth::id());

        if ($projectLockKey) {
            return [
                'error' => true,
                'message' => 'This project is being processed.',
            ];
        }

        if ($currentJobCount >= auth()->user()->getPlan()['concurrent_jobs']) {
            return [
                'error' => true,
                'message' => 'You have reached your concurrent job limit.',
            ];
        }

        return [
            'error' => false,
            'message' => 'You can start a new job.',
        ];
    }

}
