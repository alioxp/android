/*
 * ttftpProcess.java
 *
 * Created on 18 October 2000, 21:29
 */

package com.wilko.TTFTP;

import com.wilko.TFTP.*;
import java.io.*;

import android.content.Context;
import android.content.ContextWrapper;

/** This class implements TFTP argument parsing and calls the appropriate TFTP stream
 * class methods to transfer a file via TFTP.
 * Change log:
 * <PRE>$Log: TTFTPProcess.java,v $
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
 */
public class TTFTPProcess extends Object {

    /** The well known TFTP port
     */
    private final static int  TFTPPORT=69;
    
    /** Creates new TTFTPProcess
     */
    public TTFTPProcess() {
    }

    /** Get a file via TFTP
     * @param host The host to retrieve the file from (domain name or dotted quad IP address)
     * @param file The name of the file to retrieve
     */
    public void tftpGet(String host, String file,FileOutputStream f)
    {
        int i=0;
        int j=0;
        try {
            TFTPInputStream r= new TFTPInputStream(host,file);
            //FileOutputStream f = this.openFileOutput("1.txt", Context.MODE_PRIVATE); //new FileOutputStream(file);
           
            byte buffer[]= new byte[512];
            while((i=r.read(buffer,0,buffer.length ))!=-1)
            {
                f.write(buffer,0,i);
                j+=i;
              /*  if ((j % 512) == 0)
                {
                    System.out.print(".");
                } */
            }
            r.close();
            f.close();
        }
        catch (IOException  e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Read "+Integer.toString(j));
    }
    
    /** Send a file to a host via TFTP
     * @param host The name of the host to send the file to (domain name or dotted quad IP address)
     * @param file The name of the file to send
     */
    public void tftpPut(String host, String file)
    {
        int i=0;
        int j=0;
        try {
            TFTPOutputStream w= new TFTPOutputStream(host,file);
            FileInputStream f = new FileInputStream(file);      
           
            byte buffer[]= new byte[512];
            while((i=f.read(buffer,0,buffer.length ))!=-1)
            {
                w.write(buffer,0,i);
                j+=i;
              /*  if ((j % 512) == 0)
                {
                    System.out.print(".");
                } */
            }
            w.close();
            f.close(); 
        }
        catch (IOException  e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Wrote "+Integer.toString(j));
        
    }
    
}