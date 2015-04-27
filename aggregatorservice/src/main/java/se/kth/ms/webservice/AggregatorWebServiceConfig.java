package se.kth.ms.webservice;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

/**
 * Configuration for the search webservice.
 *
 * Created by babbarshaer on 2015-04-26.
 */
public class AggregatorWebServiceConfig {

    private final String configLocation;
    
    public AggregatorWebServiceConfig(Config config){
        
        try{
            configLocation = config.getString("aggregator-webservice.server");
            
        }
        catch(ConfigException.Missing ex){
            throw new RuntimeException("Unable to locate server information.");
        }
    }
    
    public AggregatorWebServiceConfig(String configLocation){
        this.configLocation = configLocation;
    }

    public String getConfigLocation(){
        return this.configLocation;
    }
}
