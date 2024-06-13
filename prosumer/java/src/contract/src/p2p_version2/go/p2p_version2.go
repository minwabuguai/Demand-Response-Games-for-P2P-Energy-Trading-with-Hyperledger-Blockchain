 
package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"strconv"
	"strings"
	"math"

	//"time"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

// SimpleChaincode example simple Chaincode implementation
type SimpleChaincode struct {
}

type user struct {
	Version int `json:"Version"`
	ObjectType string `json:"docType"` //docType is used to distinguish the various types of objects in state database
	Name       string `json:"name"`    //the fieldtags are needed to keep case from bouncing around
	Bid       float64   `json:"bid"`
	Role      string `json:"role"`
}

// ===================================================================================
// Main
// ===================================================================================
func main() {
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		fmt.Printf("Error starting Simple chaincode: %s", err)
	}
}

// Init initializes chaincode
// ===========================
func (t *SimpleChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	return shim.Success(nil)
}

// Invoke - Our entry point for Invocations
// ========================================
func (t *SimpleChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	function, args := stub.GetFunctionAndParameters()
	fmt.Println("invoke is running " + function)

	// Handle different functions
	if function == "initAuction" { //create a new marble
		return t.initAuction(stub, args)
	} else if function == "initUser" { //
		return t.initUser(stub, args)
	}else if function == "updateBid" { //
		return t.updateBid(stub, args)
	}else if function == "Check_State" { //
		return t.Check_State(stub, args)
	}else if function == "Query" { //
		return t.Query(stub, args)
	} else if function == "PricingModel" { //
		return t.PricingModel(stub, args)
	}else if function == "SubmitPrice" { //
		return t.SubmitPrice(stub, args)
	}
	fmt.Println("invoke did not find func: " + function) //error
	return shim.Error("Received unknown function invocation")
}




// ============================================================
// initAuction - initiation parameters that is used in auction
// ============================================================
func (t *SimpleChaincode) initAuction(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	var err error

	//   0       1       2           3                         4           5       6     7    
	// "p_out", "p_s", "p_b"   "prosumerNum_total"   retailerNum_total    lambda   mu    theta   
	if len(args) != 8 {
		return shim.Error("Incorrect number of arguments. Expecting 7")
	}

	// ==== Input sanitation ====
	fmt.Println("- start init Auction new")
	if len(args[0]) <= 0 {
		return shim.Error("1st argument must be a non-empty string")
	}
	if len(args[1]) <= 0 {
		return shim.Error("2nd argument must be a non-empty string")
	}
	if len(args[2]) <= 0 {
		return shim.Error("3rd argument must be a non-empty string")
	}
	if len(args[3]) <= 0 {
		return shim.Error("4rd argument must be a non-empty string")
	}
	if len(args[4]) <= 0 {
		return shim.Error("5rd argument must be a non-empty string")
	}
	if len(args[5]) <= 0 {
		return shim.Error("6rd argument must be a non-empty string")
	}
	if len(args[6]) <= 0 {
		return shim.Error("7rd argument must be a non-empty string")
	}
	if len(args[7]) <= 0 {
		return shim.Error("7rd argument must be a non-empty string")
	}	
	// === Save initial prices to state ===
	err = stub.PutState("p_out", []byte(args[0]))  //only updated by the pricing model
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("p_s", []byte(args[1]))  //only updated by the pricing model
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("p_b", []byte(args[2]))  //only updated by the pricing model
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("prosumerNum_total", []byte(args[3]))  //only be read
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("retailerNum_total", []byte(args[4]))    //only be read
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("lambda", []byte(args[5]))	//only be read by pricing model
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("mu", []byte(args[6]))    //only be read by pricing model
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("theta", []byte(args[7]))    //only be read by pricing model
	if err != nil {
		return shim.Error(err.Error())
	}

// initiate some parameters as 0
	err = stub.PutState("prosumer_num", []byte("0"))   //This is a buggy key!!!!!
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("buyer", []byte("0"))	//??????? don't delete it
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("seller", []byte("0"))	//???????? don't delete it
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("retailer_num", []byte("0"))   //This is a buggy key!!!!!
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("buyerBidSum", []byte("0"))    //seems like not been used
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("sellerBidSum", []byte("0"))    //seems like not been used
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("retailerBidSum", []byte("0"))   //seems like not been used
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("price_version", []byte("0"))   //only updated by pricing model
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("current_bid_version", []byte("1"))   //only updated by pricing model
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("isConverge", []byte("0"))		//only updated by pricing model
	if err != nil {
		return shim.Error(err.Error())
	}
	fmt.Println("init isConverge")
	fmt.Println("- end init auction")
	return shim.Success(nil)
}



