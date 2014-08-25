/**
 * Created by babbarshaer on 2014-08-14.
 */

// This will be using jquery to perform the fading in and out of the directory.

/**
 * Clear the table contents everytime a table is displayed.
 */
function refreshMetaDataEntryTable (){
    console.log(" Refresh Data Table ");
}

//$.noConflict();
//jQuery(document).ready(function(){
//    // When the document is done loading.
//    jQuery(".file").on("click",function(){
//
//        var checkedState = $(this).prop("checked");
//
//        //Clear every checkbox state.
//        jQuery(this).closest("#fileset").find(".file").each(function(){
//            jQuery(this).prop("checked",false);
//        });
//
//        var table = jQuery(this).closest("#mainUploadContent").find("#indexEntryTable");
//
//        // check own changed state.
//        if(checkedState){
//
//            refreshMetaDataEntryTable();
//            table.fadeIn();
//        }
//        // silently let the table fade out.
//        else{
//            table.fadeOut();
//        }
//
//        //Update my property.
//        jQuery(this).prop("checked",checkedState);
//
//    });
//
//});


function addEntryInfoAnimation (){

    jQuery(".file").on("click",function(){

        var checkedState = $(this).prop("checked");

        //Clear every checkbox state.
        jQuery(this).closest("#fileset").find(".file").each(function(){
            jQuery(this).prop("checked",false);
        });

        var table = jQuery(this).closest("#mainUploadContent").find("#indexEntryTable");

        // check own changed state.
        if(checkedState){

            refreshMetaDataEntryTable();
            table.fadeIn();
        }
        // silently let the table fade out.
        else{
            table.fadeOut();
        }

        //Update my property.
        jQuery(this).prop("checked",checkedState);

    });
}




