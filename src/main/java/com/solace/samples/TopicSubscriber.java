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
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageListener;

public class TopicSubscriber extends BaseClass {

	
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
        // Consume-only session is now hooked up and running!
        return latch;
		
	}

    public static void main(String... args) throws JCSMPException {

        // Check command line arguments
    	TopicSubscriber sub = new TopicSubscriber();
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
