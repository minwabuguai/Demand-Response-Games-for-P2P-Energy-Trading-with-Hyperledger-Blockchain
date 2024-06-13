
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

/******************************************************
 *  Copyright 2018 IBM Corporation
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.example.chaincode.invocation;


import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.*;
import java.lang.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.client.CAClient;
import org.example.client.ChannelClient;
import org.example.client.FabricClient;
import org.example.config.Config;
import org.example.config.Config_dynamic;
import org.example.user.UserContext;
import org.example.util.Util;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
//import com.mathworks.engine.*;
//import com.mathworks.matlab.types.*;

/**
 *
 * @author Balaji Kadambi
 *
 */

public class Biding_retailer {

	private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
	private static final String EXPECTED_EVENT_NAME = "event";

	public static double a0;
	public static double a1;
	public static double a2;
	public static double bid;
	public static double gamma;
	public static double p_out = 0;
	public static double price_version = 0;
	public static double bid_version = 0;
	public static String isConverge = "0";
	public static boolean isOffloading = false;
	static LocalDateTime starttime;
	static LocalDateTime endtime;
	public static int retries = 0;
	public static int sleep_time;
	public static int v=25;
	public static int m=25;

	public Biding_retailer(){

		try {
			FileInputStream file = new FileInputStream(new File("data_final.xlsm"));

			//Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			//Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(Integer.parseInt(Config_dynamic.SHEET_NUM));
			Row row0 = sheet.getRow(Integer.parseInt(Config_dynamic.LINE_NUM));
			Cell cell1 = row0.getCell(1);//energy generation
			Cell cell2 = row0.getCell(2);
			Cell cell3 = row0.getCell(3);
			Cell cell4 = row0.getCell(4);
			Cell cell5 = row0.getCell(5);

			a0 = cell1.getNumericCellValue();
			a1 = cell2.getNumericCellValue();
			a2 = cell3.getNumericCellValue();
			gamma = cell4.getNumericCellValue();
			bid = cell5.getNumericCellValue();
			//System.out.println("a0="+a0+"aa="+aa);


		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static void main(String args[]) {
		starttime = LocalDateTime.now();

		try {
			//Util.cleanUp();
			Biding_retailer retailer = new Biding_retailer();
			String caUrl = Config.CA_ORG1_URL;
			CAClient caClient = new CAClient(caUrl, null);
			// Enroll Admin to Org1MSP
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

//InitUser------------------------------------------------------------------
			TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
			ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).build();
			request.setChaincodeID(ccid);
			request.setFcn("initUser");
			String[] arguments = {Config_dynamic.USER_NAME, Config_dynamic.ROLE_NAME};
			request.setArgs(arguments);
			request.setProposalWaitTime(500);

			Map<String, byte[]> tm = new HashMap<>();
			tm.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8)); // Just some extra junk
			// in transient map
			tm.put("method", "TransactionProposalRequest".getBytes(UTF_8)); // ditto
			tm.put("result", ":)".getBytes(UTF_8)); // This should be returned see chaincode why.
			tm.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA); // This should trigger an event see chaincode why.
			request.setTransientMap(tm);
			Collection<ProposalResponse> responses = channelClient.sendTransactionProposal(request);
			System.out.println(responses);

			Thread.sleep(6*1000);

			String Auction_active = "true";

			if (isOffloading == true){
				retailer.offloading();
				Thread.sleep(3 * 1000);
			}



