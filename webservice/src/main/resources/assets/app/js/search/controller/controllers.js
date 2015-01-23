/**
 * Created by babbarshaer on 04-07-2014.
 */
'use strict';
/** Represents the controller definitions used by the application.**/

angular.module('searchModule.controllers', [
    'searchModule.services',
    'core-services'
    ])

    .controller('SearchInputController', ['$log', '$location' , 'dataStoreService' , 'searchService', '$scope', function ($log, $location, dataStoreService, searchService, $scope) {

        $scope.input = {
            category: 'Video'
        };
        $scope.goSearchValue = function () {
            // Performing search.
            searchService.performSearch($scope.input);
//            $log.info($location.path());
            $location.path('/results');
//            $log.info($location.path());

        };

    }])

    .controller('SearchResultController', ['$scope', '$log', '$location','$rootScope', 'dataStoreService', 'GvodService', function ($scope, $log, $location, $rootScope, dataStoreService, GvodService) {


        function _getDummyResults() {

            var results = [
                {
                    fileName: "Abhi",
                    description: " This is dummy description",
                    url: "0"
                }
            ];

            return results;
        }

        
        $scope.testMe = function(){

            var linkInfo = "/play/".concat("flash.mp4").concat("/").concat("0").concat("/").concat("58026");
            $location.path(linkInfo);
        };
        
        // Update structure with necessary data.
        function _restructureData(data) {

            if(data != null){
                
                var _defaultDesc = "Download";

                for (var i = 0; i < data.length; i++) {
                    data[i]["linkDesc"] = _defaultDesc;
                }
            }
            
            return data;
        }


        function initScope(scope) {

            // Create an object to hold results.
            scope.results = null;
            
            $log.info("Init Scope Called ... ");
            
            // Register a watch event, to capture updated results.
            scope.$watch(dataStoreService.fetchSearchResults, function(data){
                scope.results = _restructureData(data);
            })
            
            
        }


        $scope.play = function (entry) {

            var filename = entry["fileName"];
            var linkDesc = entry["linkDesc"];
            var url = entry["url"];

            var json = {
                name: filename,
                overlayId: parseInt(url)
            };

            if (linkDesc === "Play") {

                GvodService.play(json)
                    
                    .success(function (data, status, headers, config) {
                        
                        // ==== Create Video Link. ====
                        
                        var linkInfo = "/play/".concat(filename).concat("/").concat(url).concat("/").concat(data);
                        $location.path(linkInfo);
                        
                    })
                    .error(function (data, status, headers, config) {
                        // Display User with the error.
                        $log.info("Unable to fetch port information.");
                    })
                
            }

            else if (linkDesc === "Download") {

                $log.info(json);

                GvodService.download(json)

                    .success(function (data, status, headers, config) {

                        $log.info("Gvod initialized for the service.");
                        
                        if(data){
                            $log.info(json.name.concat("->").concat(" initialized."));
                        }
                        else{
                            $log.info(json.name.concat("->").concat(" already initialized."));
                        }

                        entry['linkDesc'] = "Play";
                    })
                    .error(function (data, status, headers, config) {
                        // Display User with the error.
                        $log.info(" Issues in GVOD initialization. ");
                    })
                
            }

            else {
                $log.info(" Undefined Option. This shouldn't happen. ");
            }
        };

        initScope($scope);
    }])

    .controller("VideoController",["$log","$scope",'$routeParams','GvodService',function($log,$scope,$routeParams,GvodService){

        // Route Params -> '/play/:name/:uri/:port'
        
        // Step 1: Construct the link information from it.
        // Step 2: Open the video player.
        // Step 3: Start buffering the video.
        
        function _initScope(scope){
            
            var _name = $routeParams.name;
            var _uri = $routeParams.uri;
            var _port = $routeParams.port;
            
            var _ip = "http://localhost:";
            
            var _videoResource = {
                name : _name,
                overlayId : parseInt(_uri)
            };
            
            scope.source = _ip.concat(_port).concat("/").concat(_name).concat("/").concat(_name);
            $log.info(scope.source);
            
            
            if(scope.source != null){

                var player = videojs('entry_video', { /* Options */ }, function() {

                    $log.info('Video Player Initialized.');

                    // == Properties of Player. ==
                    this.preload(true);
                    this.controls(true);
                    this.src(scope.source);
                    this.dimensions(800,400);

                    // == Event Listeners. ==
                    this.on('ended', function() {
                        console.log('File Finished');
                    });
                });
            }
            
            
            // Register a call when scope destroyed.
            scope.$on('$destroy', function(){

                // Dispose the player. WARNING : Without this, the player doesn't gets initialized again in the system.
                
                player.dispose();
                
                $log.info('Scope Getting Destroyed.');
                GvodService.stop(_videoResource);
                
            });
            
            

        }

        _initScope($scope);

    }]);
