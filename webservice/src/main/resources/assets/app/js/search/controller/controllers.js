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

    .controller('SearchResultController', ['$scope', '$log', '$window', 'dataStoreService', 'GvodService', function ($scope, $log, $window, dataStoreService, GvodService) {


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

        // Update structure with necessary data.
        function _restructureData(data) {
            
            var _defaultDesc = "Download";

            for (var i = 0; i < data.length; i++) {
                data[i]["linkDesc"] = _defaultDesc;
            }
            
            return data;
        }


        function initScope(scope) {

            // Create an object to hold results.
            scope.results = null;
            
            // Register a watch event, to capture updated results.
            scope.$watch(dataStoreService.fetchSearchResults, function(data){
                scope.results = _restructureData(data);
            })
            
            
        }


        $scope.play = function (entry) {

            var filename = entry["fileName"];
            var linkDesc = entry["linkDesc"];
            var url = parseInt(entry["url"]);

            var json = {
                name: filename,
                overlayId: url
            };

            if (linkDesc === "Play") {

                GvodService.play(json)
                    
                    .success(function (data, status, headers, config) {
                        // Create the video in particular format.
                        var url = "http://localhost:".concat(data).concat("/").concat(filename).concat("/").concat(filename);
                        $window.open(url);
                    })
                    .error(function (data, status, headers, config) {
                        // Display User with the error.
                        $log.info("Unable to fetch port information.");
                    })
                
            }

            else if (linkDesc === "Download") {

//                entry['linkDesc'] = "Play";
                $log.info(json);

                GvodService.download(json)

                    .success(function (data, status, headers, config) {

                        $log.info("Gvod initialized for the service.");
                        
                        if(data){
                            entry['linkDesc'] = "Play";    
                        }
                        else{
                            $log.info("No able to play the video.");
                        }

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
    }]);
