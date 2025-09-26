<?php

namespace App\Http\Controllers;

use App\Helpers\CustomHelper;
use App\Models\Project;
use App\Models\User;
use Illuminate\Support\Facades\Storage;

class DownloadController extends Controller
{
    public function downloadOutput($project_id)
    {
        if (auth()->user()->role === User::ROLE_SANDBOX) {
            abort(403, 'Sandbox accounts cannot download output files.');
        }

        $project = Project::findOrFail($project_id);
        $file = CustomHelper::reverseFileName($project['jar']);
        $filePath = Storage::disk('uploads')->path($file);

        if (Storage::disk('uploads')->exists($file)) {
            $originalName = pathinfo($project['jar_original_name'], PATHINFO_FILENAME) . '-obf.jar';

            return response()->download($filePath, $originalName, [
                'Content-Type' => 'application/java-archive',
            ])->deleteFileAfterSend();
        } else {
            abort(404, 'File not found');
        }
    }

}
