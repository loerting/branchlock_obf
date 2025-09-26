<?php

namespace App\Http\Controllers;

use App\Models\Changelog;
use GrahamCampbell\Markdown\Facades\Markdown;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Cookie;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;

class DocsController extends Controller
{
    public function show()
    {
        return view('home.sites.docs', [
            'docs' => $this->parseDocs(),
        ]);
    }

    public function parseDocs(): array
    {
        $newestTimestamp = now()->timestamp;
        Cookie::queue(Cookie::forever('docs_last_seen', $newestTimestamp));

        if (Cache::has('parsed_docs')) {
            return Cache::get('parsed_docs');
        }

        $documentationContent = file(Storage::disk('files')->path('GENERAL_DOCUMENTATION.md'));

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
            $doc['content'] = Markdown::convertToHtml($doc['content']);
            $doc['subtitles'] = collect($doc['subtitles'])->map(function ($subtitle) {
                $subtitle['content'] = Markdown::convertToHtml($subtitle['content']);
                return $subtitle;
            });
            return $doc;
        })->toArray();

        Cache::put('parsed_docs', $documentation, now()->addHours(1));

        return $documentation;
    }

}
