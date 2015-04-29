/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.ms.webservice;

//import io.dropwizard.Application;
//import io.dropwizard.Configuration;
//import io.dropwizard.setup.Bootstrap;
//import io.dropwizard.setup.Environment;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import se.kth.ms.webservicemodel.*;
import se.sics.ms.types.IndexEntry;
import se.sics.ms.types.SearchPattern;

/**
 *
 * @author jdowling
 */
public class SearchWebService extends Service<Configuration> implements SearchDelegate
{
    static SearchWebServiceMiddleware search;

    static Semaphore requestMutex       = new Semaphore(1);
    static Semaphore waitForResultMutex = new Semaphore(0);

    static boolean isWaitForResultInProgress = false;
    static long SEARCH_TIMEOUT = 10;
    static long INSERT_TIMEOUT = 30*3;

    static String REQUEST_TIMED_OUT_MSG = "Request timed out";
    static String REQUEST_INTERRUPTED_MSG = "Request interupted";

    static boolean addIndexSuccess = false;
    static ArrayList<SearchIndexResultJSON> searchIndexResults;

    public SearchWebService(SearchWebServiceMiddleware searchMain)
    {
        search = searchMain;
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap)
    {
//        bootstrap.addBundle(new AssetsBundle("/assets/","/webapp","app/SweepMain.html"));
        bootstrap.addBundle(new AssetsBundle("/interface/","/webapp/"));
//        bootstrap.setName("dw-server"); // name must match the yaml config file
    }

