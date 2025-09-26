<?php

namespace App\Http\Middleware;

use App\Models\User;
use Closure;

class CheckPlanMiddleware
{
    public function handle($request, Closure $next)
    {
        if (auth()->check() && auth()->user()->plan === User::PLAN_FREE) {
            return response()->json([
                'status' => 'error',
                'message' => 'API is not available for free users.',
            ], 403);
        }

        return $next($request);
    }
}
