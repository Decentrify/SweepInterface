/**
 * Created by babbarshaer on 2015-02-02.
 */


// === SWEEP WEBSERVICE === //

angular.module('app')
    .service('sweepService',['$log','$http',function($log,$http){

        
        // Default Objects.
        
        var _defaultMethod = 'PUT';
        var _defaultHeader = {'Content-Type': 'application/json'};
        var _defaultIp = "http://localhost:18080";
        
        
        function _getPromiseObject(method, url, headers, data){
            return $http({
                method: method,
                url: url,
                headers: headers,
                data: data
            })
        }
        
        
        return {

            performSearch : function(searchJson){
                
                var _url = _defaultIp.concat('/').concat('search');
                return _getPromiseObject(_defaultMethod,_url,_defaultHeader,searchJson);
            },

            addIndexEntry : function(entryData){
                $log.info("Index Entry Initiated.");
                var _url = _defaultIp.concat('/').concat('add');
                return _getPromiseObject(_defaultMethod, _url, _defaultHeader, entryData);
            }

        }
        
    }]);