			while (Auction_active == "true") {
				// Query the initiate price first----------------------------------------------
				String[] arg0 = {"price_version", "p_out", "isConverge"};
				Collection<ProposalResponse> responsesQuery0 = channelClient.queryByChainCode(Config.CHAINCODE_1_NAME, "Query", arg0);
				for (ProposalResponse pres : responsesQuery0) {
					String stringResponse = new String(pres.getChaincodeActionResponsePayload());
					System.out.println(stringResponse);
					String[] result = stringResponse.split(",");
					retailer.price_version = Double.parseDouble(result[0]);
					retailer.p_out = Double.parseDouble(result[1]);
					retailer.isConverge = result[2];
				}

				if (retailer.isConverge.equals("true")){
					endtime = LocalDateTime.now();
					System.out.println("The final price is : p_out= "+retailer.p_out);
					System.out.println("The time is : p_out= "+starttime+"--"+endtime);

					break;
				}

				if (retailer.price_version >= retailer.bid_version) {
					if (retailer.bid_version == 0 && isOffloading == false){
						retailer.Submit_first_bid(fabClient, ccid, channelClient);
					}else {
						try {
							retailer.updateBid (fabClient, ccid, channelClient);
						}catch(Exception e) {
							if (retries < 50) {
								retries+=1;
								System.out.println ("retry time=" + retries);
								continue;
							} else {
								throw e;
							}
						}
					}

					retailer.bid_version += 1;

					Thread.sleep(500);  //This is sleep2
				}


				Random r = new Random();
				//sleep_time = (int)r.nextGaussian()*v+m;
				if (sleep_time<=0){
					sleep_time = 1;
				}
				sleep_time = 5;
				System.out.println(sleep_time);

				Thread.sleep(sleep_time * 100);//This is Sleep 1

			}

		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Submit_first_bid(FabricClient fabClient, ChaincodeID ccid, ChannelClient channelClient) throws InvalidArgumentException, ProposalException {
		System.out.println("the current bid is:"+bid);
		TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
		request.setChaincodeID(ccid);
		request.setFcn("updateBid");
		String[] arguments = {Config_dynamic.USER_NAME, Double.toString(bid)};
		request.setArgs(arguments);
		request.setProposalWaitTime(500);
		Map<String, byte[]> tm2 = new HashMap<>();
		tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8)); // Just some extra junk
		// in transient map
		tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8)); // ditto
		tm2.put("result", ":)".getBytes(UTF_8)); // This should be returned see chaincode why.
		tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA); // This should trigger an event see chaincode why.
		request.setTransientMap(tm2);
		Collection<ProposalResponse> responses = channelClient.sendTransactionProposal(request);
		System.out.println(responses);
	}

	public static void updateBid(FabricClient fabClient, ChaincodeID ccid, ChannelClient channelClient) throws InvalidArgumentException, ProposalException {
		try {
			String[] params = new String[] {"octave","--eval", "retailerApp("+bid+","+a0+","+a1+","+a2+","+gamma+","+p_out+")" };
			System.out.println("the passing cmd is:"+params[0]+params[1]+params[2]);
			Process process = Runtime.getRuntime().exec(params);
			int exitValue = process.waitFor();
			//boolean exitValue = process.waitFor(200, TimeUnit.SECONDS);
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
				if(s.length()>=4) {
					if (s.substring(0,3).equals("ans")){
						temp = s.substring(6);
						bid = Double.parseDouble(temp);
						System.out.println(bid);
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

		System.out.println("updated bid is:"+bid);
		TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
		request.setChaincodeID(ccid);
		request.setFcn("updateBid");
		String[] arguments = {Config_dynamic.USER_NAME, Double.toString(bid)};
		request.setArgs(arguments);
		request.setProposalWaitTime(500);
		Map<String, byte[]> tm2 = new HashMap<>();
		tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8)); // Just some extra junk
		// in transient map
		tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8)); // ditto
		tm2.put("result", ":)".getBytes(UTF_8)); // This should be returned see chaincode why.
		tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA); // This should trigger an event see chaincode why.
		request.setTransientMap(tm2);
		Collection<ProposalResponse> responses = channelClient.sendTransactionProposal(request);
		System.out.println(responses);

		//Auction_active="false";

	}

	public static void offloading(){
		try {
			System.out.println("the passing cmd is:python client_retailer.py");
			Process process = Runtime.getRuntime().exec("python client_retailer.py");
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
				if(s.length()>=14) {
					if (s.substring(0,9).equals("converged")){
						temp = s.substring(14);
						bid = Double.parseDouble(temp);
						System.out.println("bid="+bid);
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


	}




}