// ============================================================
// submit bid, store into chaincode state
// ============================================================
func (t *SimpleChaincode) initUser(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	//   0           1        
	// "userName",  "role"
	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}

	userName := strings.ToLower(args[0])
	bid := 0.0
	role := strings.ToLower(args[1])
	version := 0

	// ==== Create userBid object and marshal to JSON ====
	objectType := "userBid"
	user := &user{version, objectType, userName, bid, role}
	fmt.Println("- user.Version is:"+ strconv.Itoa(user.Version))
	userJSONasBytes, err := json.Marshal(user)
	if err != nil {
		return shim.Error(err.Error())
	}

	// === Save user to state ===
	err = stub.PutState(userName, userJSONasBytes)
	if err != nil {
		return shim.Error(err.Error())
	}

	//====  Index the role~name =====
	indexName := "Role~Name"
	RoleNameIndexKey, err := stub.CreateCompositeKey(indexName, []string{user.Role, user.Name})
	if err != nil {
		return shim.Error(err.Error())
	}
	//  Save index entry to state. Only the key name is needed, no need to store a duplicate copy of the marble.
	//  Note - passing a 'nil' value will effectively delete the key from state, therefore we pass null character as value 
	stub.PutState(RoleNameIndexKey, []byte{0x00})

	return shim.Success(nil)
}


func (t *SimpleChaincode) updateBid(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	var err error

	//   0            1        
	// "userName", "new bid"
	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2")
	}

	// ==== Input sanitation ====
	fmt.Println("- start update bid for user:" + args[0])
	if len(args[0]) <= 0 {
		return shim.Error("1st argument must be a non-empty string")
	}
	if len(args[1]) <= 0 {
		return shim.Error("2nd argument must be a non-empty string")
	}

	userAsBytes, err := stub.GetState(args[0])
	if err != nil {
		return shim.Error("Failed to get value:" + err.Error())
	} else if userAsBytes == nil {
		return shim.Error("asset does not exist")
	}

	target := user{}

	json.Unmarshal(userAsBytes, &target)
	fmt.Println("- Unmarshal userAsBytes")

    //update the bid and bid_version
	target.Bid, err = strconv.ParseFloat(args[1],64)
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}
	target.Version += 1
	fmt.Println("- updated target.Name: "+ target.Name)
	fmt.Println("- updated target.Version: "+ strconv.Itoa(target.Version))

	userAsBytes, err = json.Marshal(target)
	err = stub.PutState(args[0], userAsBytes)
	fmt.Println("- run Putstate: "+ args[0])

	if err != nil {
		return shim.Error(err.Error())
	}
	fmt.Println("- Marshal target")


	return shim.Success(nil)
}

func (t *SimpleChaincode) Check_State(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	//count how many users has update their bids, return this value

	startKey := "user_buyer_1"
	endKey := "user_seller_99999"

	retailerIterator, err := stub.GetStateByRange(startKey, endKey)
	if err != nil {
		return shim.Error(err.Error())
	}
	defer retailerIterator.Close()

	// buffer is a JSON array containing QueryResults
	var buffer bytes.Buffer

	var num_count int = 0
	//current_bid_version, err := strconv.Atoi(args[0])
	current_bid_version, err := stub.GetState("current_bid_version")
		if err != nil {
			return shim.Error("Error:Failed to get state")
		} else if current_bid_version == nil {
			return shim.Error("Error key does not exist")
		}
	int_current_bid_version, err := strconv.Atoi(string(current_bid_version))
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}


	target := user{}

	for retailerIterator.HasNext() {
		retailerResponse, err := retailerIterator.Next()
		if err != nil {
			return shim.Error(err.Error())
		}
		json.Unmarshal(retailerResponse.Value, &target)

		fmt.Println("target.verion is:"+ strconv.Itoa(target.Version))  // 难道不能小写？？？

		if target.Version == int_current_bid_version{
			num_count += 1
		}

		fmt.Println("num_count is:"+strconv.Itoa(num_count))
	}

	//invoke the PricingModel function if enough bids from users
	prosumerNum_total, err := stub.GetState("prosumerNum_total")
	if err != nil {
		return shim.Error("Failed to get value:" + err.Error())
	} else if prosumerNum_total == nil {
		return shim.Error("asset does not exist")
	}
	IntProsumerNum_total, err := strconv.Atoi(string(prosumerNum_total))
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}
	retailerNum_total, err := stub.GetState("retailerNum_total")
	if err != nil {
		return shim.Error("Failed to get value:" + err.Error())
	} else if retailerNum_total == nil {
		return shim.Error("asset does not exist")
	}
	IntRetailerNum_total, err := strconv.Atoi(string(retailerNum_total))
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}
	pass_args := []string{}
	if num_count >= (IntProsumerNum_total + IntRetailerNum_total){
		return t.PricingModel(stub, pass_args)
	}


	buffer.WriteString(strconv.Itoa(num_count))
	return shim.Success(buffer.Bytes())
}

