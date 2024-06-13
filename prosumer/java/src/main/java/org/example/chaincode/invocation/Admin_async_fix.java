package org.example.chaincode.invocation;

        import org.example.client.CAClient;
        import org.example.client.ChannelClient;
        import org.example.client.FabricClient;
        import org.example.config.Config;
        import org.example.config.Config_dynamic;
        import org.example.user.UserContext;
        import org.hyperledger.fabric.sdk.*;

        import java.util.Collection;
        import java.util.HashMap;
        import java.util.Map;
        import java.util.logging.Level;
        import java.util.logging.Logger;

        import static java.nio.charset.StandardCharsets.UTF_8;

public class Admin_async_fix {

    private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
    private static final String EXPECTED_EVENT_NAME = "event";
    public static boolean isRun = true;
    public static int retries = 0;


    public static void main(String args[]) {
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

            Thread.sleep(6 * 1000);

            while (isRun == true) {
                try {
                    String[] args2 = {""};
                    TransactionProposalRequest request = fabClient.getInstance ().newTransactionProposalRequest ();
                    ChaincodeID ccid = ChaincodeID.newBuilder ().setName (Config.CHAINCODE_1_NAME).build ();
                    request.setChaincodeID (ccid);
                    request.setFcn ("PricingModel");
                    request.setArgs (args2);
                    request.setProposalWaitTime (2000);
                    Map <String, byte[]> tm2 = new HashMap <> ();
                    tm2.put ("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes (UTF_8)); // Just some extra junk
                    // in transient map
                    tm2.put ("method", "TransactionProposalRequest".getBytes (UTF_8)); // ditto
                    tm2.put ("result", ":)".getBytes (UTF_8)); // This should be returned see chaincode why.
                    tm2.put (EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA); // This should trigger an event see chaincode why.
                    request.setTransientMap (tm2);
                    Collection <ProposalResponse> responses2Pricing = channelClient.sendTransactionProposal (request);

                    System.out.println (responses2Pricing);
                    for (ProposalResponse pres : responses2Pricing) {
                        String stringResponse2 = new String (pres.getChaincodeActionResponsePayload ());
                        System.out.println ("pricing_model Response:" + stringResponse2);
                    }
                }catch(Exception e) {
                    if (retries < 5000) {
                        retries+=1;
                        System.out.println ("retry time=" + retries);
                        continue;
                    } else {
                        throw e;
                    }
                }

                Thread.sleep(5000);


                String[] arg2 = {"price_version"};
                Collection<ProposalResponse> responsesQuery0 = channelClient.queryByChainCode(Config.CHAINCODE_1_NAME, "Query", arg2);
                for (ProposalResponse pres : responsesQuery0) {
                    String stringResponse2 = new String (pres.getChaincodeActionResponsePayload ());
                    System.out.println ("query Response:" + stringResponse2);
                    if (stringResponse2.equals ("28")) {
                        System.out.println ("28 times reach");

                        isRun = false;
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
