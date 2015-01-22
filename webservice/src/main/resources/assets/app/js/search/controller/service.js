/**
 * This is the REST service which will fetch the search from the exposed service.
 **/

'use strict';

angular.module('searchModule.services', [])

    .service('dataStoreService',['$log',function($log){

        var searchParameters={};
        var result;
        var errorResult;

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
            }
        };
    }])

    .service('searchService', ['dataStoreService','$log', '$http', function ( dataStoreService, $log, $http) {

        // Create a search parameter.
        var searchParameters;
        return {

            performSearch: function (search) {

                // Store the Data in a datastore and perform the search.
                $log.info('In searchService:performSearch()');
                dataStoreService.storeSearchData(search);

                $http({
                    method: 'PUT',
                    url: 'http://localhost:8080/search',
                    headers: {'Content-Type': 'application/json'},
                    data: search
                })
                    .success(function (data, status, headers, config) {
                        $log.info('Data Received.');
//                        $log.info(data.url);
                        dataStoreService.storeSearchResults(data);
                        return data;
                    })
                    .error(function (data, status, headers, config) {
                        $log.info(" Search Service Error. Please Check ..");
//                        dataStoreService.storeErrorSearchResults(data);
//                        return data;
                    })
            },

            addIndexEntry: function () {

                //TODO: Update the data present in the function.
                $http({
                    method: 'PUT',
                    url: 'http://193.10.66.39:8080/add',
                    headers: {'Content-Type': 'application/json'},
                    data: search
                })
                    .success(function (data, status, headers, config) {
                        // Some response needs to be there for this.
                        $log.info('Data Persisted in the system.');
                    })
                    .error(function (data, status, headers, config) {
                        // Same Here.
                        $log.info('Data Retrieved from it.');
                    })
            }
        };

    }]);