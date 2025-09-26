<?php

namespace App\Http\Middleware;

use App\Models\Project;
use Closure;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Validator;

class CheckPrjectExistanceByIdMiddleware
{
    public function handle($request, Closure $next)
    {
        $validator = null;

        if ($request->isMethod('get')) {
            $validator = Validator::make($request->route()->parameters(), [
                'project_id' => ['required', 'string', 'max:30'],
            ]);
        } elseif ($request->isMethod('post')) {
            $validator = Validator::make($request->all(), [
                'project_id' => ['required', 'string', 'max:30'],
            ]);
        }

        if ($validator && $validator->fails()) {
            return response()->json([
                'status' => 'error',
                'message' => $validator->errors()->first(),
            ], 400);
        }

        $projectId = $validator->safe()->only(['project_id'])['project_id'] ?? null;

        if (!$projectId) {
            return $next($request);
        }

        $project = Project::where('user_id', Auth::id())
            ->where('project_id', $projectId)
            ->first();

        if (!$project) {
            return response()->json([
                'status' => 'error',
                'message' => 'Project not found.',
            ], 404);
        }

        $request->attributes->add(['project' => $project]);

        return $next($request);
    }
}
