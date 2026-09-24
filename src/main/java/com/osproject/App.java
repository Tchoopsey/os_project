package com.osproject;

import com.osproject.OS.OSKernel;
import com.osproject.shell.Shell;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        OSKernel kernel = new OSKernel();
        kernel.boot();
        Shell shell = new Shell(kernel);
        shell.start();
    }
}

