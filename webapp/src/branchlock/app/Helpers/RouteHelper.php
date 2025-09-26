<?php

namespace App\Helpers;


use Illuminate\Support\Facades\Route;
use Illuminate\Support\Str;

class RouteHelper
{

    public static function getRoutesByStarting($start = ''): array
    {
        $list = Route::getRoutes()->getRoutesByName();
        if (empty($start)) {
            return $list;
        }

        $routes = [];
        foreach ($list as $name => $route) {
            if (\Illuminate\Support\Str::startsWith($name, $start)) {
                $routes[$name] = $route;
            }
        }

        return $routes;
    }

    // get routes by ControllerClass
    public static function getRoutesByController($controllerClass): array
    {
        $list = Route::getRoutes()->getRoutesByName();
        $routes = [];
        foreach ($list as $name => $route) {
            if (Str::startsWith($route->action['controller'], $controllerClass)) {
                $routes[$name] = $route;
            }
        }

        return $routes;
    }

}
