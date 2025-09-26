<?php

namespace App\Http\Middleware;

use App\Models\User;
use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class NoSandboxMiddleware
{
    public function handle($request, Closure $next)
    {
        if (auth()->check() && auth()->user()->role === User::ROLE_SANDBOX) {
            return response()->json([
                'status' => 'error',
                'message' => 'API is not available for sandbox users.',
            ], 403);
        }

        return $next($request);
    }
}
