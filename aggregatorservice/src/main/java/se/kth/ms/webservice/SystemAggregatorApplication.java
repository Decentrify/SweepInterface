package se.kth.ms.webservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.net.VodAddress;
import se.sics.gvod.net.VodNetwork;
import se.sics.gvod.timer.Timer;
import se.sics.kompics.*;
import se.sics.ms.types.SweepAggregatedPacket;
import se.sics.p2ptoolbox.aggregator.api.model.AggregatedStatePacket;
import se.sics.p2ptoolbox.aggregator.api.msg.GlobalState;
import se.sics.p2ptoolbox.aggregator.api.msg.Ready;
import se.sics.p2ptoolbox.aggregator.api.port.GlobalAggregatorPort;
import se.sics.p2ptoolbox.aggregator.core.GlobalAggregatorComponent;
import se.sics.p2ptoolbox.aggregator.core.GlobalAggregatorComponentInit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Receives and interprets the updates from the GlobalAggregator Application.
 * Created by babbarshaer on 2015-03-18.
 */
public class SystemAggregatorApplication extends ComponentDefinition{
    
    private Map<VodAddress, SweepAggregatedPacket> systemGlobalState;
    private Logger logger = LoggerFactory.getLogger(SystemAggregatorApplication.class);
    private Component globalAggregator;
    private long timeout = 5000;
    
    private Positive<VodNetwork> networkPort = requires(VodNetwork.class);
    private Positive<Timer> timerPositive = requires(Timer.class);
    private Negative<AggregatorApplicationPort> applicationPort = provides(AggregatorApplicationPort.class);
    private SystemAggregatorApplication myComp;
    
    private String[] arguments;
    
    public SystemAggregatorApplication(SystemAggregatorApplicationInit init){
        
        doInit(init);
        subscribe(startHandler, control);
        
        globalAggregator = create(GlobalAggregatorComponent.class, new GlobalAggregatorComponentInit(timeout));
        connect(globalAggregator.getNegative(Timer.class), timerPositive);
        connect(globalAggregator.getNegative(VodNetwork.class), networkPort);
        
        subscribe(globalStateHandler, globalAggregator.getPositive(GlobalAggregatorPort.class));
        subscribe(readyHandler, globalAggregator.getPositive(GlobalAggregatorPort.class));
    }
    
    
    
    public void doInit(SystemAggregatorApplicationInit init){
        
        logger.info("init");
        systemGlobalState = new HashMap<VodAddress, SweepAggregatedPacket>();
        myComp = this;
        arguments = init.args;
    }
    
    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            
            logger.debug("System Application for capturing aggregated data booted up.");
            logger.info("Boot the webservice from here.");
            
            AggregatorWebService service = new AggregatorWebService(myComp);
            try {
                
                arguments = arguments != null ? arguments : new String[]{"server"};
                service.run(arguments);
                
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(" Unable to start the webservice for aggregator.");
            }
        }
    };
    
    
    Handler<GlobalState> globalStateHandler = new Handler<GlobalState>() {
        @Override
        public void handle(GlobalState event) {

            logger.info("Received Aggregated State Packet Map");
            Map<VodAddress, AggregatedStatePacket> map = event.getStatePacketMap();
            systemGlobalState.clear();
            
            for(Map.Entry<VodAddress, AggregatedStatePacket> entry : map.entrySet()){
                
                VodAddress address = entry.getKey();
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
            trigger(new Ready(), applicationPort);
        }
    };
    
    
    public Collection<SweepAggregatedPacket> getSystemGlobalState(){
        return this.systemGlobalState.values();
    }
}
