
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

package org.example.config;

import java.io.File;

public class Config {
	
	public static final String ORG1_MSP = "Org1MSP";

	public static final String ORG1 = "org1";

	public static final String ORG2_MSP = "Org2MSP";

	public static final String ORG2 = "org2";

	public static final String ADMIN = "admin";

	public static final String ADMIN_PASSWORD = "adminpw";
	
	public static final String CHANNEL_CONFIG_PATH = "/home/ubuntu/java/config/channel.tx";
	
	public static final String ORG1_USR_BASE_PATH = "/home/ubuntu/java/crypto-config/peerOrganizations/org1/users/Admin@org1/msp/";
	
	public static final String ORG2_USR_BASE_PATH = "/home/ubuntu/java/crypto-config/peerOrganizations/org2/users/Admin@org2/msp/";
	
	public static final String ORG1_USR_ADMIN_PK =ORG1_USR_BASE_PATH + File.separator + "keystore";
	public static final String ORG1_USR_ADMIN_CERT = ORG1_USR_BASE_PATH + File.separator + "admincerts";

	public static final String ORG2_USR_ADMIN_PK = ORG2_USR_BASE_PATH + File.separator + "keystore";
	public static final String ORG2_USR_ADMIN_CERT = ORG2_USR_BASE_PATH + File.separator + "admincerts";
	
	public static final String CA_ORG1_URL = "http://172.24.35.181:30000";
	
	public static final String CA_ORG2_URL = "http://172.24.35.179:30100";
	
	public static final String ORDERER_URL = "grpc://172.24.35.181:32000";
	
	public static final String ORDERER_NAME = "orderer0.orgorderer1";
	
	public static final String CHANNEL_NAME = "mychannel";
	
	public static final String ORG1_PEER_0 = "peer0.org1";
	
	public static final String ORG1_PEER_0_URL = "grpc://172.24.35.181:30001";
	
	public static final String ORG1_PEER_1 = "peer1.org1";
	
	public static final String ORG1_PEER_1_URL = "grpc://172.24.35.180:30004";
	
    public static final String ORG2_PEER_0 = "peer0.org2";
	
	public static final String ORG2_PEER_0_URL = "grpc://172.24.35.180:30101";
	
	public static final String ORG2_PEER_1 = "peer1.org2";
	
	public static final String ORG2_PEER_1_URL = "grpc://172.24.35.179:30104";
	
	public static final String CHAINCODE_ROOT_DIR = "../src/contract";
	
	public static final String CHAINCODE_1_NAME = "p2p05";
	
	public static final String CHAINCODE_1_PATH = "p2p_version2/go";
	
	public static final String CHAINCODE_1_VERSION = "1";


}
