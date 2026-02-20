/*
 * Setup.java
 *
 * Created on March 17, 2007, 6:52 PM
 *
 * Copyright (c) 2007 Roy Zywina
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package main;

import java.io.*;
import java.beans.*;
import java.util.*;

/**
 * This object contains the application setup.  Paths to specific utilities 
 * needed by the program, etc.
 * 
 * Setup is saved/loaded from users home directory.
 *
 * @author Roy Zywina
 */
public class Setup {
    // the defaults should be fine for most unix systems
    

    /// directory used last
    public static String filedir = null;
    /// directory for the output files
    public static String outdir=System.getProperty("user.dir")+"/out";
    
    /// load configuration
    public static void load(){
        String fn = System.getProperty("user.home") + "/.CheckENSDF_conf.xml";
        HashMap<?, ?> hm;
        try{
            XMLDecoder d = new XMLDecoder(
                new BufferedInputStream(
                    new FileInputStream(fn)));
            hm = (HashMap<?, ?>)d.readObject();
            d.close();

            filedir  = (String)hm.get("filedir");
            outdir=(String)hm.get("outdir");
        }catch(Exception ex){} // ignore error
        
    }
    
    /// save configuration
    public static void save(){
        HashMap<String, String> hm = new HashMap<String, String>();

        hm.put("filedir",filedir);
        hm.put("outdir",outdir);
        String fn = System.getProperty("user.home") + "/.CheckENSDF_conf.xml";
        try{
            XMLEncoder e = new XMLEncoder(
                new BufferedOutputStream(
                    new FileOutputStream(new File(fn))));
            e.writeObject(hm);
            e.close();
        }catch(Exception ex){} // ignore error        
    }
}

