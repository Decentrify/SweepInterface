/**
 * Created by babbarshaer on 04-07-2014.
 */
'use strict';
/** Represents the controller definitions used by the application.**/

angular.module('uploader.controller',[
    'uploader.service',
    'core-services'
    ])
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

    .controller('EntryAdditionController',['$log','dataStoreService','$scope','addIndexEntryService','GvodService', function($log, dataStoreService, $scope, addIndexEntryService,GvodService){

        // Double Brackets not used in the method call,
        // $scope.results = dataStoreService.fetchSearchResults;

        function _reformatData(data){
            
            var list = [];
            
            var isCheckSet = false;
            
            for(var key in data){
               
                var obj = {};    
                obj["name"] = key;
                obj["status"] = data[key];

                if(!isCheckSet && obj["status"] === "NONE"){
                    
                    // Set the checked flag.
                    obj["isChecked"] = true;
                    isCheckSet = true;
                    
                    // Update the initial entry in the table.
                    $scope.indexEntryData["fileName"] = obj["name"];
                }
                else{
                    obj["isChecked"] = false;
                }

                list.push(obj);
            }

            return list;
        }

        function initScope(scope){

            // Append the jQuery animation to be used.
//            scope.$on('$viewContentLoaded',addEntryInfoAnimation);

            //Load the configuration from the service.
            scope.configuration = dataStoreService.fetchConfigurationInformation();

            // Create an object to hold the data in the system.
            scope.indexEntryData ={
                
                fileName: 'none',
                language:'English',
                fileSize: 1,
                category: 'Video'
            };

            GvodService.fetchFiles()
                
                .success(function(data){
                    $log.info(data);
                    scope.files = _reformatData(data);
                    $log.info(scope.files);
                })
                
                .error(function(data){
                    $log.info("Unable to fetch files from the library. ");
                })
        }

        
        function _authenticateValues(data){

            if(data["fileName"] == null || data["fileName"] === "none" ){
                return false;
            }
            else if(data["url"] == null){
                return false;
            }

            else if(data["description"] == null){
                return false;
            }
            
            return true;
        }
        
        
        // Call the service to add the index entry.
        $scope.submitIndexEntry = function(){

            $log.info(" Submit Index Entry Call Issued . Checking for in consistencies ....");
            
            if(_authenticateValues($scope.indexEntryData)){

                var lastObjCalled = $scope.indexEntryData;
                
                
                addIndexEntryService.addIndexEntry($scope.indexEntryData)
                    
                    .success(function (data, status, headers, config) {
                        
                        // Some response needs to be there for this.
                        $log.info(' Index Entry Added in the System. ');

                        var obj = {
                            name : lastObjCalled["fileName"],
                            overlayId : lastObjCalled["url"]
                        };
                        
                        GvodService.pendingUpload(obj)
                            
                            .success(function(data){
                                
                                if(data){
                                    $log.info(" Successful Pending Upload ... ");
                                    
                                    GvodService.upload(obj)
                                        
                                        .success(function(data){
                                            
                                            if(data){
                                                
                                                $log.info(" Successful Upload ... ");
                                                
                                                GvodService.fetchFiles()

                                                    .success(function(data){
                                                        
                                                        $log.info(data);
                                                        $scope.files = _reformatData(data);
                                                        $log.info($scope.files);
                                                        
                                                    })
                                                    .error(function(data){
                                                        $log.info("Unable to fetch files from the library. ");
                                                    })
                                                
                                            }
                                        })
                                }

                            })
                            
                            .error(function(data){
                                $log.info(" Pending upload failed ....");
                            })
                        
                    })
                    
                    .error(function (data, status, headers, config) {
                        // Same Here.
                        $log.info('Index Entry Addition Failed. ');
                    })
                
                
                $log.info($scope.indexEntryData);
                
            }

            // Restart the fetching of the updated entries in the same here.

            //Clean the Entry Addition Table.
            $scope.refreshAddIndexEntryTable();
        };

        // Simply reset the data in the system.
        $scope.refreshAddIndexEntryTable = function(){
            $scope.indexEntryData ={
                
                fileName: 'none',
                language:'English',
                fileSize:1,
                category: 'Video'
            };
        };

        initScope($scope);
    }]);
