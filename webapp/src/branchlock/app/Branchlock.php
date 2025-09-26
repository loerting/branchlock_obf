<?php

namespace App;

use App\Helpers\CustomHelper;
use App\Http\Controllers\ObfuscatorController;
use App\Jobs\BranchlockExecution;
use App\Models\Task;
use Carbon\Carbon;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;
use stdClass;

class Branchlock
{
    protected string $blVersion;
    protected string $input;
    protected string $output;
    protected array $libraries = [];
    protected bool $demo = false;
    protected bool $debug = false;
    protected bool $javaDebug = false;
    protected array $tasks = [];
    protected array $general = [];
    protected string $projectId;
    protected string $stacktraceKey;
    protected BranchlockRunType $runType;

    public function __construct(BranchlockRunType $runType)
    {
        $this->runType = $runType;
    }

    public function setBlVersion(string $blVersion): void
    {
        if (Storage::disk('versions')->exists($blVersion)) {
            $this->blVersion = $blVersion;
        }
    }

    public function setDemo(bool $demo): void
    {
        $this->demo = $demo;
    }

    public function setDebug(bool $debug): void
    {
        $this->debug = $debug;
    }

    public function setJavaDebug(bool $javaDebug): void
    {
        $this->javaDebug = $javaDebug;
    }

    public function setInput(string $input): void
    {
        $this->input = Storage::disk('uploads')->path("$input");
        if ($this->runType === BranchlockRunType::WEB_OBFUSCATION || $this->runType === BranchlockRunType::ANDROID_OBFUSCATION) {
            $this->output = Storage::disk('uploads')->path(CustomHelper::reverseFileName($input));
        }
    }

    public function setOutput(string $output): void
    {
        $disk = $this->runType === BranchlockRunType::INTERACTIVE_DEMO ? 'demo' : 'uploads';
        $this->output = Storage::disk($disk)->path("$output");
    }

    public function setLibraries(array $libraries): void
    {
        $libraryPaths = array_map(function ($library) {
            return Storage::disk('uploads')->path($library['file']) ?? null;
        }, $libraries);

        $libraryPaths = array_filter($libraryPaths);

        $this->libraries = $libraryPaths;
    }

    public function setConfig(array $tasks = [], array $general = []): void
    {
        $this->tasks = $tasks;
        $this->general = $general;
    }

    public function setProjectId(string $projectId): void
    {
        $this->projectId = $projectId;
    }

    public function setStacktraceKey(string $stacktraceKey): void
    {
        $this->stacktraceKey = $stacktraceKey;
    }


    public function run(bool $api = false): array
    {
        $config = $this->buildConfig();

        [$jsonPath, $blPath, $runClass] = $this->prepare(json_encode($config));


        if ($this->runType === BranchlockRunType::WEB_OBFUSCATION || $this->runType === BranchlockRunType::ANDROID_OBFUSCATION) {

            $limit = ObfuscatorController::isUserRateLimited($this->projectId);
            if ($limit['error']) {
                return [
                    'error' => true,
                    'status' => 'error',
                    'message' => $limit['message'],
                ];
            }

            BranchlockExecution::dispatch($blPath, $runClass, $jsonPath, Auth::id(), $this->projectId, false, $api, $this->javaDebug)
                ->onQueue(auth()->user()->getPlan()['queue_priority']);

            return [
                'status' => 'success',
                'message' => 'Process queued',
                'json' => $jsonPath
            ];
        }

        return (new BranchlockExecution($blPath, $runClass, $jsonPath, null, null, true))->handle();
    }

    public function buildConfig(): array
    {
        if ($this->runType !== BranchlockRunType::STACKTRACE_DECRYPTION) {
            return array_merge([
                'input' => $this->input ?? null,
                'output' => $this->output ?? null,
                'libraries' => $this->libraries,
                'demo' => $this->demo,
                'android' => $this->runType === BranchlockRunType::ANDROID_OBFUSCATION,
                'debug_mode' => $this->debug,

            ], $this->formatConfig(
                (!empty($this->tasks) ? $this->tasks : null),
                (!empty($this->general) ? $this->general : null),
                !$this->demo, $this->runType === BranchlockRunType::ANDROID_OBFUSCATION
            ));
        }

        return [
            'input' => $this->input ?? null,
            'output' => $this->output ?? null,
            'key' => $this->stacktraceKey ?? null,
            'general' => new stdClass()
        ];
    }

    private function prepare($config): array
    {
        $disk = $this->runType === BranchlockRunType::INTERACTIVE_DEMO ? 'demo' : 'uploads';
        $jsonName = pathinfo($this->output, PATHINFO_FILENAME) . '.json';
        Storage::disk($disk)->put($jsonName, $config);

        $jsonPath = Storage::disk($disk)->path($jsonName);
        // if blVersion is null, then it will be set to the latest version, otherwise it will be set to the version specified in the config
        $blPath = Storage::disk('versions')->path($this->blVersion ?? $this->getVersions()[0]['name']);
        $runClass = $this->runType->getRunClass();

        return [$jsonPath, $blPath, $runClass];
    }

