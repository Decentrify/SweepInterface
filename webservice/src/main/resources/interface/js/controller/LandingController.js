'use strict';

angular.module('app')
    .controller('LandingController', ['$log','$location', function($log, $location){
        $log.info("Current Host Location: " + $location.host());
        $log.info('Landing Controller Initialized.');
    }]);