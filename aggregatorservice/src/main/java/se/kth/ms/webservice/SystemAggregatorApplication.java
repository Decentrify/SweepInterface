package se.kth.ms.webservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.ms.webmodel.SimpleDataModel;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.ms.aggregator.data.SweepAggregatedPacket;
import se.sics.p2ptoolbox.aggregator.api.model.AggregatedStatePacket;
import se.sics.p2ptoolbox.aggregator.api.msg.GlobalState;
import se.sics.p2ptoolbox.aggregator.api.msg.Ready;
import se.sics.p2ptoolbox.aggregator.api.port.GlobalAggregatorPort;
import se.sics.p2ptoolbox.aggregator.core.GlobalAggregatorComponent;
import se.sics.p2ptoolbox.aggregator.core.GlobalAggregatorComponentInit;
import se.sics.p2ptoolbox.util.config.SystemConfig;
import se.sics.p2ptoolbox.util.network.impl.BasicAddress;
import se.sics.p2ptoolbox.util.network.impl.DecoratedAddress;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Receives and interprets the updates from the GlobalAggregator Application.
 * Created by babbarshaer on 2015-03-18.
 */
public class SystemAggregatorApplication extends ComponentDefinition{
    
    private ConcurrentMap<BasicAddress, SweepAggregatedPacket> systemGlobalState;
    private Logger logger = LoggerFactory.getLogger(SystemAggregatorApplication.class);
    private Component globalAggregator;
    private long timeout = 5000;
    private long windowTimeout = 10000;
    
    private Positive<Network> networkPort = requires(Network.class);
    private Positive<Timer> timerPositive = requires(Timer.class);
    private Negative<AggregatorApplicationPort> applicationPort = provides(AggregatorApplicationPort.class);
    private SystemAggregatorApplication myComp;
    
    private SystemConfig systemConfig;
    private AggregatorWebServiceConfig aggregatorConfig;

    public SystemAggregatorApplication(SystemAggregatorApplicationInit init){
        
        doInit(init);
        subscribe(startHandler, control);
        
        globalAggregator = create(GlobalAggregatorComponent.class, new GlobalAggregatorComponentInit(timeout, windowTimeout));
        connect(globalAggregator.getNegative(Timer.class), timerPositive);
        connect(globalAggregator.getNegative(Network.class), networkPort);

        subscribe(globalStateHandler, globalAggregator.getPositive(GlobalAggregatorPort.class));
        subscribe(readyHandler, globalAggregator.getPositive(GlobalAggregatorPort.class));
    }
    
    
    
    public void doInit(SystemAggregatorApplicationInit init){
        
        logger.info("init");
        systemGlobalState = new ConcurrentHashMap<BasicAddress, SweepAggregatedPacket>();
        myComp = this;
        systemConfig = init.systemConfig;
        aggregatorConfig = init.aggregatorWebServiceConfig;
    }
    
    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            
            logger.debug("System Application for capturing aggregated data booted up.");
            logger.info("Boot the webservice from here.");
        }
    };
    
    
    Handler<GlobalState> globalStateHandler = new Handler<GlobalState>() {
        @Override
        public void handle(GlobalState event) {


            Map<DecoratedAddress, AggregatedStatePacket> map = event.getStatePacketMap();
            logger.info("Received Aggregated State Packet Map with size: {}" , map.size());
            systemGlobalState.clear();
            
            for(Map.Entry<DecoratedAddress, AggregatedStatePacket> entry : map.entrySet()){
                
                BasicAddress address = entry.getKey().getBase();
                AggregatedStatePacket statePacket = entry.getValue();
                
                if(statePacket instanceof SweepAggregatedPacket){
                    SweepAggregatedPacket sap = (SweepAggregatedPacket)statePacket;
                    systemGlobalState.put(address, sap);
                }
            }
        }
    };
    
    Handler<Ready> readyHandler = new Handler<Ready>() {
        @Override
        public void handle(Ready event) {
            logger.debug("Global Aggregator Component Ready");
            startWebService();
            trigger(new Ready(), applicationPort);
        }
    };


    private void startWebService(){

        logger.info(" Start Web Service Method invoked. ... ");

        AggregatorWebService service = new AggregatorWebService(myComp);
        String confLocation = aggregatorConfig.getConfigLocation();
        logger.info("Location of webservice configuration: {}", confLocation);

        try {
            String[] arguments = confLocation != null ? new String[]{"server",confLocation} : new String[]{"server"};
            service.run(arguments);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(" Unable to start the webservice for aggregator.");
        }
    }

    
    public Collection<SweepAggregatedPacket> getSystemGlobalState(){
        return this.systemGlobalState.values();
    }

    public Collection<SimpleDataModel> getStateInSimpleDataModel(){
        return SystemAggregatorDataModelBuilder.getSimpleDataModel(systemGlobalState);
    }
    
    
    

}
