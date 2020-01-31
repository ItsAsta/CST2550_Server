package org.abdz.utils;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class ServerUtils {

    /**
     * Pass an object to the client
     *
     * @param output Stream output you would like to use
     * @param object The object that you are passing over
     */
    public static void passData(ObjectOutputStream output, Object object) {
        try {
//            Write the object in the stream
            output.writeObject(object);
//            Flush the stream so the packet is sent immediately
            output.flush();
        } catch (IOException e) {
            System.out.println("Can't Pass Data To Client!");
            e.printStackTrace();
        }
    }
}