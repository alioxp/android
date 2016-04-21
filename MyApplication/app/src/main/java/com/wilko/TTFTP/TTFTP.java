/*
 * TTFTP.java
 *
 * Created on 18 October 2000, 21:17
 */

package com.wilko.TTFTP;

/** A simple Main() wrapper for the TTFTP command
 * Change log:
 * <PRE>$Log: TTFTP.java,v $
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
public class TTFTP extends Object {

    /** Creates new TTFTP */
    public TTFTP() {
    }

    /** Simple Main() wrapper for TTFTP
     * @param args the command line arguments
     */
    public static void main00 (String args[]) {
        if (args.length==3)
        {
            if (args[0].equals("get"))
            {
                TTFTPProcess t = new TTFTPProcess();
                //t.tftpGet(args[1],args[2]);
                return;
            }
            else if (args[0].equals("put"))
            {
                TTFTPProcess t = new TTFTPProcess();
                // t.tftpPut(args[1],args[2]);
                return;
            }
        }
        System.out.println("Usage ttftp get|put host filename");
    }
}
