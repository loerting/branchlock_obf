<?php

namespace App\Http\Middleware;

use App\Models\Project;
use Closure;
use Illuminate\Support\Facades\Auth;

class CheckProjectOwnership
{
    /**
     * Handle an incoming request.
     *
     * @param \Closure(\Illuminate\Http\Request): (\Symfony\Component\HttpFoundation\Response) $next
     */
    public function handle($request, Closure $next)
    {
        $projectId = $request->route('id');
        $project = Project::findOrFail($projectId);

        if (!$project || $project->user_id !== Auth::id()) {
            return response()->json([
                'status' => 'error',
                'message' => 'Project not found or does not belong to the user'
            ]);
        }

        return $next($request);
    }
}
