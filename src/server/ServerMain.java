package com.example.lab_9_project.server;

import com.example.lab_9_project.protocol.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        System.out.println("RemoteShell server starting on port " + Protocol.PORT);
        try (ServerSocket serv = new ServerSocket(Protocol.PORT)) {
            while (true) {
                Socket sock = serv.accept();
                System.out.println("Accepted " + sock.getRemoteSocketAddress());
                ServerThread st = new ServerThread(sock);
                st.start();
            }
        }
    }
}