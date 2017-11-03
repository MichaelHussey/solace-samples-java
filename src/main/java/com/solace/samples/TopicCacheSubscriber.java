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

import java.util.concurrent.CountDownLatch;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.CacheLiveDataAction;
import com.solacesystems.jcsmp.CacheSession;
import com.solacesystems.jcsmp.CacheSessionProperties;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageListener;

public class TopicCacheSubscriber extends BaseClass{

	// Listen to Topic and quit after <count> messages
	public CountDownLatch subscribe() throws JCSMPException {
        // used for
        // synchronizing b/w threads
        final CountDownLatch latch = new CountDownLatch(count); 

        /** Anonymous inner-class for MessageListener
         *  This demonstrates the async threaded message callback */
        cons = session.getMessageConsumer(new XMLMessageListener() {
            public void onReceive(BytesXMLMessage msg) {
            	processedCount++;
            	if(processedCount == 1)
            		startTime = System.nanoTime();
                if (msg instanceof TextMessage) {
                    if (debug)
                    	System.out.printf("TextMessage received: '%s'%n",
                            ((TextMessage)msg).getText());
                } else {
                    System.out.println("Message received.");
                }
                if (debug)
                	System.out.printf("Message Dump:%n%s%n",msg.dump());
                latch.countDown();  // unblock main thread
            }
            public void onException(JCSMPException e) {
                System.out.printf("Consumer received exception: %s%n",e);
                latch.countDown();  // unblock main thread
            }
        });
        session.addSubscription(topic);
        System.out.println("Connected. Awaiting "+ count +" messages...");
        cons.start();
        
        // Add Cache handling
		// Requests the most recent message on the topic, using default timeout 10sec
		CacheSessionProperties props = new CacheSessionProperties(cacheName);
		try {
			CacheSession cacheSession = session.createCacheSession(props);
						
			cacheSession.sendCacheRequest(1L, topic, false, CacheLiveDataAction.FULFILL);
			
		} catch (JCSMPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // Consume-only session is now hooked up and running!
        return latch;
		
	}
	@Override
	public void parseArgs(String[] args) {
		super.parseArgs(args);
		if (cacheName == null) {
			System.out.println("Please set the cacheName");
			usage();
		}
	}

	@Override
	public void usage() {
	    System.out.println("Usage: "+this.getClass().getName()+" -vpn=<msg_vpn> -smfhost=<msg_backbone_ip:port>\n"
	    		+" -username=<username> -password=<password> -topic=<topic/name> -count=<num of messages to send/receive>\n"
	       		+" -debug=<true|false> enable tracing\n"
	       		+" -continuous=<true|false> publish in a loop\n"
	       		+" -cacheName=<name> Name of a SolCache to use for initial receipt of direct messages\n"
	       	    		);
	    System.exit(-1);
	}


    public static void main(String... args) throws JCSMPException {

        // Check command line arguments
    	TopicCacheSubscriber sub = new TopicCacheSubscriber();
        sub.parseArgs(args);
        
        sub.connect();
        CountDownLatch latch = sub.subscribe();
        
        try {
            latch.await(); // block here until message received, and latch will flip
            long finishTime = System.nanoTime();
            double delta = (finishTime - sub.getStartTime()) / 1000.0;
            System.out.println("Received "+sub.getCount()+" messages in "+delta+"ms");
       } catch (InterruptedException e) {
            System.out.println("I was awoken while waiting");
        }
        // Close consumer
        sub.close();
    }
	private JCSMPSession getSession() {
		return session;
	}
}
