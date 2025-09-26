<?php

namespace App\Http\Controllers;

use App\Helpers\UploadHelper;
use App\Models\Project;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class JarController extends Controller
{
    public function upload(Request $request, $project_id): \Illuminate\Http\JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'jar' => 'required|file|max:100000|min:1',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => 'error',
                'message' => $validator->errors()->first('jar')
            ], 400);
        }

        $jar = $request->file('jar');
        $project = Project::findOrFail($project_id);

        if ($request->has('dzchunkindex')) {
            return UploadHelper::handleChunkedUpload($request, $project, $jar);
        }

        return response()->json([
            'status' => 'error',
            'message' => 'Invalid request'
        ], 400);
    }

    public function delete(Request $request, $project_id): \Illuminate\Http\JsonResponse
    {
        $project = Project::findOrFail($project_id);

        $project->removeInput();

        return response()->json([
            'status' => 'success',
            'message' => 'File deleted successfully'
        ]);
    }

}
