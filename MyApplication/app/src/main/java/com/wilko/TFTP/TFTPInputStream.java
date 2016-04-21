/*
 * tftpReader.java
 *
 * Created on 18 October 2000, 21:37
 */

package com.wilko.TFTP;


import java.io.*;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

/** TFTPInputStream is a sub-class of InputStream that provides access to a  <B>TFTP Server</B>
 * Change log:
 * <PRE>$Log: TFTPInputStream.java,v $
 * <PRE>Revision 1.1.1.1  2000/10/29 04:08:23  wilko
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
 * @see com.wilko.TFTP.TFTPSession
 */
public class TFTPInputStream extends java.io.InputStream {

    /** The host to read the file from
     */
    private String host;
    /** The filename to read from the host
     */
    private String fileName;
    /** Data read from the host that is available for consumption
     */
    private byte buffer[];
    /** Pointer to the next byte in the buffer to be read
     */
    private int bufferPtr;
    /** The underlying TFTPSession object
     */
    private TFTPSession session;
    /** Has end of file been reached?
     */
    private boolean eof;
    /** Has this stream been closed?
     */
    private boolean closed;
    
    /** Creates a new tftpInputStream that retrieves the specified filename from the specified host
     * @param host Host name to retrieve the file from.  Can be specified as a domain name or dotted quad IP address
     * @param fileName The filename to retrieve from the specified host.  This can not be a path.
     * TFTP does support sub-directories
     */
 /*   public TFTPInputStream() {
        buffer=new byte[512];
        bufferPtr=512;
        host=null;
        fileName=null;
        session=null;
        eof=false;
        closed=false;
    }
*/
    public TFTPInputStream(String host,String fileName)
    {
        bufferPtr=512;
        buffer=new byte[512];
        this.host=host;
        this.fileName=fileName;
        session=null;
        eof=false;
        closed=false;
    }
    
    /** Tests if this input stream supports the mark and reset methods.
     * @return returns false as TFTPInputStream does not support mark/reset
     */
    public boolean markSupported()
    {
        return(false);
    }
    
    /** Returns the number of bytes that can be read (or skipped over) from this input stream without blocking by the next caller of an input method for this input stream.
     * @return the number of bytes that can be read from this input stream without blocking.
     */
    public int available()
    {
        return(buffer.length-bufferPtr);
    }
    
    /** Skips over and discards n bytes of data from this input stream.  This method may
     * skip less than n bytes, possibly 0.  The actual number of bytes skipped is returned.
     * @param n Number of bytes to skip
     * @return The number of bytes skipped
     */
    public long skip(long n)
    {
        long length = buffer.length-bufferPtr;
        if (length > n)
        {
           length=n;
        }
        bufferPtr+=length;
        return(length);
    }
           
    /** Read the next character from the input stream.  Returns -1 if EOF is reached
     * This method blocks until data is available, the end of file is reached or an
     * exception is thrown
     * @throws IOException if an I/O error occurs
     * @return the next character available from this input stream or -1 if EOF has been reached.
     */
    public int read() throws IOException
    {
        if (closed)
        {
            throw new IOException("Closed");
        }
        
        if (eof)
        {
            return(-1);
        }
        
        if (bufferPtr < buffer.length)
        {
            return(((int)buffer[bufferPtr++])&0x0ff);
        }
        else
        {
            getNextBlock();
            if (eof)
            {
                return(-1);
            }
            else
            {
                return(((int)buffer[bufferPtr++])&0x0ff);
            }
        }
    }
                
    
    /* public boolean ready() throws IOException
    {
        if (closed)
        {
            throw new IOException("Closed");
        }
        
        if (bufferPtr <buffer.length)
        {
            return(true);
        }
        else
        {
            return(false);
        }
    } */
    
    /** This method always throws an IOException as mark/reset is not supported on
     * TFTPStreams.
     * @throws IOException always - reset() is not supported
     */
    public void reset() throws IOException
    {
        throw new IOException("reset() not suported");
    }
   
    /** This method always throws an IOException as mark()/reset() are not supported
     * on TFTP Streams.
     * @throws IOException Always - mark() is not supported on a TFTPInputStream
     */
    public void mark() throws IOException
    {
        throw new IOException("mark() not suported");
    }
    
    
    /** Reads up to len bytes of data from the input stream into an array of bytes. An attempt is made to read as many as len bytes, but a smaller number may be read, possibly zero. The number of bytes actually read is returned as an integer.
     *
     * This method blocks until input data is available, end of file is detected, or an exception is thrown.
     *
     * If b is null, a NullPointerException is thrown.
     *
     * If off is negative, or len is negative, or off+len is greater than the length of the array b, then an IndexOutOfBoundsException is thrown.
     *
     * If len is zero, then no bytes are read and 0 is returned; otherwise, there is an attempt to read at least one byte. If no byte is available because the stream is at end of file, the value -1 is returned; otherwise, at least one byte is read and stored into b.
     *
     * The first byte read is stored into element b[off], the next one into b[off+1], and so on. The number of bytes read is, at most, equal to len. Let k be the number of bytes actually read; these bytes will be stored in elements b[off] through b[off+k-1], leaving elements b[off+k] through b[off+len-1] unaffected.
     *
     * In every case, elements b[0] through b[off] and elements b[off+len] through b[b.length-1] are unaffected.
     * If the first byte cannot be read for any reason other than end of file, then an IOException is thrown. In particular, an IOException is thrown if the input stream has been closed.
     * @param b the buffer into which bytes are read
     * @param off The starting offset within buf at which bytes are stored
     * @param len The maxmimum number of bytes that will be read
     * @throws IOException If an IO Error occurs.
     * @return The actual number of bytes that were read
     */
    public int read(byte[] b,int off,int len) throws IOException
    {   
        if (closed)
        {
            throw new IOException("Closed");
        }
        
        if (eof)
        {
            return(-1);
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
                System.arraycopy(buffer,bufferPtr,b,off+counter,length);
                
                counter+=length;
                bufferPtr+=length;   
            }
            else
            {
                getNextBlock();
            }
             
        }
        
        return(counter);
    }
    
    /** Closes this input stream and releases any system resources associated with the stream.
     * @throws IOException If an IO error occurs
     */
    public void close() throws IOException
    {
        closed=true;
    }
    
    /** Get the next block data from the TFTPSession object
     * @throws IOException if an IO Error occurs
     */
    private void getNextBlock() throws IOException
    {
        if (session == null)
        {
            if (host == null)
            {
                throw (new IOException("Host not specified"));
            }
       
            session=new TFTPSession(host);
            session.initiateRead(fileName,TFTPSession.OCTET_MODE);
        }

        if (buffer.length < 512)
        {
           eof=true;
        }
        else
        {
           bufferPtr=0;
           buffer=session.readBlock();
        }
    }
 
}