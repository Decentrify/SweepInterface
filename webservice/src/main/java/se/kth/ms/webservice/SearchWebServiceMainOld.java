///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package se.kth.ms.webservice;
//
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//import java.util.*;
//import java.util.logging.Level;
//
//import org.apache.commons.cli.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import se.sics.cm.ChunkManagerConfiguration;
//import se.sics.co.FailureDetectorComponent;
//import se.sics.co.FailureDetectorPort;
//import se.sics.gvod.address.Address;
//import se.sics.gvod.common.Self;
//import se.sics.gvod.common.util.ToVodAddr;
//import se.sics.gvod.config.ElectionConfiguration;
//import se.sics.gvod.config.GradientConfiguration;
//import se.sics.gvod.config.SearchConfiguration;
//import se.sics.gvod.nat.traversal.NatTraverser;
//import se.sics.gvod.nat.traversal.events.NatTraverserInit;
//import se.sics.gvod.net.NatNetworkControl;
//import se.sics.gvod.net.NettyInit;
//import se.sics.gvod.net.NettyNetwork;
//import se.sics.gvod.net.Transport;
//import se.sics.gvod.net.VodNetwork;
//import se.sics.gvod.net.events.PortBindRequest;
//import se.sics.gvod.net.events.PortBindResponse;
//import se.sics.gvod.timer.Timer;
//import se.sics.gvod.timer.java.JavaTimer;
//import se.sics.kompics.*;
//import se.sics.kompics.nat.utils.getip.IpAddrStatus;
//import se.sics.kompics.nat.utils.getip.ResolveIp;
//import se.sics.kompics.nat.utils.getip.ResolveIpPort;
//import se.sics.kompics.nat.utils.getip.events.GetIpRequest;
//import se.sics.kompics.nat.utils.getip.events.GetIpResponse;
//import se.sics.ms.common.MsSelfImpl;
//import se.sics.ms.configuration.MsConfig;
//import se.sics.ms.launch.CommonEncodeDecode;
//import se.sics.ms.net.MessageFrameDecoder;
//import se.sics.ms.search.SearchPeer;
//import se.sics.ms.search.SearchPeerInit;
//import se.sics.ms.ports.UiPort;
//import se.sics.ms.timeout.IndividualTimeout;
//import se.sics.p2ptoolbox.croupier.core.CroupierConfig;
//import se.sics.p2ptoolbox.election.core.ElectionConfig;
//import se.sics.p2ptoolbox.gradient.core.GradientConfig;
//
///**
// * @author alidar
// */
//public class SearchWebServiceMain extends ComponentDefinition {
//
//    private static final Logger logger = LoggerFactory.getLogger(SearchWebServiceMain.class);
//    Component network;
//    Component timer;
//    Component natTraverser;
//    Component searchPeer;
//    Component searchMiddleware;
//    private Component resolveIp;
//    private Self self;
//    private Address myAddr;
//    private Address bootstrapAddress;
//    Positive<UiPort> uiPort = positive(UiPort.class);
//    SearchDelegate delegate;
//    private SearchWebServiceMain myComp;
//    private int bindCount = 0; //
//
//    // Create Options for Command Line Parsing.
//    private Options options = new Options();
//
//
//    private CommandLine line;
//    private CommandLineParser parser;
//
//    private String[] dropwizardArgs = null;
//    private String ip;
//
//    private static String[] arguments;
//
//
//    // Aggregator Component Information.
//    private String aggregatorIp = "cloud7.sics.se";
//    private int aggregatorId = 0;
//    private int aggregatorPort = 54321;
//    private Address aggregatorAddress;
//
//    public static class PsPortBindResponse extends PortBindResponse {
//        public PsPortBindResponse(PortBindRequest request) {
//            super(request);
//        }
//    }
//
//    public SearchWebServiceMain() {
//
//        init();
////        CommonEncodeDecode.init();    // Croupier and Gradient Boot Up.
//        myComp = this;
//        subscribe(handleStart, control);
//
//        resolveIp = create(ResolveIp.class, Init.NONE);
//        timer = create(JavaTimer.class, Init.NONE);
//        bootstrapAddress = MsConfig.getBootstrapServer();
//        connect(resolveIp.getNegative(Timer.class), timer.getPositive(Timer.class));
//        subscribe(handleGetIpResponse, resolveIp.getPositive(ResolveIpPort.class));
//
//    }
//
//
//    public void init(){
//
//        List<String> argList = new ArrayList<String>();
//        for (int i = 0; i < arguments.length; i++) {
//            if (arguments[i].startsWith("-X")) {
//                argList.add(arguments[i]);
//            }
//        }
//
//
//        Option dropwizardOption = new Option("Xserver",true," Dropwizard Server Command.");
//        Option IpOption = new Option("Xip", true,"Specific IP Command");
//        Option aggregatorIpOption = new Option("XaIp", true, "Aggregator Ip Address");
//        Option aggregatorIdOption = new Option("XaId", true, "Aggregator Component Id");
//        Option aggregatorPortOption = new Option("XaPort", true, "Aggregator Port");
//
//        options.addOption(dropwizardOption);
//        options.addOption(IpOption);
//        options.addOption(aggregatorIpOption);
//        options.addOption(aggregatorIdOption);
//        options.addOption(aggregatorPortOption);
//
//        parser = new GnuParser();
//
//        try{
//            line = parser.parse(options,argList.toArray(new String[argList.size()]));
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//            System.exit(-1);
//        }
//
//        if(line.hasOption(dropwizardOption.getOpt())){
//
//            String serverLoc = line.getOptionValue(dropwizardOption.getOpt());
//            dropwizardArgs = new String[]{"server",serverLoc};
//            System.out.println("Dropwizard Config File Location. -> " + serverLoc);
//
//        }
//
//        if(line.hasOption(IpOption.getOpt())){
//
//            ip = line.getOptionValue(IpOption.getOpt());
//            System.out.println("IP Option Passed. " + ip);
//
//        }
//
//        if(line.hasOption(aggregatorIpOption.getOpt())){
//            aggregatorIp = line.getOptionValue(aggregatorIpOption.getOpt());
//            logger.info(" Aggregator Ip Option Set: {}", aggregatorIp);
//        }
//
//        if(line.hasOption(aggregatorIdOption.getOpt())){
//            aggregatorId = Integer.parseInt(line.getOptionValue(aggregatorIdOption.getOpt()));
//            logger.info(" Aggregator Id Option Set: {}", aggregatorId);
//        }
//
//        if(line.hasOption(aggregatorPortOption.getOpt())){
//            aggregatorPort = Integer.parseInt(line.getOptionValue(aggregatorPortOption.getOpt()));
//            logger.info(" Aggregator Port Option Set: {}", aggregatorPort);
//        }
//
//    }
//
//    Handler<Start> handleStart = new Handler<Start>() {
//        @Override
//        public void handle(Start event) {
//            trigger(new GetIpRequest(false),
//                    resolveIp.getPositive(ResolveIpPort.class));
//        }
//    };
//    private Handler<PsPortBindResponse> handlePsPortBindResponse =
//            new Handler<PsPortBindResponse>() {
//                @Override
//                public void handle(PsPortBindResponse event) {
//
//                    if (event.getStatus() != PortBindResponse.Status.SUCCESS) {
//                        logger.warn("Couldn't bind to port {}. Either another instance of the program is"
//                                        + "already running, or that port is being used by a different program. Go"
//                                        + "to settings to change the port in use. Status: ", event.getPort(),
//                                event.getStatus());
//                        Kompics.shutdown();
//                        System.exit(-1);
//                    } else {
//
//                        bindCount++;
//                        if (bindCount == 2) { //if both UDP and TCP ports have successfully binded.
//                            self = new MsSelfImpl(ToVodAddr.systemAddr(myAddr));
//
//                            Set<Address> publicNodes = new HashSet<Address>();
//                            try {
//
//                                if(bootstrapAddress != null)
//                                    publicNodes.add(bootstrapAddress);
//                                else
//                                    throw new UnknownHostException("Bootstrapping Node not found.");
//
//                            } catch (UnknownHostException ex) {
//                                // Don't start if we cant get an address for the bootstrapping node.
//                                logger.error(ex.getMessage());
//                                System.exit(-1);
//                            }
//
//                            CroupierConfig croupierConfig = buildCroupierConfiguration();
//                            GradientConfig gradientConfig = buildGradientConfiguration();
//                            ElectionConfig electionConfig = buildElectionConfiguration();
//
//                            aggregatorAddress = getAggregatorAddress();
//
//                            natTraverser = create(NatTraverser.class, new NatTraverserInit(self, publicNodes, MsConfig.getSeed()));
//                            searchMiddleware = create(SearchWebServiceMiddleware.class, new SearchWebServiceMiddlewareInit(null,dropwizardArgs));
////                            searchPeer = create(SearchPeer.class, new SearchPeerInit(self, croupierConfig,
////                                    SearchConfiguration.build(), GradientConfiguration.build(),
////                                    ElectionConfiguration.build(), ChunkManagerConfiguration.build(),gradientConfig,
////                                    ToVodAddr.bootstrap(bootstrapAddress),  (aggregatorAddress!= null) ? ToVodAddr.systemAddr(aggregatorAddress) : null, electionConfig));
//
//                            Component fd = create(FailureDetectorComponent.class, Init.NONE);
//
//                            connect(natTraverser.getNegative(Timer.class), timer.getPositive(Timer.class));
//                            connect(natTraverser.getNegative(VodNetwork.class), network.getPositive(VodNetwork.class));
//                            connect(natTraverser.getNegative(NatNetworkControl.class), network.getPositive(NatNetworkControl.class));
//                            connect(searchMiddleware.getPositive(UiPort.class), searchPeer.getNegative(UiPort.class));
//
//                            /** Filter not working for some reason so commenting it.**/
////                            connect(network.getPositive(VodNetwork.class), searchPeer.getNegative(VodNetwork.class),new MsgDestFilterAddress(myAddr));
//
//                            connect(network.getPositive(VodNetwork.class), searchPeer.getNegative(VodNetwork.class));
//                            connect(timer.getPositive(Timer.class), searchPeer.getNegative(Timer.class),
//                                    new IndividualTimeout.IndividualTimeoutFilter(myAddr.getId()));
//                            connect(fd.getPositive(FailureDetectorPort.class), searchPeer.getNegative(FailureDetectorPort.class));
//
////                            subscribe(handleFault, natTraverser.getControl());
//
//                            trigger(Start.event, natTraverser.getControl());
//                            trigger(Start.event, searchMiddleware.getControl());
//                            trigger(Start.event, searchPeer.getControl());
//                            trigger(Start.event, fd.getControl());
//
//                        }
//                    }
//                }
//            };
//
//
//
//    /**
//     * Based on the parameters passed, construct the aggregator address.
//     * @return Aggregator Address.
//     */
//    private Address getAggregatorAddress(){
//
//        Address aggregatorAddress = null;
//        try {
//
//            InetAddress ipAddr = InetAddress.getByName(aggregatorIp);
//            aggregatorAddress = new Address(ipAddr, aggregatorPort, aggregatorId);
//
//        }
//        catch (UnknownHostException e) {
//            e.printStackTrace();
//            return null;
//        }
//        return aggregatorAddress;
//    }
//
//
//    private GradientConfig buildGradientConfiguration() {
//        return new GradientConfig(MsConfig.GRADIENT_VIEW_SIZE, MsConfig.GRADIENT_SHUFFLE_PERIOD, MsConfig.GRADIENT_SHUFFLE_LENGTH);
//    }
//    private ElectionConfig buildElectionConfiguration() {
//        ElectionConfig.ElectionConfigBuilder builder = new ElectionConfig.ElectionConfigBuilder(MsConfig.GRADIENT_VIEW_SIZE);
//        builder.setLeaseTime(50000); // 50 seconds
//
//        return builder.buildElectionConfig();
//    }
//
//    /**
//     * @return Croupier Configuration.
//     */
//    private CroupierConfig buildCroupierConfiguration(){
//
//        return null;
//    }
//
//
//    public Handler<GetIpResponse> handleGetIpResponse = new Handler<GetIpResponse>() {
//        @Override
//        public void handle(GetIpResponse event) {
//
//            //FIXME: Fix the case in which the different nodes in which the initial seed is generated same.
////            int myId = (new Random(MsConfig.getSeed())).nextInt();
////            int myId = (new Random()).nextInt();
//
//            int myId = Integer.MAX_VALUE;   // Testing Purposes.
//
//            InetAddress localIp = resolveLocalAddress(ip , event.getAddrs());
//
//            // Switch to default mode.
//            if(localIp == null){
//                System.out.println(" Unable to resolve local address");
//                System.out.println(" Switching to default mode to fetch IPAddress.");
//                localIp = event.getIpAddress();
//            }
//
//            // Quit from the system.
//            if(localIp == null){
//                System.exit(-1);
//                throw new RuntimeException("No IPAddress Found. Quitting ... ");
//            }
//
//            logger.info("My Local Ip Address returned from ResolveIp is:  " + localIp.getHostName());
//            if (localIp.getHostName().equals(bootstrapAddress.getIp().getHostName()))
//                myId = 0;
//
//            // Bind Udt and Udp on separate ports in the system for now.
//            myAddr = new Address(localIp, MsConfig.getPort(), myId);
//            Address myUdtAddr = new Address(localIp, MsConfig.getPort() + 1, myId);
//
//            network = create(NettyNetwork.class, new NettyInit(MsConfig.getSeed(), true, MessageFrameDecoder.class));
//
////            subscribe(handleNettyFault, network.getControl());
//            subscribe(handlePsPortBindResponse, network.getPositive(NatNetworkControl.class));
//
//            trigger(Start.event, network.getControl());
//
//            bindPort(Transport.UDP, myAddr);
//            bindPort(Transport.UDT, myUdtAddr);
//        }
//    };
//
//
//    private InetAddress resolveLocalAddress(String ipAddress , List<IpAddrStatus> localAddresses){
//
//        InetAddress addr = null;
//
//        // NULL CHECK OR NOT ?
//        if(ipAddress != null){
//
//            for(IpAddrStatus addrStatus : localAddresses){
//                if(addrStatus.getAddr().getHostAddress().equals(ipAddress)){
//                    addr = addrStatus.getAddr();
//                }
//            }
//
//        }
//
//        return addr;
//    }
//
//
//    void bindPort(Transport transport, Address address) {
//
//        logger.info("Sending a port bind request for : " + address.toString());
//        PortBindRequest portBindReq = new PortBindRequest(address, transport);
//        PsPortBindResponse pbr = new PsPortBindResponse(portBindReq);
//        portBindReq.setResponse(pbr);
//        trigger(portBindReq, network.getPositive(NatNetworkControl.class));
//
//    }
//
//    public static int randInt(int min, int max) {
//
//        // Usually this should be a field rather than a method variable so
//        // that it is not re-seeded every call.
//        Random rand = new Random(MsConfig.getSeed());
//
//        // nextInt is normally exclusive of the top value,
//        // so add 1 to make it inclusive
//        int randomNum = rand.nextInt((max - min) + 1) + min;
//
//        return randomNum;
//    }
//
//    /**
//     * Starts the execution of the program
//     *
//     * @param args the command line arguments
//     * @throws IOException in case the configuration file couldn't be created
//     */
//    public static void main(String[] args) throws IOException {
//
//        int cores = Runtime.getRuntime().availableProcessors();
//        int numWorkers = Math.max(1, cores - 1);
//
//        System.setProperty("java.net.preferIPv4Stack", "true");
//        try {
//            // Keep a copy of arguments and initialize the MsConfig.
//            arguments = args;
//            MsConfig.init(args);
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(SearchWebServiceMain.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        Kompics.createAndStart(SearchWebServiceMain.class, numWorkers);
//    }
//}
