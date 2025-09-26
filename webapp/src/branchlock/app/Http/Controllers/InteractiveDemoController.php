<?php

namespace App\Http\Controllers;

use App\Branchlock;
use App\BranchlockRunType;
use App\Models\Changelog;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\App;
use Illuminate\Support\Facades\Storage;

class InteractiveDemoController extends Controller
{
    public function process(Request $request): JsonResponse
    {
        $validator = validator($request->all(), [
            'tasks' => 'required|array',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'code' => "/* Something went wrong. Please contact support with the following log. */\n\n" . $validator->errors()->toJson(),
            ]);
        }

        $tasks = $validator->safe()->only('tasks')['tasks'];

        // Generate binary representation of tasks and identify enabled tasks
        $bin = '';
        $tasksArray = [];
        foreach ($tasks as $key => $value) {
            $enabled = $value == "true";
            $bin .= $enabled ? '1' : '0';

            if ($enabled) {
                $tasksArray[$key]['enabled'] = true;
            }
        }

        $latestBlVersion = Changelog::getRunningVersion()['version'];
        $filePath = "{$bin}-$latestBlVersion";
        $expire = Carbon::now()->subDays(3)->timestamp;

        // Check if file needs generation or exists within expiry or in local environment
        if (!$this->isFileValid($filePath, $expire)) {
            $this->generateFile($filePath, $tasksArray);
        }

        // Check if file exists and handle accordingly
        if (!Storage::disk('demo')->exists($filePath)) {
            $code = Storage::disk('demo')->get("$filePath-log");
            return response()->json([
                'code' => "/* Something went wrong. Please contact support with the following log. */\n\n$code",
            ]);
        }

        $code = htmlspecialchars(Storage::disk('demo')->get($filePath));

        return response()->json([
            'code' => $code
        ]);
    }

    private function isFileValid(string $filePath, int $expire): bool
    {
        if (App::environment('local')) {
            return false;
        }

        return (
            Storage::disk('demo')->exists($filePath) &&
            Storage::disk('demo')->lastModified($filePath) >= $expire
        );
    }

    public function generateFile(string $filePath, array $tasksArray): void
    {
        $bl = new Branchlock(BranchlockRunType::INTERACTIVE_DEMO);
        $bl->setOutput($filePath);
        $bl->setConfig($tasksArray);
        $response = $bl->run();

        $content = htmlspecialchars(implode($response));
        Storage::disk('demo')->put("$filePath-log", $content);
    }
}
