package se.kth.ms.webservice;

import se.sics.kompics.PortType;
import se.sics.p2ptoolbox.aggregator.api.msg.Ready;

/**
 * Application Port.
 *
 * Created by babbarshaer on 2015-03-18.
 */
public class AggregatorApplicationPort extends PortType{{
    indication(Ready.class);
}}
