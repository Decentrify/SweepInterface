package se.kth.ms.webservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.address.Address;
import se.sics.gvod.net.*;
import se.sics.gvod.net.events.PortBindRequest;
import se.sics.gvod.net.events.PortBindResponse;
import se.sics.gvod.timer.Timer;
import se.sics.gvod.timer.java.JavaTimer;
import se.sics.kompics.*;
import se.sics.kompics.nat.utils.getip.ResolveIp;
import se.sics.kompics.nat.utils.getip.ResolveIpPort;
import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
import se.sics.ms.common.CommonEncodeDecode;
import se.sics.ms.net.MessageFrameDecoder;
import se.sics.p2ptoolbox.aggregator.api.msg.Ready;

import java.net.InetAddress;


/**
 * Main Class required to Boot Up the Aggregator Service
 *
 * Created by babbarshaer on 2015-03-18.
 */
public class AggregateServiceLauncher extends ComponentDefinition{
    
    private int port = 58223; // Default Value.
    private Logger logger = LoggerFactory.getLogger(AggregateServiceLauncher.class);
    private static String[] arguments = null;
    private int id =0;
    private int seed = 1234;
    
    Component timer;
    Component resolveIp;
    Component network;
    Component application;      // Use this reference and invoke direct methods on it.
    
    private Address selfAddress;
    
    public AggregateServiceLauncher(){
        
        init();
        CommonEncodeDecode.init();
        subscribe(startHandler, control);
        
        timer = create(JavaTimer.class, Init.NONE);
        resolveIp = create(ResolveIp.class, Init.NONE);

        connect(resolveIp.getNegative(Timer.class), timer.getPositive(Timer.class));
        subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));
        
        
    }

    /**
     * Initialize the launcher.
     * Mainly do the command line parsing in this.
     */
    private void init() {

    }
    
    
    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            logger.debug("Component Started.");
            logger.info("phase 1 - getting ip");
            trigger(new GetIpRequest(false), resolveIp.getPositive(ResolveIpPort.class));
        }
    };

    Handler<GetIpResponse> handleGetIpResponse  = new Handler<GetIpResponse>() {
        @Override
        public void handle(GetIpResponse resp) {
            phase2(resp.getIpAddress());
            BootstrapPortBind.Request pb1 = new BootstrapPortBind.Request(selfAddress, Transport.UDP);
            pb1.setResponse(new BootstrapPortBind.Response(pb1));
            trigger(pb1, network.getPositive(NatNetworkControl.class));
        }
    };
    

    private void phase2(InetAddress selfIp) {
        logger.info("phase 2 - ip:{} - binding port:{}", selfIp, port);
        selfAddress = new Address(selfIp, port, id);

        network = create(NettyNetwork.class, new NettyInit(seed, true, MessageFrameDecoder.class));
        connect(network.getNegative(Timer.class), timer.getPositive(Timer.class));

        subscribe(handlePsPortBindResponse, network.getPositive(NatNetworkControl.class));
        trigger(Start.event, network.getControl());
    }


    public Handler<BootstrapPortBind.Response> handlePsPortBindResponse = new Handler<BootstrapPortBind.Response>() {

        @Override
        public void handle(BootstrapPortBind.Response resp) {
            if (resp.getStatus() != PortBindResponse.Status.SUCCESS) {
                logger.warn("Couldn't bind to port {}. Either another instance of the program is"
                        + "already running, or that port is being used by a different program. Go"
                        + "to settings to change the port in use. Status: ", resp.getPort(), resp.getStatus());
                Kompics.shutdown();
                System.exit(-1);
            } else {
                selfAddress = resp.boundAddress;
                phase3();
            }
        }
    };



    private void phase3(){

        logger.info("phase 3: trying to bring up the aggregator component.");
        application = create(SystemAggregatorApplication.class, new SystemAggregatorApplicationInit(arguments) );

        connect(application.getNegative(Timer.class), timer.getPositive(Timer.class));
        connect(application.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));
        subscribe(readyEventHandler, application.getPositive(AggregatorApplicationPort.class));

        trigger(Start.event, application.control());
    }
    
    
    
    Handler<Ready> readyEventHandler = new Handler<Ready>() {
        @Override
        public void handle(Ready event) {
            logger.info("Received the ready event from the sweep aggregator application.");
            logger.info("Time to boot up the webservice.");
            phase4();
        }
    };

    
    private void phase4() {
        logger.debug("At this point Aggregator Wrapper is ready.");
    }


    private static class BootstrapPortBind {
        private static class Request extends PortBindRequest {

            public final Address boundAddress;
            
            public Request(Address address, Transport transport) {
                super(address, transport);
                this.boundAddress = address;
            }
        }

        private static class Response extends PortBindResponse {
            public final Address boundAddress;

            public Response(Request req) {
                super(req);
                this.boundAddress = req.boundAddress;
            }
        }
    }
    
    

    public static void main(String[] args) {
        
        int cores = Runtime.getRuntime().availableProcessors();
        int numWorkers = Math.max(1, cores - 1);
        System.setProperty("java.net.preferIPv4Stack", "true");
        arguments = args;
        Kompics.createAndStart(AggregateServiceLauncher.class, numWorkers);
    }
}
