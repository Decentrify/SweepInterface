
angular.module('videoApp',[])
    
    .controller("MainVideoController",["$log","$scope",function($log,$scope){
        
        function _initScope(scope){
            scope.source = "http://localhost:58026/messi.mp4/messi.mp4";
            
            
            if(scope.source != null){

                var player = videojs('example_video_1', { /* Options */ }, function() {
                    
                    console.log('Good to go!');

//                    console.log(this);
                    
//                    this.source(scope.source);
                    this.src(scope.source);
                    this.dimensions(640,264);
                    
                    this.controls(true);
//                    this.play(); // if you don't trust autoplay for some reason
                    
                    // How about an event listener?
                    this.on('ended', function() {
                        console.log('awww...over so soon?');
                    });
                    
//                    console.log(" PlaybackRate: ");
                    
                    this.on('volumechange',function(){

                        var bufferedTimeRange = this.buffered();
                        console.log(bufferedTimeRange);

                        // Number of different ranges of time have been buffered. Usually 1.
                        var numberOfRanges = bufferedTimeRange.length;
                        console.log("number of ranges: " + numberOfRanges);

                        // Time in seconds when the first range starts. Usually 0.
                        var firstRangeStart = bufferedTimeRange.start(0);
                        console.log("start range: " + firstRangeStart);

                        // Time in seconds when the first range ends
                        var firstRangeEnd = bufferedTimeRange.end(0);
                        console.log("end range:" + firstRangeEnd);

                        // Length in seconds of the first time range
                        var firstRangeLength = firstRangeEnd - firstRangeStart;
                        console.log("range length:" + firstRangeLength);
                        
                        console.log()
                    });
                    
                    
                    this.on('seeked',function(){
                        console.log('Seeked .... ');
                    });

                    this.on('seeking', function () {
                        
                        console.log("seeking forward ...");
                        
                        var ct = player.currentTime();
                        if(ct > curpos) {
                            player.currentTime(curpos);
                        }
                    });
                    
                    
                    
                });
                
            }
            
        }
        
        
        
        
        _initScope($scope);
        
    }]);