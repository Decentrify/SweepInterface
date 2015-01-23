/**
 * Created by babbarshaer on 04-07-2014.
 */
/** Main module of the search gui which will include all other modules.**/
'use strict';

angular.module('searchModule',['ngRoute','searchModule.controllers','searchModule.services'])
    .config(['$locationProvider','$routeProvider',function($locationProvider,$routeProvider){

            $routeProvider.when('/input',{templateUrl: 'partials/search/SearchInputPartial.html', controller: 'SearchInputController'});
            $routeProvider.when('/results',{templateUrl: 'partials/search/SearchResultPartial.html', controller: 'SearchResultController'});
            $routeProvider.when('/play/:name/:uri/:port',{templateUrl:'partials/search/VideoPlayer.html',controller: 'VideoController'});
            $routeProvider.otherwise({redirectTo: '/input'});

    }]);