<?php

namespace App\Http\Controllers;

use App\Branchlock;
use App\BranchlockRunType;
use App\Helpers\CustomHelper;
use App\Models\Project;
use App\Models\Task;
use App\Models\User;
use Carbon\Carbon;
use GrahamCampbell\Markdown\Facades\Markdown;
use Illuminate\Foundation\Application;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;
use Illuminate\Validation\Rule;
use Illuminate\View\Factory;
use Illuminate\View\View;

class ProjectController extends Controller
{

    public function show(): View|Application|Factory
    {
        // get projects by user id
        $projects = Project::where('user_id', Auth::id())
            ->get()
            ->sortByDesc('created_at')
            ->map(function ($item) {
                $item = $this->processProjectItem($item);
                return $item;
            });

        $allTasks = Task::all();

        $categoryOrder = Task::getCategoryOrder();
        $groupedTasks = $allTasks->groupBy('category')->sortBy(function ($group) use ($categoryOrder) {
            return array_search($group->first()->category, $categoryOrder);
        })->map(function ($tasks) {
            return $tasks->sortByDesc(function ($task) {
                return strlen($task->frontend_name);
            });
        });

        $desktopTasks = $groupedTasks->map(function ($tasks) {
            return $tasks->filter(function ($task) {
                return $task->desktop;
            });
        });

        $androidTasks = $groupedTasks->map(function ($tasks) {
            return $tasks->filter(function ($task) {
                return $task->android;
            });
        });


        $generalSettings = config('branchlock-config.general');

        // get ranges docs
        $rangesDocs = $this->parseRangesDocs();


        // FREE WEEK
        if(auth()->user()['plan'] === 'free') {
            $user = auth()->user();
            $user->plan = 'solo';
            $user->purchase_date = date('Y-m-d H:i:s');
            $user->purchase_end_date = Carbon::parse($user->purchase_date)->addDays(7)->format('Y-m-d H:i:s');
            $user->save();

            session()->flash('success', 'You have been granted a free week of premium features. Enjoy!');
        }


        return view('app.sites.projects.index', [
            'projects' => $projects,
            'tasks' => $allTasks,
            'desktopTasks' => $desktopTasks,
            'androidTasks' => $androidTasks,
            'generalSettings' => $generalSettings,
            'rangesDocs' => $rangesDocs,
            'blVersions' => Branchlock::getVersions(),
        ]);
    }