    public static function getVersions(): array
    {
        return collect(Storage::disk('versions')->allFiles())
            ->filter(fn($file) => Str::endsWith($file, '.jar'))
            ->map(function ($file) {
                preg_match('/branchlock4-(\d+\.\d+\.\d+)/', $file, $matches);
                $version = $matches[1] ?? null;

                return [
                    'name' => $file,
                    'clean_name' => str_replace('-all', '', str_replace('branchlock4-', '', pathinfo($file, PATHINFO_FILENAME))),
                    'version' => $version,
                    'date' => Carbon::createFromTimestamp(Storage::disk('versions')->lastModified($file))->diffForHumans(),
                ];
            })
            ->sort(function ($a, $b) {
                return version_compare($b['version'], $a['version']);
            })
            ->values()
            ->toArray();
    }


    public static function formatConfig(?array $tasks = null, ?array $general = null, bool $fullAccess = false, bool $android = false): array
    {
        $availableTasks = Task::getAvailableTasks($fullAccess, $android);
        $availableGeneral = Task::getAvailableGeneralOptions($fullAccess, $android);

        $tasksFinal = self::processNestedTasks($tasks ?? [], $availableTasks);
        $generalFinal = self::filterAndCorrectValues($general ?? [], $availableGeneral);

        if (!isset($general['target_version'])) {
            unset($generalFinal['target_version']);
        }

        $config = [];
        if (!empty($generalFinal)) {
            $config['general'] = $generalFinal;
        }
        if (!empty($tasksFinal)) {
            $config['tasks'] = $tasksFinal;
        } else {
            $config['tasks'] = new StdClass();
        }

        return $config;
    }

    private static function processNestedTasks(array $tasks, array $availableTasks): array
    {
        foreach ($tasks as $task => &$taskData) {
            if (!array_key_exists($task, $availableTasks)) {
                unset($tasks[$task]);
                continue;
            }

            $taskData = self::filterAndCorrectValues($taskData, $availableTasks[$task]);
        }

        return $tasks;
    }

    private static function filterAndCorrectValues(?array $input, array $available): array
    {
        $settingsWithDefaultTrue = array_filter($available, function ($item) {
            return $item === true;
        });

        foreach ($input as $key => &$value) {
            if (in_array($key, ['exclude', 'include'])) {
                if (is_array($value)) {
                    $value = array_filter($value, function ($item) {
                        return is_string($item);
                    });
                } else {
                    unset($input[$key]);
                }
                continue;
            }

            if (!array_key_exists($key, $available)) {
                unset($input[$key]);
            } else {
                $value = self::correctValueType($available[$key], $value);
            }
        }

        // Check for settings with default value as true not present in user array and set them to false
        foreach ($settingsWithDefaultTrue as $key => $defaultValue) {
            if (!array_key_exists($key, $input)) {
                $input[$key] = false;
            }
        }

        return array_filter(array_merge($available, $input), function ($item) {
            return $item !== null;
        });
    }


    private static function correctValueType(mixed $originalValue, mixed $value): mixed
    {
        $originalValueType = gettype($originalValue);
        return match ($originalValueType) {
            'boolean' => is_bool($value) ? $value : $value === 'on',
            'integer' => is_numeric($value) ? (int)$value : ($value === 'auto' ? null : $originalValue),
            'double' => is_numeric($value) ? floatval(max('0.1', min('0.99', ($value / 10)))) : $originalValue,
            'string' => empty($value) ? $originalValue : Str::limit($value, 255, ''),
            'NULL' => is_numeric($value) ? (int)$value : null,
            default => $value,
        };
    }

    public static function removeRanges(&$config): void
    {
        if (isset($config['general'])) {
            $config['general'] = array_filter($config['general'], function ($key) {
                return !Str::contains($key, 'exclude') && !Str::contains($key, 'include');
            }, ARRAY_FILTER_USE_KEY);
        }

        if (isset($config['tasks'])) {
            $config['tasks'] = array_map(function ($task) {
                return array_filter($task, function ($key) {
                    return !Str::contains($key, 'exclude') && !Str::contains($key, 'include');
                }, ARRAY_FILTER_USE_KEY);
            }, $config['tasks']);
        }
    }

    public static function getDefaultTasks(bool $fullAccess = false, bool $android = false): array
    {
        return Task::getAvailableTasks($fullAccess, $android);
    }

    public static function makeConfigMinimalistic(&$config): void
    {
        $filterValues = function ($item) {
            return $item !== null && $item !== false && $item !== '';
        };

        if (isset($config['tasks'])) {
            foreach ($config['tasks'] as $task => &$taskData) {
                if (!($taskData['enabled'] ?? false)) {
                    unset($config['tasks'][$task]);
                    continue;
                }

                unset($config['tasks'][$task]['enabled']);

                $taskData = array_filter($taskData, $filterValues);

                if (empty($taskData)) {
                    $taskData = new \stdClass();
                }
            }
        }

        if (isset($config['general'])) {
            $config['general'] = array_filter($config['general'], $filterValues);
        }
    }

}
