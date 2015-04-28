package se.kth.ms.webservice;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;
import se.sics.ms.net.SerializerSetup;
import se.sics.p2ptoolbox.aggregator.api.msg.Ready;
import se.sics.p2ptoolbox.aggregator.network.AggregatorSerializerSetup;
import se.sics.p2ptoolbox.chunkmanager.ChunkManagerSerializerSetup;
import se.sics.p2ptoolbox.croupier.CroupierSerializerSetup;
import se.sics.p2ptoolbox.election.network.ElectionSerializerSetup;
import se.sics.p2ptoolbox.gradient.GradientSerializerSetup;
import se.sics.p2ptoolbox.util.config.SystemConfig;
import se.sics.p2ptoolbox.util.serializer.BasicSerializerSetup;



/**
 * Main Class required to Boot Up the Aggregator Service
 *
 * Created by babbarshaer on 2015-03-18.
 */
public class AggregateServiceLauncher extends ComponentDefinition{

    private Logger logger = LoggerFactory.getLogger(AggregateServiceLauncher.class);
    private int startId = 128;

    Component timer;
    Component network;
    Component application;      // Use this reference and invoke direct methods on it.

    // Configuration settings.
    Config config;

    public AggregateServiceLauncher(){

        init();

        SystemConfig systemConfig = new SystemConfig(config);
        AggregatorWebServiceConfig aggregatorWebServiceConfig = new AggregatorWebServiceConfig(config);

        timer = create(JavaTimer.class, Init.NONE);
        network = create(NettyNetwork.class, new NettyInit(systemConfig.self));

        logger.info("Trying to create the aggregator component");
        application = create( SystemAggregatorApplication.class, new SystemAggregatorApplicationInit(systemConfig, aggregatorWebServiceConfig) );

        connect(application.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(application.getNegative(Network.class), network.getPositive(Network.class));

        subscribe(readyEventHandler, application.getPositive(AggregatorApplicationPort.class));
        subscribe(startHandler, control);
        subscribe(readyEventHandler, application.getPositive(AggregatorApplicationPort.class));
    }

    private void registerSerializers(int startId) {

        int currentId = startId;
        BasicSerializerSetup.registerBasicSerializers(currentId);
        currentId += BasicSerializerSetup.serializerIds;
        currentId = CroupierSerializerSetup.registerSerializers(currentId);
        currentId = GradientSerializerSetup.registerSerializers(currentId);
        currentId = ElectionSerializerSetup.registerSerializers(currentId);
        currentId = AggregatorSerializerSetup.registerSerializers(currentId);
        currentId = ChunkManagerSerializerSetup.registerSerializers(currentId);
        SerializerSetup.registerSerializers(currentId);
    }

    /**
     * Initialize the launcher.
     * Mainly do the command line parsing in this.
     */
    private void init() {

        logger.debug("Loading the configuration files");
        config = ConfigFactory.load();
        registerSerializers(startId);
    }
    
    
    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            logger.debug("Component Started.");
        }
    };




    Handler<Ready> readyEventHandler = new Handler<Ready>() {
        @Override
        public void handle(Ready event) {
            logger.info("Received the ready event from the sweep aggregator application.");
            logger.info("Time to boot up the webservice.");
        }
    };


    public static void main(String[] args) {
        
        int cores = Runtime.getRuntime().availableProcessors();
        int numWorkers = Math.max(1, cores - 1);
        System.setProperty("java.net.preferIPv4Stack", "true");
        Kompics.createAndStart(AggregateServiceLauncher.class, numWorkers);
    }
}
