<?php

namespace App\Helpers;

use App\Models\Project;
use Illuminate\Http\Request;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Str;

class UploadHelper
{
    public static function handleChunkedUpload(Request $request, Project $project, $jar, $lib = false): \Illuminate\Http\JsonResponse
    {
        $chunkNumber = $request->input('dzchunkindex');
        $chunkSize = $request->input('dzchunksize');
        $totalChunks = $request->input('dztotalchunkcount');
        $uploadPath = storage_path('app/chunks');

        $uniqueName = Str::slug($project->id . $jar->getClientOriginalName());

        $chunkFileName = $uniqueName . '_chunk_' . $chunkNumber;
        $jar->storeAs('chunks', $chunkFileName, 'local');

        if ($chunkNumber == $totalChunks - 1) {
            return self::handleFinalChunk($uniqueName, $totalChunks, $uploadPath, $project, $jar, $lib);
        } else {
            return response()->json(['status' => 'success', 'message' => 'Chunk uploaded successfully']);
        }
    }

    private static function handleFinalChunk($uniqueName, $totalChunks, $uploadPath, Project $project, $jar, $lib = false): \Illuminate\Http\JsonResponse
    {
        $finalFileName = $uniqueName;
        $finalFilePath = $uploadPath . '/' . $finalFileName;

        for ($i = 0; $i < $totalChunks; $i++) {
            $chunkFilePath = $uploadPath . '/' . $uniqueName . '_chunk_' . $i;
            file_put_contents($finalFilePath, file_get_contents($chunkFilePath), FILE_APPEND);
            unlink($chunkFilePath);
        }

        $finalFile = new UploadedFile($finalFilePath, $jar->getClientOriginalName());

        if ($lib) {
            $newFileName = $project->addLib($finalFile);
        } else {
            $project->addInput($finalFile);
        }

        $fileSize = filesize($finalFilePath);
        unlink($finalFilePath);

        return response()->json([
            'status' => 'success',
            'message' => 'File uploaded successfully',
            'file' => htmlspecialchars($jar->getClientOriginalName()),
            'id' => $newFileName ?? null,
            'size' => CustomHelper::formatBytes($fileSize),
            'uploaded_at' => 'Just now'
        ]);
    }

}
