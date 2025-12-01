package com.example.lab_9_project.client;

import com.example.lab_9_project.protocol.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        if (args.length < 1) { System.err.println("Usage: java lab_9_project.client.ClientMain <clientName> [host]"); return; }
        String name = args[0];
        String host = args.length >= 2 ? args[1] : "localhost";
        try (Socket sock = new Socket(host, Protocol.PORT)) {
            ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());

            oos.writeObject(new MsgConnect(true, name)); oos.flush();
            MsgConnect res = (MsgConnect) ois.readObject();
            if (!res.ok) { System.err.println("Connect failed: " + res.message); return; }
            System.out.println(res.message);

            Scanner sc = new Scanner(System.in);
            Thread reader = getThread(ois);
            reader.start();

            System.out.println("Enter commands:");
            while (true) {
                String line = sc.nextLine();
                if (line.equals("quit") || line.equals("q")) {
                    oos.writeObject(new MsgDisconnect()); oos.flush(); break;
                }
                if (line.startsWith("shell")) {

                    String[] parts = line.split(" ", 2);
                    String path = parts.length > 1 ? parts[1] : "/usr/bin/bash";
                    oos.writeObject(new MsgShellStart(path)); oos.flush();
                    System.out.println("Start shell requested");
                    System.out.println("Entering remote-input mode. Type '::exit' on a new line to leave remote-input mode.");
                    while (true) {
                        String inLine = sc.nextLine();
                        if ("::exit".equals(inLine)) { System.out.println("Leaving remote-input mode"); break; }
                        byte[] bytes = (inLine + "\n").getBytes();
                        oos.writeObject(new MsgShellInput(bytes)); oos.flush();
                    }
                    continue;
                }

                System.err.println("Unknown command. Use 'shell [path]' to start remote shell or 'quit' to exit.");
            }
        } catch (Exception e) {
            System.err.println("Runtime error: " + e.getMessage());
        }
    }

    private static Thread getThread(ObjectInputStream ois) {
        Thread reader = new Thread(() -> {
            try {
                while (true) {
                    Message m = (Message) ois.readObject();
                    switch (m.getID()) {
                        case Protocol.CMD_SHELL_OUTPUT:
                            MsgShellOutput mo = (MsgShellOutput)m;
                            System.out.write(mo.data);
                            System.out.flush();
                            break;
                        case Protocol.CMD_SHELL_EXIT:
                            MsgShellExit me = (MsgShellExit)m;
                            System.out.println("\n[remote shell exited with code=" + me.exitCode + "]");
                            break;
                        default:
                    }
                }
            } catch (Exception _) {}
        });
        reader.setDaemon(true);
        return reader;
    }
}