/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.ms.webservice;

import se.sics.kompics.Init;

/**
 *
 * @author alidar
 */
public class SearchWebServiceMiddlewareInit extends Init<SearchWebServiceMiddleware> {

    private SearchDelegate searchDelegate;
    private String[] args;
    private SearchWebServiceConfig config;
    
    public SearchWebServiceMiddlewareInit(SearchDelegate searchDelegate, String[] args){
        this.searchDelegate = searchDelegate;
        this.args = args;
    }
    
    public SearchWebServiceMiddlewareInit(SearchWebServiceConfig config){
        this.config = config;
    }
    
    
    public SearchWebServiceConfig getConfig(){
        return this.config;
    }
    
    /**
     * @return the searchDelegate
     */
    public SearchDelegate getSearchDelegate() {
        return searchDelegate;
    }

    /**
     *  
     * @return arguments.
     */
    public String[] getArgs(){
        return this.args;
    }

    /**
     * @param args
     */
    public void setArgs(String[] args){
        
        this.args = args;
    }
    
    /**
     * @param searchDelegate the searchDelegate to set
     */
    public void setSearchDelegate(SearchDelegate searchDelegate) {
        this.searchDelegate = searchDelegate;
    }

}
