/**
 * Created by babbarshaer on 04-07-2014.
 */
'use strict';
/** Represents the controller definitions used by the application.**/

angular.module('uploader.controller',['uploader.service'])


    .controller('UploaderInitiationController',['dataStoreService','$log', '$location' ,'fetchFileInfo', '$scope', function(dataStoreService,$log, $location, fetchFileInfo , $scope){

        // Categories to be displayed.
        $scope.categories=[

            {name:"Video",
             value:"Video"},

            {name:"Books",
             value:"Books"},

            {name:"Music",
             value:"Music"}

        ];

        $scope.configuration={};

        $scope.startEntryAddition = function(){

            //Store the data in the data store service.
            dataStoreService.storeConfigurationInformation($scope.configuration);

            //log to the console.
            $log.info(" Going to start the fetching of files. ");

            // trigger fetching of the updated files.
            fetchFileInfo.fetchFreshAddedFiles();

            //update the location.
            $location.path('/uploader/addEntryInfo');
        };
    }])

    .controller('EntryAdditionController',['dataStoreService','$scope','addIndexEntryService', function( dataStoreService, $scope, addIndexEntryService){

        // Double Brackets not used in the method call,
        // $scope.results = dataStoreService.fetchSearchResults;

        // Append the jQuery animation to be used.
        $scope.$on('$viewContentLoaded',addEntryInfoAnimation);

        //Load the configuration from the service.
        $scope.configuration = dataStoreService.fetchConfigurationInformation();


        //Fetch the files information also  and be careful to not call the method immediately.
        //TODO: Research on this aspect of javascript.
        $scope.filesInformation = dataStoreService.fetchFilesInformation;

        // Create an object to hold the data in the system.
        $scope.indexEntryData ={};

        // Call the service to add the index entry.
        $scope.submitIndexEntry = function(){
            addIndexEntryService.addIndexEntry($scope.indexEntryData);
            // Restart the fetching of the updated entries in the same here.
        }


    }]);
