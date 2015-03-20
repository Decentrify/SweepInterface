package se.kth.ms.webservice;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.ms.webmodel.SimpleDataModel;
import se.sics.ms.aggregator.data.SweepAggregatedPacket;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * Main aggregator webservice.
 *  
 * Created by babbarshaer on 2015-03-18.
 */
public class AggregatorWebService extends Service<Configuration> {
    
    private Logger logger = LoggerFactory.getLogger(AggregatorWebService.class);
    private static SystemAggregatorApplication application;
    
    public AggregatorWebService(SystemAggregatorApplication application){
        this.application = application;
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        logger.debug("Service Initialize invoked");
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        
        logger.info("Service Run Method invoked.");
        environment.addProvider(new GlobalStateResource());
        environment.addProvider(new Handshake());

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

    @Path("/systemstate")
    @Produces(MediaType.APPLICATION_JSON)
    public static class GlobalStateResource {

        @GET
        public Response getGlobalStateResource() {
            Collection<SweepAggregatedPacket> stateMap = application.getSystemGlobalState();
            
            if(stateMap == null){
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Found Map with null values").build();
            }
            return Response.status(Response.Status.OK).entity(stateMap).build();
        }
    }

    @Path("/systemsimplemodel")
    @Produces(MediaType.APPLICATION_JSON)
    public static class SimpleDataModelResource {

        @GET
        public Response getGlobalStateResource() {
            Collection<SimpleDataModel> simpleModelCollection = application.getStateInSimpleDataModel();

            if(simpleModelCollection == null){
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Found Map with null values").build();
            }
            return Response.status(Response.Status.OK).entity(simpleModelCollection).build();
        }
    }
    
    
    

    @Path("/handshake")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Handshake {
        @GET
        public Response removeVideo() {
            return Response.status(Response.Status.OK).entity(true).build();
        }
    }
    
}
