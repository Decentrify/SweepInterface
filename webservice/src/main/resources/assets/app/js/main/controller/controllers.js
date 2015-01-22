/**
 * Created by babbarshaer on 2014-08-18.
 */

// Main module responsible for transferring control to a new page in angular.
angular.module('mainModule.controller',[])

    .controller('NavigationController',['$window','$log','$scope',function($window,$log,$scope){

         $scope.uploadNavigation = function(){
            $log.info("Start the transfer to the new page ...");
            $window.location = "IndexUploadMain.html";
        };

        $scope.searchNavigation = function(){
            $log.info("Start the transfer to the search page ... ");
            $window.location = "SearchMain.html";
        }

    }]);