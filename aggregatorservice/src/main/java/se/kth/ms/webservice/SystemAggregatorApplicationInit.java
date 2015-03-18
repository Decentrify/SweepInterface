package se.kth.ms.webservice;

import se.sics.gvod.address.Address;
import se.sics.kompics.Init;

/**
 * Init for the System Aggregator Application.
 * Created by babbarshaer on 2015-03-18.
 */
public class SystemAggregatorApplicationInit extends Init<SystemAggregatorApplication>{
    
    public String[] args;
    public Address aggregatorAddress;
    
    public SystemAggregatorApplicationInit(String args[], Address aggregatorAddress){
        this.args = args;
        this.aggregatorAddress = aggregatorAddress;
    }
}
