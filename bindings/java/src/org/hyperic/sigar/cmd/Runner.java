/*
 * Copyright (c) 2006-2007 Hyperic, Inc.
 * Copyright (c) 2010 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hyperic.sigar.cmd;

import java.io.File;
import java.io.FileFilter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.net.URLClassLoader;
import java.net.URL;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarLoader;

/**
 * Some commands require junit and log4j on the classpath.
 */
public class Runner {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            args = new String[] { "Shell" };
        }
        else {
            //e.g. convert
            //          "ifconfig", "eth0"
            //   to:
            // "Shell", "ifconfig", "eth0" 
            if (Character.isLowerCase(args[0].charAt(0))) {
                String[] nargs = new String[args.length + 1];
                System.arraycopy(args, 0, nargs, 1, args.length);
                nargs[0] = "Shell";
                args = nargs;
            }
        }

        String name = args[0];

        String[] pargs = new String[args.length - 1];
        System.arraycopy(args, 1, pargs, 0, args.length-1);

        Class cmd = null;
        String[] packages = {
            "org.hyperic.sigar.cmd.",
            "org.hyperic.sigar.test.",
            "org.hyperic.sigar.",
            "org.hyperic.sigar.win32.",
            "org.hyperic.sigar.jmx.",
        };

        for (int i=0; i<packages.length; i++) {
            try {
                cmd = Class.forName(packages[i] + name);
                break;
            } catch (ClassNotFoundException e) {}
        }

        if (cmd == null) {
            System.out.println("Unknown command: " + args[0]);
            return;
        }

        Method main = cmd.getMethod("main",
                                    new Class[] {
                                        String[].class
                                    });

        try {
            main.invoke(null, new Object[] { pargs });
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof NoClassDefFoundError) {
                System.out.println("Class Not Found: " +
                                   t.getMessage());
            }
            else {
                t.printStackTrace();
            }
        }
    }
}
