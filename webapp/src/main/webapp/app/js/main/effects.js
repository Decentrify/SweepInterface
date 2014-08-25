// Effect to simply move the div above.
jQuery(document).ready(function(){

    setTimeout(function(){
        jQuery('#heading').animate({"marginTop" :"0px"},600,function(){
            $('.navigation').fadeIn();
        });
    },500);

});