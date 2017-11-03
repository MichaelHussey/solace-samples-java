/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.solace.samples;

import java.util.regex.Pattern;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageProducer;

/**
 *  
 * @author michussey
 *
 */
public class BaseClass {

	protected String msgVpnName = "default";
	protected String clientUsername = "default";
	protected String clientPassword = null;
	protected String smfHost = "localhost:5555";
	protected String topicName = "tutorial/topic";
	protected JCSMPSession session;
	protected int count = 1;
	protected long processedCount = 0;
	protected Topic topic;
	protected XMLMessageConsumer cons;
	protected XMLMessageProducer prod;
	protected boolean debug = false;
	protected boolean continuous = false;
	long startTime;
	protected int rate = 4;
	protected String cacheName;

	public void connect() throws JCSMPException
	{
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, smfHost);
        properties.setProperty(JCSMPProperties.VPN_NAME, msgVpnName);
        properties.setProperty(JCSMPProperties.USERNAME, clientUsername);
        if (clientPassword != null)
        {
            properties.setProperty(JCSMPProperties.PASSWORD, clientPassword);
        }
        System.out.println(this.getClass().getName()+" initializing...");
        topic = JCSMPFactory.onlyInstance().createTopic(topicName);
        session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();		
	}

	public void parseArgs(String[] args) {
		
		if (args.length == 0)
			usage();
		
		for (String arg : args) {
			// Only split if param matches the "-param=value" regexp.
			if (Pattern.matches("-[\\w]+=.*", arg)) {
				String[] tokens = arg.split("=", 2);
				if (tokens[0].equals("-vpn"))
				{
					msgVpnName  = tokens[1];
				}
				else if (tokens[0].equals("-username"))
				{
					clientUsername  = tokens[1];
				}
				else if (tokens[0].equals("-password"))
				{
					clientPassword  = tokens[1];
				}
				else if (tokens[0].equals("-smfhost"))
				{
					smfHost  = tokens[1];
				}
				else if (tokens[0].equals("-topic"))
				{
					topicName  = tokens[1];
				}
				else if (tokens[0].equals("-count"))
				{
					count   = Integer.parseInt(tokens[1]);
				}
				else if (tokens[0].equals("-rate"))
				{
					rate    = Integer.parseInt(tokens[1]);
				}
				else if (tokens[0].equals("-debug"))
				{
					debug   = Boolean.parseBoolean(tokens[1]);
				}
				else if (tokens[0].equals("-continuous"))
				{
					continuous   = Boolean.parseBoolean(tokens[1]);
				}
				else if (tokens[0].equals("-cacheName"))
				{
					cacheName   = tokens[1];
				}
				else
				{
					System.out.println("Unrecognised parameter "+arg);
					usage();
				}
			}
		}
	
	}
	protected int getCount() {
		return count;
	}

	public void usage() {
	    System.out.println("Usage: "+this.getClass().getName()+" -vpn=<msg_vpn> -smfhost=<msg_backbone_ip:port>\n"
	    		+" -username=<username> -password=<password> -topic=<topic/name> -count=<num of messages to send/receive>\n"
	       		+" -debug=<true|false> enable tracing\n"
	       		+" -continuous=<true|false> publish in a loop\n"
	       	    		);
	    System.exit(-1);
	}
	
	protected void close() {
		if(cons != null)
			cons.close();
        System.out.println("Exiting. Processed "+count+" messages on Topic: "+topicName);
        session.closeSession();
	}
	protected long getStartTime() {
		return startTime;
	}


}