func (t *SimpleChaincode) Query(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	//   1      
	//  p_s
	fmt.Println("Running inside query and the length is:"+strconv.Itoa(len(args)))
	if len(args) == 1{
		value, err := stub.GetState(args[0])
		if err != nil {
			return shim.Error("Error:Failed to get state")
		} else if value == nil {
			return shim.Error("Error key does not exist")
		}
		return shim.Success(value)
	} else if len(args) == 2{
		var value bytes.Buffer
		result1, err1 := stub.GetState(args[0])
		if err1 != nil {
			return shim.Error("Error:Failed to get state")
		} else if result1 == nil {
			return shim.Error("Error key does not exist")
		}
		value.WriteString(string(result1)+",")
		result2, err2 := stub.GetState(args[1])
		if err2 != nil {
			return shim.Error("Error:Failed to get state")
		} else if result2 == nil {
			return shim.Error("Error key does not exist")
		}
		value.WriteString(string(result2))
		return shim.Success(value.Bytes())
	}else if len(args) == 3{
		var value bytes.Buffer
		result1, err1 := stub.GetState(args[0])
		if err1 != nil {
			return shim.Error("Error:Failed to get state")
		} else if result1 == nil {
			return shim.Error("Error key does not exist")
		}
		value.WriteString(string(result1)+",")
		result2, err2 := stub.GetState(args[1])
		if err2 != nil {
			return shim.Error("Error:Failed to get state")
		} else if result2 == nil {
			return shim.Error("Error key does not exist")
		}
		value.WriteString(string(result2)+",")
		result3, err3 := stub.GetState(args[2])
		if err3 != nil {
			return shim.Error("Error:Failed to get state")
		} else if result3 == nil {
			return shim.Error("Error key does not exist")
		}
		value.WriteString(string(result3))
		return shim.Success(value.Bytes())
	} else if len(args) == 4{
		var value bytes.Buffer
		result1, err1 := stub.GetState(args[0])
		if err1 != nil {
			return shim.Error("Error:Failed to get state")
		} else if result1 == nil {
			return shim.Error("Error key does not exist")
		}
		value.WriteString(string(result1)+",")
		result2, err2 := stub.GetState(args[1])
		if err2 != nil {
			return shim.Error("Error:Failed to get state")
		} else if result2 == nil {
			return shim.Error("Error key does not exist")
		}
		value.WriteString(string(result2)+",")
		result3, err3 := stub.GetState(args[2])
		if err3 != nil {
			return shim.Error("Error:Failed to get state")
		} else if result3 == nil {
			return shim.Error("Error key does not exist")
		}
		value.WriteString(string(result3)+",")
		result4, err4 := stub.GetState(args[3])
		if err4 != nil {
			return shim.Error("Error:Failed to get state")
		} else if result4 == nil {
			return shim.Error("Error key does not exist")
		}
		value.WriteString(string(result4))
		return shim.Success(value.Bytes())
	} else {
		return shim.Error("Wrong number of queried variable")
	}
}




