package dev.itsasta.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerUtils {

    public static void passData(ObjectOutputStream output, Object object) {
        try {
            output.writeObject(object);
            output.flush();
        } catch (IOException e) {
            System.out.println("Can't Pass Data To Client!");
            e.printStackTrace();
        }
    }
}