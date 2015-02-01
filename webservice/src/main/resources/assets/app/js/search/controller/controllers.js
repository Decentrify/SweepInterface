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

    .controller('SearchResultController', ['$scope', '$log', '$location', '$rootScope', 'dataStoreService', 'GvodService', function ($scope, $log, $location, $rootScope, dataStoreService, GvodService) {

        // Update structure with necessary data.
        function _restructureData(data) {

            if (data != null) {

                var _defaultDesc = "Play";

                for (var i = 0; i < data.length; i++) {
                    data[i]["linkDesc"] = _defaultDesc;
                }
            }

            return data;
        }


        function initScope(scope) {

            // Create an object to hold results.
            scope.results = null;
            scope.player = null;
            scope.currentVideoResource = null;
            scope.previousVideoResource = null;

            $log.info("Init Scope Called ... ");

            // Register a watch event, to capture updated results.
            scope.$watch(dataStoreService.fetchSearchResults, function (data) {

                scope.results = _restructureData(data);

                if (scope.results && scope.results.length > 0) {

                    scope.player = videojs("video_player", {}, function () {
                    });
                    scope.player.dimensions("100%", "100%");
                    scope.player.controls(true);

                }

            });


            scope.$on('$destroy', function () {

                // Dispose the player. WARNING : Without this, the player doesn't gets initialized again in the system.

                $log.info('Scope Getting Destroyed.');
                if (scope.player != null) {

                    $log.info('Disposing off the player.');
                    scope.player.dispose();
                }
                
                
                if (scope.currentVideoResource != null) {
                    $log.info('Sending Call to Gvod to stop buffering video resource.');
                    GvodService.stop(scope.currentVideoResource);
                }
            });

        }


        $scope.play = function (entry) {

            var filename = entry["fileName"];
            var url = entry["url"];

            var json = {
                name: filename,
                overlayId: parseInt(url)
            };

            
            // STEP 1: Pause the current player as the play action should be immediately visible.
            if($scope.player != null){
                $scope.player.pause();
            }
            
            // STEP 2: Now stop the current playing video.
            if($scope.currentVideoResource != null){
                
                $log.info("Sending stop to gvod service for: " + angular.toJson($scope.currentVideoResource));
                GvodService.stop($scope.currentVideoResource)
                    
                    // STEP 3: On success, play the video fetched. ( Not sure the response in addition to fast clicks. )
                    .success(function(){
                        $log.info("Stop successful for video: " + angular.toJson($scope.currentVideoResource));
                        _callPlayRest(json);
                    })
            }
            
            else{
                _callPlayRest(json);
            }
        };
        
        
        function _callPlayRest(parameter){

            var filename = parameter["name"];
            var _ip = "http://localhost:";

            $log.info(" Going to send play to service for video: " + angular.toJson(parameter));
            GvodService.play(parameter)
                
                // The response data contains port only, but it should contain more meta-data used to differentiate the calls.
                .success(function(data){

                    $log.info("Successfully received port for video: " + angular.toJson(parameter));
                    // Update the current video resource.
                    $scope.currentVideoResource = parameter;

                    // STEP 4: Construct video address and play video.
                    var src = _ip.concat(data).concat("/").concat(filename).concat("/").concat(filename);
                    
                    if ($scope.player != null) {
                        
                        $scope.player.src(src);
                        $scope.player.load();
                        $scope.player.play();
                    }

                    else {
                        $log.warn(" UI Video Player : Not Initialized.");
                    }
                })
                
                // Issue with the video.
                .error(function(data){
                    $log.warn(" Play REST Failed -> Issue with gvod-webservice. Video Info: " + angular.toJson(parameter));
                })
            
        }

        initScope($scope);
    }])

    .controller("VideoController", ["$log", "$scope", '$routeParams', 'GvodService', function ($log, $scope, $routeParams, GvodService) {

        // Route Params -> '/play/:name/:uri/:port'

        // Step 1: Construct the link information from it.
        // Step 2: Open the video player.
        // Step 3: Start buffering the video.

        function _initScope(scope) {

            var _name = $routeParams.name;
            var _uri = $routeParams.uri;
            var _port = $routeParams.port;

            var _ip = "http://localhost:";

            var _videoResource = {
                name: _name,
                overlayId: parseInt(_uri)
            };

            scope.source = _ip.concat(_port).concat("/").concat(_name).concat("/").concat(_name);
            $log.info(scope.source);


            if (scope.source != null) {

                var player = videojs('entry_video', { /* Options */ }, function () {

                    $log.info('Video Player Initialized.');

                    // == Properties of Player. ==
                    this.preload(true);
                    this.controls(true);
                    this.src(scope.source);
                    this.dimensions(800, 400);

                    // == Event Listeners. ==
                    this.on('ended', function () {
                        console.log('File Finished');
                    });
                });
            }


            // Register a call when scope destroyed.
            scope.$on('$destroy', function () {

                // Dispose the player. WARNING : Without this, the player doesn't gets initialized again in the system.

                player.dispose();

                $log.info('Scope Getting Destroyed.');
                GvodService.stop(_videoResource);

            });


        }

        _initScope($scope);

    }]);
