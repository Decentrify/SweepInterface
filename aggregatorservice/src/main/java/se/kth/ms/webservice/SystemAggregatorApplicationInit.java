package se.kth.ms.webservice;

import se.sics.kompics.Init;
import se.sics.p2ptoolbox.util.network.impl.BasicAddress;

/**
 * Init for the System Aggregator Application.
 * Created by babbarshaer on 2015-03-18.
 */
public class SystemAggregatorApplicationInit extends Init<SystemAggregatorApplication>{
    
    public String[] args;
    public BasicAddress aggregatorAddress;
    
    public SystemAggregatorApplicationInit(String args[], BasicAddress aggregatorAddress){
        this.args = args;
        this.aggregatorAddress = aggregatorAddress;
    }
}
