/*
 * TFTPOutputStream.java
 *
 * Created on 28 October 2000, 14:18
 */

package com.wilko.TFTP;

import java.io.*;

/** TFTPOutputStream is a sub-class of OutputStream that writes to a <B>TFTP Server</B>.
 * Change log:
 * <PRE>$Log: TFTPOutputStream.java,v $
 * <PRE>Revision 1.1.1.1  2000/10/29 04:08:24  wilko
 * <PRE>Initial Import
 * <PRE></PRE>
 *
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
 * @see com.wilko.TFTP.TFTPSession
 */
public class TFTPOutputStream extends java.io.OutputStream 
{

    /** Creates new TFTPOutputStream */
     private String host;
    /** The name of the file being written
     */
    private String fileName;
    /** The buffer of data to write
     */
    private byte buffer[];
    /** Position in the buffer to store the next byte
     */
    private int bufferPtr;
    /** The underlying TFTP session for this TFTPOutputStream
     */
    private TFTPSession session;
    /** Has End of File been reached?
     */
    private boolean eof;
    /** Has the stream been closed?
     */
    private boolean closed;

    /** Creates a new TFTPOutputStream to read write the specified file to the specified host
     * @param host Name of host to write file to.  May be a domain name or a dotted quad IP address
     * @param fileName The name of the file to write to the host.
     * This name cannot contain directory information
     */
    public TFTPOutputStream(String host,String fileName)
    {
        bufferPtr=0;
        buffer=new byte[512];
        this.host=host;
        this.fileName=fileName;
        session=null;
        eof=false;
        closed=false;
    }
    
    /** Close this output stream
     * @throws IOException If an IO Error occurs
     */
    public void close() throws IOException
    {
        sendNextBlock();
        closed=true;
    }
    
    /** Write the bytes contained in <I>b</I>, starting at offset <I>off</I>, for length <I>len</I>.
     * @param b The bytes to be written
     * @param off The 1st byte within b to be written
     * @param len The number of bytes to be written
     * @throws IOException If an IO Error occurs
     */
    public void write(byte b[],int off, int len) throws IOException
    {
        
        if ((off <0)||(len <0))
        {
            throw(new IndexOutOfBoundsException());
        }
        
        if (b == null)
        {
            throw(new NullPointerException());
        }
        
        int counter=0;
        while ((counter < len)&(!eof))
        {
            if (bufferPtr < buffer.length)
            {
                int length = buffer.length-bufferPtr;
                if (length > len-counter)
                {
                    length=len-counter;
                }
                System.arraycopy(b,off+counter,buffer,bufferPtr,length);
                
                counter+=length;
                bufferPtr+=length;   
            }
            else
            {
                sendNextBlock();
            }
        }
    }
    
    /** Writes the specifed int (cast to a byte) to the Output stream
     * @param b The int(byte) to be written
     * @throws IOException If an IO Error occurs
     */
    public void write(int b) throws IOException
    {
       
        buffer[bufferPtr++]=(byte)b;
        if (bufferPtr == 512)
        {
            sendNextBlock();
        }
        
    }
    
    /** Send the current buffer to the TFTPSession
     * @throws IOException if a network error occurs
     */
    private void sendNextBlock() throws IOException
    {
        
        if (session == null)
        {
            if (host == null)
            {
                throw (new IOException("Host not specified"));
            }
       
            session=new TFTPSession(host);
            session.initiateWrite(fileName,TFTPSession.OCTET_MODE);
        }
        
        session.writeBlock(buffer,bufferPtr);
        if (bufferPtr < 512)
        {
            eof=true;
        }
        
        bufferPtr=0;
    }
    
}