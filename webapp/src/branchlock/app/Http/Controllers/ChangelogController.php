<?php

namespace App\Http\Controllers;

use App\Models\Changelog;
use Carbon\Carbon;
use GrahamCampbell\Markdown\Facades\Markdown;
use Illuminate\Support\Facades\Cookie;

class ChangelogController extends Controller
{
    public function show()
    {
        $changelog = Changelog::all()->sortByDesc('timestamp')->map(function ($item) {
            $item['date1'] = Carbon::createFromTimestamp($item['timestamp'])->diffForHumans();
            $item['date2'] = Carbon::createFromTimestamp($item['timestamp'])->toFormattedDateString();

            if (!empty($item['content'])) {
                if (!str_starts_with($item['content'], '<')) {
                    $item['content'] = Markdown::convertToHtml($item['content']);
                }
            }

            return $item;
        });

        // get changelog entry with running true and remove it from $changelog so it can be displayed separately
        $running = $changelog->where('running', true)->first();
        $changelog = $changelog->where('running', false);

        // get changelog entries with published false and remove them from $changelog so they can be displayed separately
        $unpublished = $changelog->where('published', false);
        $changelog = $changelog->where('published', true);

        // get timestamp of newest changelog entry
        $newestTimestamp = Changelog::getRunningVersion()->timestamp;
        Cookie::queue(Cookie::forever('changelog', $newestTimestamp));

        return view('home.sites.changelog', [
            'running' => $running,
            'unpublished' => $unpublished,
            'changelog' => $changelog,
        ]);
    }
}
