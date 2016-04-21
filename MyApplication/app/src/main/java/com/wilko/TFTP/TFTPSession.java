/*
 * tftpSession.java
 *
 * Created on 18 October 2000, 22:02
 */

package com.wilko.TFTP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.*;
import java.io.*;

/** TFTPSession implements the <B>TFTP</B> protocol as defined in RFC1350.
 * Change log:
 * <PRE>$Log: TFTPSession.java,v $
 * <PRE>Revision 1.1.1.1  2000/10/29 04:08:26  wilko
 * <PRE>Initial Import
 * <PRE></PRE>
 * Copyright (C) 2000 Paul Wilkinson (wilko@sourceforge.net)
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * See also: {@link <a href="http://www.gnu.org/copyleft/gpl.html">http://www.gnu.org/copyleft/gpl.html</a>}
 * @author $Author: wilko $
 * @version $Revision: 1.1.1.1 $
 * @see "RFC 1350"
 */
public class TFTPSession extends Object {

    /** The socket used to communicate with the TFTP Server
     */
    //private DatagramSocket socket;
    private MulticastSocket socket;
    /** The host we are connecting to
     */
    private String host;
    /** my port number
     */
    private int myTID;
    /** the server's port number
     */
    private int theirTID;
    /** The resolved Internet Address for the server
     */
    private InetAddress them;
    /** The last packet we sent.  Used for retry/timeout
     */
    private DatagramPacket lastPacket;
    /** The low byte of the current block number
     */
    private byte lowBlock;
    /** The high byte of the current block number
     */
    private byte highBlock;
    /** The low byte of the next block to acknowledge
     */
    private byte ackLow;
    /** The high byte of the next block to acknowledge
     */
    private byte ackHigh;
    /** The current block number
     */
    private int blockNo;
    /** The recv/transmit buffer
     */
    private byte recvbuf[];
    /** Is this session in "reading" mode?
     */
    private boolean reading;
    /** Is this session in "writing" mode
     */
    private boolean writing;

    /** Specifies that this TFTP shuld take place in "octet" mode
     */
    public static final String OCTET_MODE="octet";
    /** Specifies that this TFTP transfer should take place in "netascii" mode
     */
    public static final String NETASCII_MODE="netascii";

    /** String descriptions of the RFC1350 defined error codes
     */
    public static final String ERRORDESC[] = {
        "Undefined Error",
        "File not found.",
        "Access Violation.",
        "Disk full or allocation Exceeded.",
        "Illegal TFTP operation.",
        "Unknown Transfer ID.",
        "File already exists.",
        "No such user."
    };
        /** The well known port of the TFTP service
         */
    public static int TFTP_PORT=30000;//69; modplace
        /** How long is considered a timeout?
         */
    private static final int TIMEOUT=5000;
        /** How many times should we retry before giving up?
         */
    private static final int RETRY_COUNT=3;
        /** The opcode for read as defined in {@link "RFC1350"}
         */
    private static final byte OPCODE_READ=1;
        /** The opcode for write as defined in {@link "RFC1350"}
         */
    private static final byte OPCODE_WRITE=2;
        /** The opcode for a data block as defined in {@link "RFC1350"}
         */
    private static final byte OPCODE_DATA=3;
        /** The opcode for an acknowledge as defined in {@link "RFC1350"}
         */
    private static final byte OPCODE_ACK=4;
        /** The opcode for an error as defined in {@link "RFC1350"}
         */
    private static final byte OPCODE_ERROR=5;

    /** Creates new TFTPSession connected to the specified host.
     * @param host Hostname to connect to.  May be either a domain name or a dotted quad IP address
     * @throws IOException If <I>host</I> cannot be resolved or a network error occurs
     */
    public TFTPSession(String host) throws IOException
    {
        this.host=host;
        lowBlock=1;
        ackLow=1;
        highBlock=0;
        ackHigh=0;
        blockNo=0;
        myTID=theirTID=0;
        reading=false;
        writing=false;
        recvbuf = new byte[516];

        try {
            //socket = new DatagramSocket(); modplace
            them=InetAddress.getByName(host);
            socket = new MulticastSocket(TFTP_PORT);//生成套接字并绑定30001端口
            socket.joinGroup(them);//加入多播组，发送方和接受方处于同一组时，接收方可抓取多播报文信息
            socket.setTimeToLive(4);//设定TTL

            socket.setSoTimeout(TIMEOUT);
            myTID=socket.getLocalPort();
        }
        catch (UnknownHostException e)
        {
            throw (new IOException(e.toString()));
        }

        catch (SocketException e)
        {
            throw (new IOException(e.toString()));
        }
    }

