<?php

namespace App\Http\Controllers\Api;

use App\Branchlock;
use App\BranchlockRunType;
use App\Http\Controllers\Controller;
use App\Http\Controllers\ObfuscatorController;
use App\Models\User;
use App\Rules\ValidJarFile;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Validator;

class CreateJob extends Controller
{
    public function __invoke(Request $request): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'input' => ['required', 'file', 'max:100000', new ValidJarFile()],
            'libraries' => ['nullable', 'array', 'max:250'],
            'libraries.*' => ['required', 'file', 'max:100000'],
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => 'error',
                'message' => $validator->errors()->first(),
            ], 400);
        }

        $project = $request->attributes->get('project');
        $validated = $validator->safe()->only(['input', 'libraries']);

        $limit = ObfuscatorController::isUserRateLimited($project->id);
        if ($limit['error']) {
            return response()->json([
                'status' => 'error',
                'message' => $limit['message'],
            ], 429);
        }

        $project->addInput($validated['input']);

        $project->removeAllLibs();

        if ($validated['libraries'] ?? false) {
            foreach ($validated['libraries'] as $library) {
                $project->addLib($library);
            }
        }

        if ($project->android) {
            $jarFile = Storage::path('private/branchlock-files/android33_stubs.jar');
            $fileInstance = new UploadedFile($jarFile, 'android33_stubs.jar');

            $project->addLib($fileInstance);
        }

        $config = $project->config ?? [];

        if ($config['ranges_disabled'] ?? false) {
            Branchlock::removeRanges($config);
        }

        $bl = new Branchlock($project->android ? BranchlockRunType::ANDROID_OBFUSCATION : BranchlockRunType::WEB_OBFUSCATION);
        if (auth()->user()['plan'] !== User::PLAN_FREE && isset($config['bl_version']) && $config['bl_version'] !== 'latest') {
            $bl->setBlVersion($config['bl_version']);
        }
        $bl->setProjectId($project->id);
        $bl->setInput($project->jar);
        $bl->setLibraries($project->libs);
        $bl->setDemo(auth()->user()['role'] === User::ROLE_ADMIN ? $config['demo_mode'] ?? false : auth()->user()->getPlan()['demo_mode']);
        $bl->setDebug(auth()->user()['role'] === User::ROLE_ADMIN ? $config['debug_mode'] ?? false : false);
        $bl->setJavaDebug(auth()->user()['role'] === User::ROLE_ADMIN ? $config['java_verbose'] ?? false : false);
        $bl->setConfig($config['tasks'] ?? [], $config['general'] ?? []);
        $response = $bl->run(true);

        $config = file_get_contents($response['json']);

        return response()->json([
            'status' => $response['status'],
            'message' => $response['message'],
            'project_id' => $project->project_id,
           // 'config' => json_encode($config),
        ]);
    }
}
