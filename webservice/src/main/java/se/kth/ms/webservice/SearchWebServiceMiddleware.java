/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.ms.webservice;
import java.util.ArrayList;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Start;
import se.sics.ms.events.UiAddIndexEntryRequest;
import se.sics.ms.events.UiAddIndexEntryResponse;
import se.sics.ms.ports.UiPort;
import se.sics.ms.events.UiSearchRequest;
import se.sics.ms.events.UiSearchResponse;
import se.sics.ms.types.IndexEntry;
import se.sics.ms.types.SearchPattern;
import se.sics.ms.ui.UiComponent;

/**
 *
 * @author alidar
 */
public class SearchWebServiceMiddleware extends ComponentDefinition {
    
    private final Logger logger = LoggerFactory.getLogger(UiComponent.class);
    Negative<UiPort> uiPort = negative(UiPort.class);    
    
    SearchDelegate searchDelegate;
    SearchWebServiceMiddleware myComp;
    
    private String[] args;
    
    public SearchWebServiceMiddleware()
    {
        subscribe(handleStart, control);
        subscribe(searchResponseHandler, uiPort);
        subscribe(addIndexEntryUiResponseHandler, uiPort);
        myComp = this;
    }
    
    public SearchWebServiceMiddleware(SearchWebServiceMiddlewareInit init){
        doInit(init);
        subscribe(handleStart, control);
        subscribe(searchResponseHandler, uiPort);
        subscribe(addIndexEntryUiResponseHandler, uiPort);
    }
    
    
    private void doInit(SearchWebServiceMiddlewareInit init){
        
        args = init.getArgs();
        myComp = this;
    }
    
    Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {

            try {
                // Start the Dropwizard REST Services
                SearchWebService searchWebService = new SearchWebService(myComp);
                if(args != null){
                    String[] newArgs = new String[]{args[0],args[1]};
                    searchWebService.run(newArgs);
                }
                else
                    searchWebService.run(new String[]{"server"});
                
                searchDelegate = searchWebService;
                
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(SearchWebServiceMiddleware.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    };    
    
    /**
     * Handles results from the Search component
     */
    final Handler<UiSearchResponse> searchResponseHandler = new Handler<UiSearchResponse>() {
        @Override
        public void handle(UiSearchResponse searchResponse) {
            ArrayList<IndexEntry> results = searchResponse.getResults();

            searchDelegate.didSearch(results);
        }
    };

    /**
     * Handles results of adding to the index
     */
    final Handler<UiAddIndexEntryResponse> addIndexEntryUiResponseHandler = new Handler<UiAddIndexEntryResponse>() {
        @Override
        public void handle(UiAddIndexEntryResponse addIndexEntryUiResponse) {
            boolean result = addIndexEntryUiResponse.isSuccessful();

            if(result)
                searchDelegate.didAddIndex();
            else
                searchDelegate.didFailToAddIndex();
        }
    };

    public void search(SearchPattern pattern) {
        trigger(new UiSearchRequest(pattern), uiPort);
    }

    public void addIndexEntry(IndexEntry entry) {
        trigger(new UiAddIndexEntryRequest(entry), uiPort);
    }
}
