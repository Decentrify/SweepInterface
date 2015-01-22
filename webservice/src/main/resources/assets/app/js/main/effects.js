// Effect to simply move the div above.
jQuery(document).ready(function(){

    setTimeout(function(){
        jQuery('#heading').animate({"marginTop" :"100px"},600,function(){
            $('.navigation').fadeIn();
        });
    },500);

});