func (t *SimpleChaincode) PricingModel(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	//get the value for different roles
	// Query the role~name index by role
	// This will execute a key range query on all keys starting with 'role'
	buyerResultsIterator, err := stub.GetStateByPartialCompositeKey("Role~Name", []string{"buyer"})
	if err != nil {
		return shim.Error(err.Error())
	}
	defer buyerResultsIterator.Close()

	var i int
	fBuyerBidSum := 0.0
	for i = 0; buyerResultsIterator.HasNext(); i++ {
		// Note that we don't get the value (2nd return variable), we'll just get the name from the composite key
		responseRange, err := buyerResultsIterator.Next()
		if err != nil {
			return shim.Error(err.Error())
		}
		// get the role and name from role~name composite key
		objectType, compositeKeyParts, err := stub.SplitCompositeKey(responseRange.Key)
		if err != nil {
			return shim.Error(err.Error())
		}
		returnedRole := compositeKeyParts[0]
		returnedName := compositeKeyParts[1]

		userAsBytes, err := stub.GetState(returnedName)
		if err != nil {
			return shim.Error("Failed to get user:" + err.Error())
		} else if userAsBytes == nil {
			return shim.Error("user does not exist")
		}
		target := user{}
		err = json.Unmarshal(userAsBytes, &target) //unmarshal it aka JSON.parse()
		if err != nil {
			return shim.Error(err.Error())
		}
		fBuyerBidSum += target.Bid //change the owner

		fmt.Printf("--index:%s role:%s name:%s fBuyerBidSum: %f\n", objectType, returnedRole, returnedName, fBuyerBidSum)
	}

	sellerResultsIterator, err := stub.GetStateByPartialCompositeKey("Role~Name", []string{"seller"})
	if err != nil {
		return shim.Error(err.Error())
	}
	defer buyerResultsIterator.Close()

	fSellerBidSum := 0.0
	for i = 0; sellerResultsIterator.HasNext(); i++ {
		// Note that we don't get the value (2nd return variable), we'll just get the name from the composite key
		responseRange, err := sellerResultsIterator.Next()
		if err != nil {
			return shim.Error(err.Error())
		}
		// get the role and name from role~name composite key
		objectType, compositeKeyParts, err := stub.SplitCompositeKey(responseRange.Key)
		if err != nil {
			return shim.Error(err.Error())
		}
		returnedRole := compositeKeyParts[0]
		returnedName := compositeKeyParts[1]

		userAsBytes, err := stub.GetState(returnedName)
		if err != nil {
			return shim.Error("Failed to get user:" + err.Error())
		} else if userAsBytes == nil {
			return shim.Error("user does not exist")
		}
		target := user{}
		err = json.Unmarshal(userAsBytes, &target) //unmarshal it aka JSON.parse()
		if err != nil {
			return shim.Error(err.Error())
		}
		fSellerBidSum += target.Bid //change the owner

		fmt.Printf("--index:%s role:%s name:%s fSellerBidSum: %f\n", objectType, returnedRole, returnedName, fSellerBidSum)
	}



	retailerResultsIterator, err := stub.GetStateByPartialCompositeKey("Role~Name", []string{"retailer"})
	if err != nil {
		return shim.Error(err.Error())
	}
	defer retailerResultsIterator.Close()

	fRetailerBidSum := 0.0
	for i = 0; retailerResultsIterator.HasNext(); i++ {
		// Note that we don't get the value (2nd return variable), we'll just get the name from the composite key
		responseRange, err := retailerResultsIterator.Next()
		if err != nil {
			return shim.Error(err.Error())
		}
		// get the role and name from role~name composite key
		objectType, compositeKeyParts, err := stub.SplitCompositeKey(responseRange.Key)
		if err != nil {
			return shim.Error(err.Error())
		}
		returnedRole := compositeKeyParts[0]
		returnedName := compositeKeyParts[1]

		userAsBytes, err := stub.GetState(returnedName)
		if err != nil {
			return shim.Error("Failed to get user:" + err.Error())
		} else if userAsBytes == nil {
			return shim.Error("user does not exist")
		}
		target := user{}
		err = json.Unmarshal(userAsBytes, &target) //unmarshal it aka JSON.parse()
		if err != nil {
			return shim.Error(err.Error())
		}
		fRetailerBidSum += target.Bid //change the owner

		fmt.Printf("--index:%s role:%s name:%s fRetailerBidSum: %f\n", objectType, returnedRole, returnedName, fRetailerBidSum)
	}

	//get the value of lambda
	lambda, err := stub.GetState("lambda")
	if err != nil {
		return shim.Error("Failed to get value:" + err.Error())
	} else if lambda == nil {
		return shim.Error("lambda asset does not exist")
	}
	f_lambda, err := strconv.ParseFloat(string(lambda), 64)
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}

	//get the value of mu
	mu, err := stub.GetState("mu")
	if err != nil {
		return shim.Error("Failed to get value:" + err.Error())
	} else if mu == nil {
		return shim.Error("mu asset does not exist")
	}
	f_mu, err := strconv.ParseFloat(string(mu), 64)
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}

	// get the value of theta
	theta, err := stub.GetState("theta")
	if err != nil {
		return shim.Error("Failed to get value:" + err.Error())
	} else if theta == nil {
		return shim.Error("mu asset does not exist")
	}
	f_theta, err := strconv.ParseFloat(string(theta), 64)
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}

	price_version, err := stub.GetState("price_version")
	if err != nil {
		return shim.Error("Failed to get value:" + err.Error())
	} else if price_version == nil {
		return shim.Error("price_version asset does not exist")
	}
	int_price_version, err := strconv.Atoi(string(price_version))
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}

	current_bid_version, err := stub.GetState("current_bid_version")
	if err != nil {
		return shim.Error("Failed to get value:" + err.Error())
	} else if current_bid_version == nil {
		return shim.Error("price_version asset does not exist")
	}
	int_current_bid_version, err := strconv.Atoi(string(current_bid_version))
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}
	
	//update external retail pricep_out  =======================================================================
	fmt.Println("- start to calculate external price")
	p_out :=  math.Abs(fBuyerBidSum + fSellerBidSum)/fRetailerBidSum   //sellerBidSum must be smaller than buyerBidSum

	fmt.Println("fBuyerBidSum:",fBuyerBidSum)
	fmt.Println("fSellerBidSum:",fSellerBidSum)
	fmt.Println("fRetailerBidSum:",fRetailerBidSum)
	fmt.Println("p_out:",p_out)

	p_out_old, err := stub.GetState("p_out")
	if err != nil {
		return shim.Error("Failed to get value:" + err.Error())
	} else if mu == nil {
		return shim.Error("p_out asset does not exist")
	}
	f_p_out_old, err := strconv.ParseFloat(string(p_out_old), 64)
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}
	fmt.Println("p_out_old:",f_p_out_old)
	
	// check if converge
	if (math.Abs(p_out - f_p_out_old)<=f_theta){
		err = stub.PutState("isConverge", []byte("true"))
		if err != nil {
			return shim.Error(err.Error())
		}
	}
	check_converge := math.Abs(p_out - f_p_out_old)
	fmt.Println("check_converge:",check_converge)
	fmt.Println("f_theta:",f_theta)

	//update internal prices
	fmt.Println("- start to calculate internal prices")
	p_b := f_lambda * p_out
	p_s := f_mu * p_out
	
	int_price_version = int_price_version +1;
	int_current_bid_version = int_current_bid_version + 1
	fmt.Println("p_b:",p_b)
	fmt.Println("int_price_version:",int_price_version)

	err = stub.PutState("p_out", []byte(strconv.FormatFloat(p_out, 'f', -1, 64)))
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("p_b", []byte(strconv.FormatFloat(p_b, 'f', -1, 64)))
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("p_s", []byte(strconv.FormatFloat(p_s, 'f', -1, 64)))
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("price_version", []byte(strconv.Itoa(int_price_version)))
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("current_bid_version", []byte(strconv.Itoa(int_current_bid_version)))
	if err != nil {
		return shim.Error(err.Error())
	}

	var value bytes.Buffer
	value.WriteString("p_out=" + strconv.FormatFloat(p_out, 'f', -1, 64) + ",p_b=" + strconv.FormatFloat(p_b, 'f', -1, 64) + ",p_s=" + strconv.FormatFloat(p_s, 'f', -1, 64)+",price_version=" + strconv.Itoa(int_price_version))
	return shim.Success(value.Bytes())
}