    /** Initiates a read connection to the host specified at construction time
     * @param fileName The file to read from the host. This cannot contain a path as TFTP does not support subdirectories
     * @param mode The mode for this transfer, either {@link #OCTET_MODE} or {@link #NETASCII_MODE}
     * @throws IOException if {@link #initiateRead(String, String)} or {@link #initiateWrite(String, String)} has already been called
     * or a network error occurs
     */
    public void initiateRead(String fileName,String mode) throws IOException
    {
        byte buf[]=new byte[4+fileName.length()+mode.length()];
        byte temp[];
        int j=2;

        if (writing)
        {
            throw (new IOException("Already in write mode"));
        }

        if (reading)
        {
            throw (new IOException("Already in read mode"));
        }

        reading=true;

        buf[0]=0;
        buf[1]=OPCODE_READ;
        temp=fileName.getBytes();
        for (int i=0;i<temp.length;i++)
        {
            buf[j++]=temp[i];
        }
        buf[j++]=0;
        temp=mode.getBytes();
        for (int i=0;i<temp.length;i++)
        {
            buf[j++]=temp[i];
        }
        buf[j]=0;

        lastPacket = new DatagramPacket(buf,buf.length,them,TFTP_PORT);
        socket.send(lastPacket);
    }

    /** Initates a Write connection to the host specified at construction time
     * @param fileName The filename to write
     * @param mode The mode to use while writing this file.  Specify either {@link #OCTET_MODE} or {@link #NETASCII_MODE}
     * @throws IOException if {@link #initiateRead(String, String)} or {@link #initiateWrite(String, String)} has already been called
     * or a network error occurs
     */
    public void initiateWrite(String fileName,String mode) throws IOException
    {
        byte buf[]=new byte[4+fileName.length()+mode.length()];
        byte temp[];
        int j=2;
        
        lowBlock=0;
        ackLow=0;

        if (writing)
        {
            throw (new IOException("Already in write mode"));
        }

        if (reading)
        {
            throw (new IOException("Already in read mode"));
        }

        writing=true;

        buf[0]=0;
        buf[1]=OPCODE_WRITE;
        temp=fileName.getBytes();
        for (int i=0;i<temp.length;i++)
        {
            buf[j++]=temp[i];
        }
        buf[j++]=0;
        temp=mode.getBytes();
        for (int i=0;i<temp.length;i++)
        {
            buf[j++]=temp[i];
        }
        buf[j]=0;

        lastPacket = new DatagramPacket(buf,buf.length,them,TFTP_PORT);
        System.out.println("Sending write request");
        socket.send(lastPacket);
    }

    /** Send an ACK packet back to the server
     * @throws IOException if a network error occurs
     */
    private void sendAck() throws IOException
    {
        byte buf[] = new byte[4];
        buf[0]=0;
        buf[1]=OPCODE_ACK;
        buf[2]=ackHigh;
        buf[3]=ackLow;
        lastPacket = new DatagramPacket(buf,buf.length,them,theirTID);
        socket.send(lastPacket);
    }

    /** Send an error packet to the specified server
     * @param errorCode The Error Code to send
     * @param dest The Destination address
     * @param destPort The destination port
     * @throws IOException if a network error occurs
     */
    private void sendError(int errorCode,InetAddress dest,int destPort) throws IOException
    {
        int i;
        byte buf[] = new byte[5+ERRORDESC[errorCode].length()];
        buf[0]=0;
        buf[1]=OPCODE_ERROR;
        buf[2]=0;
        buf[3]=(byte)(0xff&errorCode);
        byte temp[]=ERRORDESC[errorCode].getBytes();
        for (i=0;i<temp.length;i++)
        {
            buf[i+4]=temp[i];
        }
        buf[i+4]=0;

        DatagramPacket p = new DatagramPacket(buf,buf.length,dest,destPort);

        socket.send(p);
    }

