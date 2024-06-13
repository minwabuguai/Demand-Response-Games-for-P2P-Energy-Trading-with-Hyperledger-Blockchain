
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
import java.util.concurrent.ThreadLocalRandom;
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

public class Biding_prosumer {

	private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
	private static final String EXPECTED_EVENT_NAME = "event";

	public static double p_s = 0.0;
	public static double p_b = 0.0;
	public static double E_max = 80.0;
	public static double E_g;
	public static double k=0.0;
	public static int k_min=4;//0.4
	public static int k_max=6;//0.6
	public static double base_d;
	public static double load_d;
	public static double E_import;
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


	public Biding_prosumer(){

		try {
			FileInputStream file = new FileInputStream(new File("data_final.xlsm"));

			//Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			//Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(Integer.parseInt(Config_dynamic.SHEET_NUM));
			Row row0 = sheet.getRow(Integer.parseInt(Config_dynamic.LINE_NUM));
			Cell cell2 = row0.getCell(2);//energy generation
			Cell cell7 = row0.getCell(7);
			Cell cell6 = row0.getCell(6);
			Cell cell8 = row0.getCell(8);

			k = cell8.getNumericCellValue();
			E_g = cell2.getNumericCellValue();
			base_d = cell7.getNumericCellValue();
			load_d = cell6.getNumericCellValue();
			E_import = E_g - (load_d + base_d);
			System.out.println("E_import="+this.E_import);

		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static void main(String args[]) {
		Biding_prosumer prosumer = new Biding_prosumer();
		starttime = LocalDateTime.now();

		try {
			//Util.cleanUp();
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
			//prosumer.k = 0.1 * ThreadLocalRandom.current().nextInt(prosumer.k_min,prosumer.k_max + 1);

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
				prosumer.offloading();
				System.out.println("prosumer.E_import = "+prosumer.E_import);
				Thread.sleep(3 * 1000);
			}


			while (Auction_active == "true") {
				// Query the initiate price first----------------------------------------------
				String[] arg0 = {"price_version", "p_b", "p_s", "isConverge"};
				Collection<ProposalResponse> responsesQuery0 = channelClient.queryByChainCode(Config.CHAINCODE_1_NAME, "Query", arg0);
				for (ProposalResponse pres : responsesQuery0) {
					String stringResponse = new String(pres.getChaincodeActionResponsePayload());
					System.out.println(stringResponse);
					String[] result = stringResponse.split(",");
					prosumer.price_version = Double.parseDouble(result[0]);
					prosumer.p_b = Double.parseDouble(result[1]);
					prosumer.p_s = Double.parseDouble(result[2]);
					prosumer.isConverge = result[3];
				}
				if (prosumer.isConverge.equals("true")){
					endtime = LocalDateTime.now();
					System.out.println("The final price is : p_b= "+prosumer.p_b+", p_s = "+ prosumer.p_s);
					System.out.println("The time is : p_out= "+starttime+"--"+endtime);
					break;
				}
				if (prosumer.price_version >= prosumer.bid_version) {
					if (prosumer.bid_version == 0 && isOffloading == false){
						prosumer.Submit_first_bid(fabClient, ccid, channelClient);
					}else {
						try{
						prosumer.updateBid(fabClient, ccid, channelClient);
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
					prosumer.bid_version += 1;
					Thread.sleep(500);     //This is Sleep 2 =5000+sleep 1

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
		System.out.println("the current E_import is:"+E_import);
		TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
		request.setChaincodeID(ccid);
		request.setFcn("updateBid");
		String[] arguments = {Config_dynamic.USER_NAME, Double.toString(E_import)};
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
			String[] params = new String[] {"python3","liboptpy/Bid_prosumer.py", String.valueOf(E_import), String.valueOf(E_max), String.valueOf(p_b),String.valueOf(p_s),String.valueOf(load_d),String.valueOf(k),String.valueOf(base_d),String.valueOf(E_g)};
			System.out.println("the passing cmd is:"+params[0]+params[1]+params[2]+params[3]);
			Process process = Runtime.getRuntime().exec(params);
			int exitValue = process.waitFor();

			// boolean exitValue = process.waitFor(200, TimeUnit.SECONDS);
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
				if (s.length()>=8) {
					System.out.println(s.substring(0,7));
					if (s.substring (0, 7).equals ("x_next=")) {
						temp = s.substring (7);
						System.out.println (temp);
						load_d = Double.parseDouble (temp);
						System.out.println (load_d);
					}
				}

			}

			// Read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace(); }

		//load_d = 5;
		//ml_eng.feval("EndUserApp",E_import,E_max,p_b,p_s,load_d,k,base_d,E_g);
		E_import = E_g-(load_d + base_d);
		System.out.println("updated load_d is:"+load_d);
		System.out.println("updated E_import is:"+E_import);
		TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
		request.setChaincodeID(ccid);
		request.setFcn("updateBid");
		String[] arguments = {Config_dynamic.USER_NAME, Double.toString(E_import)};
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
			System.out.println("the passing cmd is:python3 liboptpy/Offload_prosumer.py");
			Process process = Runtime.getRuntime().exec("python3 liboptpy/Offload_prosumer.py");
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
				if(s.length()>=10) {
					System.out.println("s.length()>=10, s = " + s.substring(0,9));
					if (s.substring(0,9).equals("converged")){
						temp = s.substring(17);
						load_d = Double.parseDouble(temp);
						System.out.println("load_d="+load_d);
					}
				}
			}
			E_import = E_g-(load_d + base_d);
			// Read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
			System.out.println("the final E_import = "+E_import);

		} catch (IOException e) { e.printStackTrace();
		} catch (InterruptedException e) { e.printStackTrace(); }


	}


}
