/**
 * Created by babbarshaer on 04-07-2014.
 */
/** Main module of the search gui which will include all other modules.**/


'use strict';

angular.module('uploaderModule',['ngRoute','uploader.controller','uploader.service'])

    .config(['$locationProvider','$routeProvider',function($locationProvider,$routeProvider){

        // Create routes which will be checked upon when routing.
        $routeProvider.when('/uploader/uploadMain',{templateUrl: '/app/partials/uploader/IndexUploadInitiationPartial.html', controller: 'UploaderInitiationController'});
        $routeProvider.when('/uploader/addEntryInfo',{templateUrl: '/app/partials/uploader/IndexUploadInfoAdditionPartial.html', controller: 'EntryAdditionController'});

        // Redirect to the Main Starting Page for the Sweep Project, so what should come here.
        $routeProvider.otherwise({redirectTo: '/uploader/uploadMain'});

    }])
    .filter('noneStatus',function(){
        
        return function(data){
            
            if(data == null){
                return;
            }
            
            var filtered = [];
            for( var i =0 ; i < data.length; i++ ){
                
                if(data[i]["status"] == "NONE"){
                    filtered.push(data[i]);
                }
            }
            
            return filtered;
        }
    })
    .filter('restStatus',function(){

        return function(data){

            if(data == null){
                return;
            }
            
            var filtered = [];
            for( var i =0 ; i < data.length; i++ ){

                if(data[i]["status"] !== "NONE"){
                    filtered.push(data[i]);
                }
            }
            
            return filtered;
        }
    });