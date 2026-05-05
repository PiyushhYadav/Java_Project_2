package client;

import common.Request;
import common.Response;

import java.io.*;
import java.net.Socket;

public class ClientConnection {
    private static final String HOST = "localhost";
    private static final int    PORT = 5000;

    public static Response send(Request request) throws IOException, ClassNotFoundException {
        try (Socket socket             = new Socket(HOST, PORT);
             ObjectOutputStream out    = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in     = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        }
    }
}