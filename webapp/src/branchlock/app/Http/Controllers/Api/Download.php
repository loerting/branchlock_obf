<?php

namespace App\Http\Controllers\Api;

use App\Helpers\CustomHelper;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

class Download extends Controller
{
    public function __invoke(Request $request): \Symfony\Component\HttpFoundation\BinaryFileResponse|\Illuminate\Http\JsonResponse
    {
        $project = $request->attributes->get('project');

        if ($project['jar'] === null) {
            return response()->json([
                'status' => 'error',
                'message' => 'File not found.'
            ], 404);
        }

        $file = CustomHelper::reverseFileName($project['jar']);

        if (Storage::disk('uploads')->exists($file)) {
            $originalName = pathinfo($project['jar_original_name'], PATHINFO_FILENAME) . '-obf.jar';
            $filePath = Storage::disk('uploads')->path($file);

            return response()->download($filePath, $originalName, [
                'Content-Type' => 'application/java-archive',
            ])->deleteFileAfterSend();
        } else {
            return response()->json([
                'status' => 'error',
                'message' => 'File not found.',
            ], 404);
        }
    }
}
