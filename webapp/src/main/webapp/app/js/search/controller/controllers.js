/**
 * Created by babbarshaer on 04-07-2014.
 */
'use strict';
/** Represents the controller definitions used by the application.**/

angular.module('searchModule.controllers',['searchModule.services'])


    .controller('SearchInputController',['$log', '$location' , 'dataStoreService' ,'searchService', '$scope', function($log, $location, dataStoreService , searchService , $scope){

        $scope.input={};
        $scope.goSearchValue = function(){
            // Performing search.
//            searchService.performSearch($scope.input);
//            $log.info($location.path());
            $location.path('/results');
//            $log.info($location.path());

        };

    }])

    .controller('SearchResultController',['dataStoreService','$scope', function(dataStoreService,$scope){

        // Double Brackets not used in the method call,
        // Shifted to the html page in which controller is called, to allow for the dynamic updation of the values.
        $scope.results = dataStoreService.fetchSearchResults;
    }]);
