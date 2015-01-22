/**
 * This services module is responsible for adding the index entries in the database.
 **/

'use strict';

angular.module('uploader.service', [])

    .service('dataStoreService',['$log',function($log){

        // Abstract it in the common service.
        var searchParameters={};
        var result;
        var errorResult;
        var configurationInformation ={};
        var files;

        return{

            storeSearchData: function(data){

                $log.info('in dataStoreService:storeSearchData()');
                searchParameters = data;
            },
            fetchData: function(){
                $log.info('in dataStoreService:fetchData()');
                return searchParameters;
            },

            storeSearchResults: function(searchResults){
                result = searchResults;
            },

            storeErrorSearchResults: function(searchResults){
                errorResult = searchResults;
            },

            fetchSearchResults: function(){
                return result;
            },

            storeConfigurationInformation: function(data){

                $log.info("Storing the configuration information");
                configurationInformation = data;
            },

            fetchConfigurationInformation: function(){
                $log.info("Fetching the configuration Information");
                return configurationInformation;
            },


            storeFilesInformation: function(data){
                $log.info("Received Files present in directory");
                files = data;
            },

            fetchFilesInformation: function(){
                return files;
            }

        };
    }])

    .service('fetchFileInfo',['dataStoreService','$http','$log',function(dataStoreService,$http,$log){

        return{
            fetchFreshAddedFiles : function(){
                $log.info(" File File Information From Gvod Initiated .... ");

                // Create an http get request which will be sent to the webservice along with the params.

//                $http({
//
//                    method: 'GET',
//                    url: 'http://localhost:8080/fetchFiles',
//                    headers:{'Content-Type':'application/json'},
//                    params: {
//                        // Fetch the category entered by the user and add it to the params.
//                        category: dataStoreService.fetchConfigurationInformation().category
//                    }
//                })
//                    .success(function(data,status,headers,config){
//                        $log.info("Success Received Some Data");
//                        dataStoreService.storeFilesInformation(data);
//                    })
//
//                    .error(function(data,status,headers,config){
//                        $log.info("Received Some Error ... ");
//                    })

            }
        }
    }])


    .service('addIndexEntryService',['$http','$log',function($http,$log){

        return{

            addIndexEntry : function(entryData){
                $log.info("Index Entry Initiated.");

                // Call the REST API To add the index entry in the system.
                return $http({
                    method: 'PUT',
                    url: 'http://localhost:8080/add',
                    headers: {'Content-Type': 'application/json'},
                    data: entryData
                });
            }
        }

    }]);