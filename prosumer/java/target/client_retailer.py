import socket
import numpy as np
from oct2py import octave
import xlrd
from configparser import ConfigParser


config = ConfigParser()
config.read('config.ini')

# read values from a section
sheet_num = int(config.get('section_1','SHEET_NUM'))   #14
line_num = int(config.get('section_1','LINE_NUM'))
role = str(config.get('section_1','ROLE')) #retailer
print(sheet_num)
print(role)
wb = xlrd.open_workbook ("data_final.xlsm")
sheet = wb.sheet_by_index (sheet_num)

a0=float(sheet.cell_value (line_num, 1))
a1=float(sheet.cell_value (line_num, 2))
a2=float(sheet.cell_value (line_num, 3))
gamma=float(sheet.cell_value (line_num, 4))
bid=float(sheet.cell_value (line_num, 5))
bit_count = 1
p_out = 0.0

if __name__ == "__main__":
    severAddr = ("172.24.35.243", 4141) #172.24.61.226
    client = socket.socket (socket.AF_INET, socket.SOCK_DGRAM)
    client.setsockopt (socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    client.bind (("", 37020))

    while True:
        print ("bid_old = " + str (bid))
        if bit_count ==1:
            bid = bid
        else:
            bid = octave.feval('retailerApp',bid,a0,a1,a2,gamma,p_out)
        print("bid_new = "+str(bid))
        bid2send = role + " " + str (bid)
        client.sendto (bid2send.encode ( ), severAddr)
        data, addr = client.recvfrom (1024)
        print ("received message: %s" % data)

        p = data.decode()
        if p[:9] == "converged":
            print (p[:9]+",bid="+str(bid))
            break
        else:
            p_split = p.split ( )
            p_out = float(p_split[0])
            print("p_out = "+str(p_out))

