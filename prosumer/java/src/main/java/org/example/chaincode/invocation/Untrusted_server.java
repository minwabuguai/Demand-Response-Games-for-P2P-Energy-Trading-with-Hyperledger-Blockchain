
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example.chaincode.invocation;

import org.example.client.CAClient;
import org.example.client.ChannelClient;
import org.example.client.FabricClient;
import org.example.config.Config;
import org.example.config.Config_dynamic;
import org.example.user.UserContext;
import org.hyperledger.fabric.sdk.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Untrusted_server {

    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "event";
    public static double p_out = 0.2;


    public static void main(String args[]) {
        try {
            //Util.cleanUp();
            String caUrl = Config.CA_ORG1_URL;
            CAClient caClient = new CAClient(caUrl, null);

            UserContext adminUserContext = new UserContext();
            adminUserContext.setName(Config.ADMIN);
            adminUserContext.setAffiliation(Config.ORG1);
            adminUserContext.setMspId(Config.ORG1_MSP);
            caClient.setAdminUserContext(adminUserContext);
            adminUserContext = caClient.enrollAdminUser(Config.ADMIN, Config.ADMIN_PASSWORD);

            FabricClient fabClient = new FabricClient(adminUserContext);

            ChannelClient channelClient = fabClient.createChannelClient(Config.CHANNEL_NAME);
            Channel channel = channelClient.getChannel();
            Peer peer = fabClient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
            EventHub eventHub = fabClient.getInstance().newEventHub("eventhub01", "grpc://localhost:7053");
            Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL);
            channel.addPeer(peer);
            channel.addEventHub(eventHub);
            channel.addOrderer(orderer);
            channel.initialize();

            String stringResponse = new String();

            try {
                System.out.println("the passing cmd is:python server.py");
                Process process = Runtime.getRuntime().exec("python server.py");
                //boolean exitValue = process.waitFor(200, TimeUnit.SECONDS);
                int exitValue = process.waitFor();

                System.out.println(exitValue);
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(process.getErrorStream()));
                // Read the output from the command
                System.out.println("Here is the standard output of the command:\n");
                String s = null;
                String temp = null;
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                    if(s.length()>=9) {
                        System.out.println(s.substring(0,9));
                        if (s.substring(0,9).equals("converged")){
                            temp = s.substring(17);
                            p_out = Double.parseDouble(temp);
                            System.out.println(p_out);
                        }
                    }
                }
                // Read any errors from the attempted command
                System.out.println("Here is the standard error of the command (if any):\n");
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                }
            } catch (IOException e) { e.printStackTrace();
            } catch (InterruptedException e) { e.printStackTrace(); }


            String[] args2 = {String.valueOf(p_out)};
            TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
            ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
            request.setChaincodeID(ccid);
            request.setFcn("SubmitPrice");
            request.setArgs(args2);
            request.setProposalWaitTime(500);
            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8)); // Just some extra junk
            // in transient map
            tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8)); // ditto
            tm2.put("result", ":)".getBytes(UTF_8)); // This should be returned see chaincode why.
            tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA); // This should trigger an event see chaincode why.
            request.setTransientMap(tm2);
            Collection<ProposalResponse> responses2Pricing = channelClient.sendTransactionProposal(request);

            System.out.println(responses2Pricing);
            for (ProposalResponse pres : responses2Pricing) {
                String stringResponse2 = new String(pres.getChaincodeActionResponsePayload());
                System.out.println("Response:" + stringResponse2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