    @Override
    public void run(Configuration configuration, Environment environment) {
        
        environment.addProvider(new SearchIndexResource());
        environment.addProvider(new AddIndexResource());
        environment.addProvider(new FetchFiles());


        /*
         * To allow cross orign resource request from angular js client
         */
        environment.addFilter(CrossOriginFilter.class,"/*").
                setInitParam("allowedOrigins", "*").
                setInitParam("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin").
                setInitParam("allowedMethods", "GET,PUT,POST,DELETE,OPTIONS").
                setInitParam("preflightMaxAge", "5184000"). // 2 months
                setInitParam("allowCredentials", "true");
    }




    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class AddIndexResource {
        @PUT
        public Response add(AddIndexRequestJSON addRequest)
        {

            System.out.println(" Received add index entry request from with filename :   " + addRequest.getFileName());
            StatusResponseJSON result = new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, null);
            try
            {
                if(requestMutex.tryAcquire(INSERT_TIMEOUT, TimeUnit.SECONDS))
                {
                    String reason = validateAddIndexRequest(addRequest);

                    if(reason == null)
                    {
                        IndexEntry entry = new IndexEntry(
                                addRequest.getUrl(),
                                addRequest.getFileName(),
                                addRequest.getFileSize(),
                                new Date(),
                                addRequest.getLanguage(),
                                addRequest.getCategory(),
                                addRequest.getDescription());

                        search.addIndexEntry(entry);

                        try
                        {
                            isWaitForResultInProgress = true;
                            if(waitForResultMutex.tryAcquire(INSERT_TIMEOUT, TimeUnit.SECONDS))
                            {
                                if(addIndexSuccess)
                                    result.setStatus(StatusResponseJSON.SUCCESS_STRING);
                                else
                                    result.setReason("Unknown reason");
                            }
                            else
                            {
                                result.setReason(REQUEST_TIMED_OUT_MSG);
                            }
                        }
                        catch (InterruptedException ex)
                        {
                            Logger.getLogger(SearchWebService.class.getName()).log(Level.SEVERE, null, ex);

                            result.setReason(REQUEST_INTERRUPTED_MSG);
                        }
                        finally
                        {
                            isWaitForResultInProgress = false;
                        }
                    }
                    else
                    {
                        result.setReason(reason);
                    }
                    requestMutex.release();
                }
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(SearchWebService.class.getName()).log(Level.SEVERE, null, ex);

                result.setReason(REQUEST_INTERRUPTED_MSG);
            }

            if(result.getStatus().equalsIgnoreCase(StatusResponseJSON.SUCCESS_STRING))
                return Response.status(Response.Status.OK).entity(result).build();
            else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
        }
    }
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class SearchIndexResource
    {
        @PUT
        public Response search(SearchIndexRequestJSON searchRequest) {

            Object res = new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, REQUEST_TIMED_OUT_MSG);

            try
            {
                if(requestMutex.tryAcquire(SEARCH_TIMEOUT, TimeUnit.SECONDS))
                {

                    searchIndexResults = null;

                    search.search(new SearchPattern(
                            searchRequest.getFileNamePattern(),
                            searchRequest.getMinFileSize(),
                            searchRequest.getMaxFileSize(),
                            searchRequest.getMinUploadDate(),
                            searchRequest.getMaxUploadDate(),
                            searchRequest.getLanguage(),
                            searchRequest.getCategory(),
                            searchRequest.getDescriptionPattern()));

                    try
                    {
                        isWaitForResultInProgress = true;
                        if(waitForResultMutex.tryAcquire(SEARCH_TIMEOUT, TimeUnit.SECONDS))
                            res = searchIndexResults;
                    }
                    catch (InterruptedException ex)
                    {
                        Logger.getLogger(SearchWebService.class.getName()).log(Level.SEVERE, null, ex);

                        ((StatusResponseJSON)res).setStatus(REQUEST_INTERRUPTED_MSG);

                    }
                    finally
                    {
                        isWaitForResultInProgress = false;
                    }
                    requestMutex.release();
                }
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(SearchWebService.class.getName()).log(Level.SEVERE, null, ex);

                ((StatusResponseJSON)res).setStatus(REQUEST_INTERRUPTED_MSG);
            }

            if((res instanceof StatusResponseJSON) &&
                    (((StatusResponseJSON)res).getStatus().equalsIgnoreCase(StatusResponseJSON.ERROR_STRING)))
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(res).build();
            else
                return Response.status(Response.Status.OK).entity(res).build();
        }
    }

    @Path("/fetchFiles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class FetchFiles
    {
        @GET
        public Response fetchFiles(){

//            System.out.println(" Request Received For Category ... " + jsonRequest.getCategory());
            System.out.println(" Relaying it to the GVod Project.  ");
//            System.exit(-1);
            return null;
        }
    }


    @Override
    public void didSearch(ArrayList<IndexEntry> results)
    {
        if(isWaitForResultInProgress)
        {
            searchIndexResults = convertToSearchIndexResultJSON(results);
            waitForResultMutex.release();
        }
    }

    @Override
    public void didAddIndex()
    {
        if(isWaitForResultInProgress)
        {
            addIndexSuccess = true;
            waitForResultMutex.release();
        }
    }

    @Override
    public void didFailToAddIndex()
    {
        if(isWaitForResultInProgress)
        {
            addIndexSuccess = false;
            waitForResultMutex.release();
        }
    }

    static String validateAddIndexRequest(AddIndexRequestJSON request)
    {
        if(request.getUrl() == null)
            return "URL is missing";
        else if((request.getFileName() == null) || (request.getFileName().trim().length() == 0))
            return "File name is missing or invalid";
        else if(request.getFileSize() == 0)
            return "File size is invalid";
        else if(request.getLanguage() == null)
            return "Language is missing";
        else
            return null;
    }

    ArrayList<SearchIndexResultJSON> convertToSearchIndexResultJSON(ArrayList<IndexEntry> indexList)
    {
        ArrayList<SearchIndexResultJSON> resultList = new ArrayList<SearchIndexResultJSON>();

        for(IndexEntry entry: indexList)
        {
            SearchIndexResultJSON convertedEntry = new SearchIndexResultJSON(
                    "",
                    entry.getUrl(),
                    entry.getFileName(),
                    entry.getFileSize(),
                    entry.getUploaded(),
                    entry.getLanguage(),
                    entry.getCategory(),
                    entry.getDescription());

            resultList.add(convertedEntry);
        }

        return resultList;
    }

}