    public function add(Request $request): \Illuminate\Http\RedirectResponse
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required|string|max:50',
            'project_id' => ['required', 'string', 'max:30', 'regex:/^[a-zA-Z0-9-]+$/',
                Rule::unique('projects')->where(fn($query) => $query->where('user_id', Auth::id()))],
        ]);

        if ($validator->fails()) {
            return redirect()->route('app.projects')->withErrors($validator)->withInput();
        }

        $validated = $validator->safe()->only(['name', 'project_id']);

        if (auth()->user()->maxProjectsReached()) {
            return redirect()->route('app.projects');
        }


        $project = new Project(['android' => $request->filled('android')]);
        $project->user_id = Auth::id();
        $project->name = $validated['name'];
        $project->project_id = strtolower($validated['project_id']);
        $project->android = $request->filled('android');

        $project->save();

        return redirect()->route('app.projects');
    }

    public function delete($project_id): \Illuminate\Http\RedirectResponse
    {
        $project = Project::findOrFail($project_id);

        $project->deleteAll();

        return redirect()->route('app.projects');
    }

    public function rename(Request $request, $project_id): \Illuminate\Http\RedirectResponse
    {
        $validator = Validator::make($request->all(), [
            'new_name' => 'required|string|max:50',
        ]);

        if ($validator->fails()) {
            return redirect()->route('app.projects')->withErrors($validator)->withInput();
        }

        $validated = $validator->safe()->only(['new_name']);

        $project = Project::findOrFail($project_id);

        $project->name = $validated['new_name'];
        $project->save();

        return redirect()->route('app.projects');
    }

    public function resetTasks($project_id): JsonResponse
    {
        $project = Project::findOrFail($project_id);

        $tasks = Branchlock::getDefaultTasks(auth()->user()->getPlan()['premium_tasks'], $project->android);

        return response()->json([
            'error' => false,
            'status' => 'success',
            'tasks' => $tasks,
        ]);
    }

    public function save(Request $request, $project_id): JsonResponse
    {
        $validator = Validator::make($request->all(), [
            'tasks' => 'nullable|array|max:255',
            'tasks.*' => 'nullable|max:255',

            'general' => 'nullable|array|max:255',
            'general.*' => 'nullable|max:255',

            'server' => 'nullable|array|max:255',
            'server.*' => 'nullable|max:255',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'error' => true,
                'status' => 'error',
                'message' => 'Invalid request',
            ], 400);
        }

        $validated = $validator->safe()->only(['tasks', 'general', 'server'], []);

        $project = Project::findOrFail($project_id);

        $requestData = $validated;

        $config = Branchlock::formatConfig($requestData['tasks'], $requestData['general'], auth()->user()->getPlan()['premium_tasks'], $project->android);

        $server = $requestData['server'] ?? [];
        $server = array_map(function ($item) {
            // Check if the item is a non-empty string or convert other types to boolean
            if (is_string($item)) {
                return $item === 'on' ? true : $item;
            } else {
                return !empty($item) && $item !== 'false' && $item !== '0';
            }
        }, $server);

        $finalConfig = array_merge($config, $server);

        $project->config = $finalConfig;
        $project->save();

        if ($requestData['server']['ranges_disabled'] ?? false) {
            Branchlock::removeRanges($config);
        }

        if ($requestData['server']['config_full'] ?? false) {
            $bl = new Branchlock($project->android ? BranchlockRunType::ANDROID_OBFUSCATION : BranchlockRunType::WEB_OBFUSCATION);
            $bl->setProjectId($project->id);
            if ($project->jar) {
                $bl->setInput(Storage::disk('uploads')->path($project->jar));
            }
            if (!empty($project->libs)) {
                $bl->setLibraries($project->libs);
            }
            $bl->setDemo(auth()->user()['role'] === User::ROLE_ADMIN ? $requestData['server']['demo_mode'] ?? false : auth()->user()->getPlan()['demo_mode']);
            $bl->setDebug(auth()->user()['role'] === User::ROLE_ADMIN ? $requestData['server']['debug_mode'] ?? false : false);
            $bl->setConfig($requestData['tasks'], $requestData['general']);
            $config = $bl->buildConfig();

            //unset input, output, libraries
            unset($config['input']);
            unset($config['output']);
            unset($config['libraries']);
        } else {
            Branchlock::makeConfigMinimalistic($config);

            if (empty($config['tasks'])) {
                unset($config['tasks']);
            }
        }

        return response()->json([
            'error' => false,
            'status' => 'success',
            'message' => 'Project saved',
            //'config' => auth()->user()['role'] === User::ROLE_ADMIN ? json_encode($config) : null,
            'config' => json_encode($config),
        ]);
    }

    private function processProjectItem($item)
    {
        $item['date1'] = Carbon::create($item['created_at'])->diffForHumans();
        $item['date2'] = Carbon::create($item['created_at'])->toFormattedDateString();
        $item['date_updated'] = Carbon::create($item['updated_at'])->diffForHumans();

        if (isset($item['jar']) && Storage::disk('uploads')->exists($item['jar'])) {
            $item = $this->processJarInformation($item);
        } else {
            $project = Project::findOrFail($item['id']);
            //$project->jar = null;
            $project->jar_original_name = null;
            $project->jar_size = null;
            $project->save();

            $item['jar'] = null;
        }

        if (isset($item['libs'])) {
            $libs = [];
            foreach ($item['libs'] as $lib) {
                if (Storage::disk('uploads')->exists($lib['file'])) {
                    $lib['name'] = strlen($lib['name']) > 64 ? substr($lib['name'], 0, 64) . '...' : $lib['name'];
                    $lib['size'] = CustomHelper::formatBytes($lib['size']);
                    $libs[] = $lib;
                } else {
                    $project = Project::findOrFail($item['id']);
                    $project->pull('libs', ['file' => $lib['file']]);
                }
            }
            $item['libs'] = $libs;
        }

        return $item;
    }

    private function processJarInformation($item)
    {
        $item['jar_original_name'] = strlen($item['jar_original_name']) > 64 ? substr($item['jar_original_name'], 0, 64) . '...' : $item['jar_original_name'];
        $item['jar_size'] = CustomHelper::formatBytes($item['jar_size']);
        $item['jar_uploaded_at'] = Carbon::create($item['jar_uploaded_at'])->diffForHumans();
        return $item;
    }

    private function parseRangesDocs(): array
    {
        if (Cache::has('parsed_ranges_docs')) {
            return Cache::get('parsed_ranges_docs');
        }

        $documentationContent = file(Storage::disk('files')->path('EXCLUSION_INCLUSION_SYNTAX.md'));

        $documentation = [];

        $currentTitle = null;
        $currentSubtitle = null;
        foreach ($documentationContent as $line) {
            if (preg_match('/^#\s(.+)/', $line, $matches)) {
                // Title
                $currentTitle = Str::slug($matches[1]);
                $documentation[$currentTitle] = [
                    'title' => trim($matches[1]),
                    'content' => '',
                    'subtitles' => [],
                ];

                $currentSubtitle = null;

            } elseif (preg_match('/^##\s*(.+)/', $line, $matches)) {
                // Subtitle
                $currentSubtitle = Str::slug($matches[1]);
                $documentation[$currentTitle]['subtitles'][$currentSubtitle] = [
                    'title' => trim($matches[1]),
                    'content' => '',
                ];

            } else {
                // Content
                if ($currentSubtitle !== null) {
                    $documentation[$currentTitle]['subtitles'][$currentSubtitle]['content'] .= $line;
                } else {
                    $documentation[$currentTitle]['content'] .= $line;
                }
            }
        }

        $documentation = collect($documentation)->map(function ($doc) {
            $doc['subtitles'] = collect($doc['subtitles'])->map(function ($subtitle) {
                $subtitle['content'] = Markdown::convertToHtml($subtitle['content']);
                return $subtitle;
            });
            return $doc;
        })->toArray();

        Cache::put('parsed_ranges_docs', $documentation, now()->addHours(1));

        return $documentation;
    }
}
