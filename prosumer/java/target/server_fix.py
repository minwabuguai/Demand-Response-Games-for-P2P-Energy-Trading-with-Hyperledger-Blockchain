# ----- An example UDP client in Python that uses recvfrom() method -----

import socket

import random
import xlrd
from configparser import ConfigParser


config = ConfigParser()
config.read('config.ini')

# read values from a section
user_total = 203  #14

retailer_bid_sum = 0.0
retailer_num = 0
buyer_bid_sum = 0.0
buyer_num = 0
seller_bid_sum = 0.0
seller_num = 0
p = [0.0, 0.0, 0.0]
lambda_a = 0.99
mu_b = 0.53
p_old = p[0]
theta = 0.00001

def Pricing_model ():
    global p
    global lambda_a
    global mu_b
    global retailer_bid_sum
    global buyer_bid_sum
    global seller_bid_sum
    global retailer_num
    global buyer_num
    global seller_num

    #print("buyer_bid_sum = "+str(buyer_bid_sum))
    #print("seller_bid_sum = " + str(seller_bid_sum))
    #print("retailer_bid_sum = "+ str(retailer_bid_sum))
    p_out = abs (buyer_bid_sum + seller_bid_sum) / retailer_bid_sum

    p_b = lambda_a * p_out
    p_s = mu_b * p_out
    p = [p_out, p_b, p_s]

    buyer_bid_sum = 0.0
    seller_bid_sum = 0.0
    retailer_bid_sum = 0.0

    return p


if __name__ == "__main__":
    print("user_total="+str(user_total))
    server = socket.socket (socket.AF_INET, socket.SOCK_DGRAM)
    server.setsockopt (socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

    server.bind(("", 4141))
    iteration_count = 0
    while(True):
        BidAndAddr = server.recvfrom(1024)

        bid = BidAndAddr[0].decode()
	sender = BidAndAddr[1]
        #print(bid)
	#print("sender:"+str(sender))

        bidSplit = bid.split()

        role = bidSplit[0]

        #print("retailer_num="+str(retailer_num)+"buyer_num="+str(buyer_num)+"seller_num"+str(seller_num))

        #server.sendto(bid.encode(), BidAndAddr[1])
        #print(role)
        if role == "retailer":
            retailer_bid_sum += float(bidSplit[1])
            retailer_num += 1
            #server.sendto (str(retailer_bid_sum).encode (), BidAndAddr [1])
        elif role == "buyer":
            buyer_bid_sum += float(bidSplit[1])
            buyer_num += 1
            #server.sendto (str(buyer_bid_sum).encode (), BidAndAddr [1])
        elif role == "seller":
            seller_bid_sum += float(bidSplit[1])
            seller_num += 1
            #server.sendto (str(seller_bid_sum).encode (), BidAndAddr [1])

        if retailer_num+buyer_num+seller_num==user_total:
            p=Pricing_model()
            iteration_count +=1
            print ("p_old and p_out_new are:" + str (p_old) + " " + str (p [0]))

            if iteration_count >=28:
                message = "converged"+",p_out="+str(p[0])
                print(message)
                server.sendto ( message.encode ( ), ('<broadcast>', 37020))
                server.sendto ( message.encode ( ), ('<broadcast>', 37021) )
                server.sendto ( message.encode ( ), ('<broadcast>', 37022) )
                server.sendto ( message.encode ( ), ('<broadcast>', 37023) )

                break;
            else:
                p2send = str(p[0])+" "+str(p[1])+" "+str(p[2])
                server.sendto ( p2send.encode ( ), ('<broadcast>', 37020))
                server.sendto ( p2send.encode ( ), ('<broadcast>', 37021) )
                server.sendto ( p2send.encode ( ), ('<broadcast>', 37022) )
                server.sendto ( p2send.encode ( ), ('<broadcast>', 37023) )

                #print ("message sent!"+p2send)
                retailer_num = 0
                buyer_num = 0
                seller_num = 0
                p_old = p[0]
