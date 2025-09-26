<?php

namespace App\Http\Controllers;

use App\Models\Changelog;
use App\Models\Task;
use App\Models\User;
use GrahamCampbell\Markdown\Facades\Markdown;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Storage;

class IndexController extends Controller
{
    public function index()
    {
        $demoTasks = Task::getInteractiveDemoTasks();
        $categoryOrder = Task::getCategoryOrder();
        $demoTasks = $demoTasks->sortBy(function ($task) use ($categoryOrder) {
            return array_search($task->category, $categoryOrder);
        })->chunk(2);

        $version = Changelog::getRunningVersion();
        $bin = str_repeat('0', count(Task::getInteractiveDemoTasks()));

        $unobfDemoCode = "{$bin}-{$version['version']}";
        $uploadsDisk = Storage::disk('demo');

        if (!$uploadsDisk->exists($unobfDemoCode)) {
            $controller = new InteractiveDemoController();
            $controller->generateFile($unobfDemoCode, []);
        }

        $unobfDemoCode = $uploadsDisk->get($unobfDemoCode);

        $allTasks = Task::all();
        $allTasks = $allTasks->sortByDesc(function ($task) {
            return strlen($task->description);
        });

        return view('home.sites.index', [
            'demoCode' => $unobfDemoCode,
            'tasks' => $allTasks,
            'demoTasksGroups' => $demoTasks,
            'faqs' => $this->parseFAQ(),
        ]);
    }

    public function parseFAQ(): array
    {
        if (Cache::has('parsed_faqs')) {
            return Cache::get('parsed_faqs');
        }

        $faqContent = file(Storage::disk('files')->path('FAQ.md'));
        $faqContent = collect($faqContent);

        $faqs = [];
        $currentFaq = null;

        $faqContent->each(function ($line) use (&$faqs, &$currentFaq) {
            $matches = [];
            if (preg_match('/^##\s(.+)/', $line, $matches)) {
                if ($currentFaq) {
                    $faqs[] = $currentFaq;
                }
                $currentFaq = ['question' => trim($matches[1]), 'answer' => ''];
            } elseif ($currentFaq) {
                $currentFaq['answer'] .= $line;
            }
        });

        if ($currentFaq) {
            $faqs[] = $currentFaq;
        }

        $faqs = collect($faqs)->map(function ($faq) {
            $faq['answer'] = Markdown::convertToHtml($faq['answer']);
            return $faq;
        })->values()->all();

        Cache::put('parsed_faqs', $faqs, now()->addHours(1));

        return $faqs;
    }

}