func (t *SimpleChaincode) SubmitPrice(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}
	err := stub.PutState("p_out", []byte(args[0]))
	if err != nil {
		return shim.Error(err.Error())
	}
	
	f_p_out, err := strconv.ParseFloat(args[0],64)
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}


	//get the value of lambda
	lambda, err := stub.GetState("lambda")
	if err != nil {
		return shim.Error("Failed to get value:" + err.Error())
	} else if lambda == nil {
		return shim.Error("lambda asset does not exist")
	}
	f_lambda, err := strconv.ParseFloat(string(lambda), 64)
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}

	//get the value of mu
	mu, err := stub.GetState("mu")
	if err != nil {
		return shim.Error("Failed to get value:" + err.Error())
	} else if mu == nil {
		return shim.Error("mu asset does not exist")
	}
	f_mu, err := strconv.ParseFloat(string(mu), 64)
	if err != nil {
		return shim.Error("argument must be a numeric string")
	}
	
	p_b := f_lambda * f_p_out
	p_s := f_mu * f_p_out
	fmt.Println("p_b:",p_b)

	err = stub.PutState("p_b", []byte(strconv.FormatFloat(p_b, 'f', -1, 64)))
	if err != nil {
		return shim.Error(err.Error())
	}
	err = stub.PutState("p_s", []byte(strconv.FormatFloat(p_s, 'f', -1, 64)))
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success(nil)

}
