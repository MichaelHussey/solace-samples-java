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

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.TextMessage;

public class TopicPublisher extends BaseClass {


	public void subscribeToEvents() throws JCSMPException{
        /** Anonymous inner-class for handling publishing events */
        prod = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
            public void responseReceived(String messageID) {
                System.out.println("ACK received for msg: " + messageID);
            }
            public void handleError(String messageID, JCSMPException e, long timestamp) {
                System.out.printf("Producer received error for msgId: %s%n%s%n",
                        messageID,e.getMessage());
            }
        });
        // Publish-only session is now hooked up and running!
		
	}
	
	public void publish() throws JCSMPException {
		boolean once = true;
		TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
		while(continuous || once){
			System.out.printf("About to send %s messages to topic '%s'...%n",count,topic.getName());
			startTime = System.nanoTime();
			for (int i=0; i<count; i++){
				final String text = startTime+" Hello world! Msg #"+i;
				msg.setText(text);
				prod.send(msg,topic);
			} 
			once = false;
			long endTime = System.nanoTime();
			double delta = (endTime - startTime);
			long waitT = (long) (1000/rate -delta/1000.0/1000.0	);
			if (waitT <=  0)
				waitT = 0;
			try {
				//long rate = (long) (count / delta * 1000.0 * 1000.0 * 1000.0);
				System.out.println("Sent at "+rate+" msgs/sec, now waiting for "+waitT+" ms");
				Thread.sleep(waitT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		System.out.println(count+" Messages sent. Exiting.");
	}

	
	public static void main(String... args) throws JCSMPException {
 
        // Check command line arguments
    	TopicPublisher pub = new TopicPublisher();
    	pub.parseArgs(args);
        
    	pub.connect();
		
    	pub.subscribeToEvents();
    	
    	pub.publish();

        pub.close();
    }

}
