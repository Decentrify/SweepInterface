// Core Service Module.

angular.module("core-services",[])
    
    .service("GvodService",['$log','$http',function($log,$http){
        
        var _defaultHost = "http://localhost:8100";
        var _defaultContentType = "application/json";
        
        // Get a promise object.
        function _getPromiseObject (method,url,contentType,data){
            
            return $http({
                method: method,
                url: url,
                headers: {'Content-Type': contentType},
                data: data
            });
        }
        
        return {
            
            // Play the resource.
            play : function(json){
                
                var method = "PUT";    
                var url = _defaultHost.concat("/play");
                
                return _getPromiseObject(method,url,_defaultContentType,json);
            },
            
            
            download : function(json){
                  
                var method = "PUT";
                var url = _defaultHost.concat("/downloadvideo");

                return _getPromiseObject(method,url,_defaultContentType,json);
            },
            
            // Fetch the files in the library.
            fetchFiles : function(){
                
                var method = "GET";
                var url = _defaultHost.concat("/files");
                
                return _getPromiseObject(method,url,_defaultContentType);
            }
            
        }

    }]);