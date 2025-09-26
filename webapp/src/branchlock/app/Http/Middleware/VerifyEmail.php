<?php

namespace App\Http\Middleware;

use App\Models\User;
use Closure;
use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class VerifyEmail
{
    public function handle(Request $request, Closure $next)
    {
        $user = $request->user();

        if ($user->status === User::STATUS_UNCONFIRMED) {
            return redirect()->route('verification.notice');
        }

        return $next($request);
    }
}
