<?php

namespace App\Http\Controllers\Api;

use App\Helpers\CustomHelper;
use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Redis;
use Illuminate\Support\Facades\Storage;

class JobStatus extends Controller
{
    public function __invoke(Request $request): JsonResponse
    {
        $project = $request->attributes->get('project');

        $jobStatus = Redis::get('project:lock:' . Auth::id() . ':' . $project->id);
        if ($jobStatus) {
            return response()->json([
                'status' => 'running',
                'message' => 'Job is running.',
                'project_id' => $project->project_id,
            ]);
        }

        $file = CustomHelper::reverseFileName($project->jar);

        if (Storage::disk('uploads')->exists($file)) {
            return response()->json([
                'status' => 'completed',
                'message' => 'Job is completed.',
                'project_id' => $project->project_id,
                'download_url' => route('api.download', ['project_id' => $project->project_id]),
                'log' => Storage::disk('uploads')->get($project->id . '-log'),
            ]);
        }

        return response()->json([
            'status' => 'error',
            'message' => 'Job not found.',
        ], 404);
    }
}