    /** Sends a block of data to be written to the file on the server
     * @param b The bytes to be sent
     * @param len The number of bytes to send
     * @throws IOException If a network error occurs
     */
    private void sendBlock(byte b[],int len) throws IOException
    {
     
        recvbuf[0]=0;
        recvbuf[1]=OPCODE_DATA;
        recvbuf[2]=highBlock;
        recvbuf[3]=lowBlock;
        
        System.arraycopy(b,0,recvbuf,4,len);
        lastPacket = new DatagramPacket(recvbuf,len+4,them,theirTID);
        socket.send(lastPacket);
    }
        
    
    /** Write bytes to the TFTP Server
     * @param b Contains the bytes to write
     *
     * @param len The number of bytes to write
     * @throws IOException if a network error occurs
     */
    public void writeBlock(byte b[],int len) throws IOException
    {
        for (int i=0;i<RETRY_COUNT;i++)
        {
            try 
            {
                DatagramPacket rp = new DatagramPacket(recvbuf,516);

                socket.receive(rp);
                if (theirTID==0)
                {
                    theirTID=rp.getPort();
                }
                else if (theirTID != rp.getPort())
                {
                    sendError(4,rp.getAddress(),rp.getPort());
                    socket.receive(rp);
                }
                
                byte data[]=rp.getData();
                if (rp.getLength() > 2)     // Did we at least get an Opcode?
                {
                    if (data[1]==OPCODE_ACK)
                    {
                        if ((data[2] == highBlock) &&(data[3]==lowBlock))
                        {
                            blockNo++;
                            if (lowBlock == (byte) 0xff)
                            {
                                lowBlock=0;
                                if (highBlock == (byte)0xff)
                                {
                                    highBlock=0;
                                }
                                else
                                {
                                    highBlock++;
                                }
                            }
                            else
                            {
                                lowBlock++;
                            }
                            sendBlock(b,len);      
                            return;
                        }
                        else
                        {
                            // System.out.println("Received incorrect block");
                        }
                    }
                    else if (data[1]==OPCODE_ERROR)
                    {
                        throw (new IOException("TFTP Error "+ERRORDESC[data[3]]+" "+(new String(data,4,data.length-5)))); 
                    }
                    else 
                    {
                        throw (new IOException("Unexpected OPCODE"));
                    }
                }
                else
                {
                    throw (new IOException("Invalid packet length received"));
                }
            }
            catch (InterruptedIOException e)
            {
                socket.send(lastPacket);
            }
        }
        throw (new IOException("Timeout from remote host"));
    }

    /** Read the next available block from the TFTP Server
     * @throws IOException If a network error occurs
     * @return the bytes read from the server. If the number of bytes read is less than 512 then this is the last packet.  0 bytes may be returned
     */
    public byte[] readBlock() throws IOException
    {
        for (int i=0;i<RETRY_COUNT;i++)
        {
            try
            {

                DatagramPacket rp= new DatagramPacket(recvbuf,516);
                socket.receive(rp);
                if (theirTID == 0)
                {
                    theirTID=rp.getPort();
                }
                else if (theirTID != rp.getPort())
                {
                    sendError(4,rp.getAddress(),rp.getPort());
                    socket.receive(rp);
                }

                byte data[]=rp.getData();
                if (rp.getLength()> 2)    // Make sure we at least got an opcode
                {
                    if (data[1]==OPCODE_DATA)
                    {
                        if ((data[2] == highBlock)&&(data[3]==lowBlock))
                        {
                            blockNo++;
                            if (lowBlock == (byte)0xff)
                            {
                                lowBlock=0;
                                if (highBlock == (byte)0xff)
                                {
                                    highBlock=0;
                                }
                                else
                                {
                                    highBlock++;
                                }
                            }
                            else
                            {
                                lowBlock++;
                            }

                            byte retData[]=new byte[rp.getLength()-4];
                            System.arraycopy(data,4,retData,0,retData.length);
                            /*for (int j=0;j<retData.length;j++)
                            {
                            retData[j]=data[j+4];
                            }*/
                            sendAck();
                            ackLow=lowBlock;
                            ackHigh=highBlock;
                            return(retData);
                        }
                    }
                    else if (data[1]==OPCODE_ERROR)
                    {
                        throw (new IOException("TFTP Error "+ERRORDESC[data[3]]+" "+(new String(data,4,data.length-5))));
                    }
                    else
                    {
                        throw (new IOException("Unexpected OPCODE"));
                    }
                }
                else
                {
                    throw (new IOException("Invalid packet length received"));
                }
            }
            catch (InterruptedIOException e)
            {
                socket.send(lastPacket);
            }
        }
        throw (new IOException("Timeout from remote host"));
    }
}

