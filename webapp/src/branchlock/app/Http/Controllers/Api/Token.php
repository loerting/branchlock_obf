<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Sanctum\PersonalAccessToken;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class Token extends Controller
{
    public function __invoke(Request $request): JsonResponse
    {
        $tokenCount = PersonalAccessToken::where('tokenable_id', Auth::user()->id)->count();

        if ($tokenCount > 0) {
            PersonalAccessToken::where('tokenable_id', Auth::user()->id)->delete();
        }

        $token = Auth::user()->createToken('branchlock-token')->plainTextToken;

        return response()->json([
            'status' => 'success',
            'message' => 'Token created',
            'token' => $token,
            'token_count' => $tokenCount
        ]);

    }
}
