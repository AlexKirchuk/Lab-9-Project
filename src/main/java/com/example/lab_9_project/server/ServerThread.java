package com.example.lab_9_project.server;

import com.example.lab_9_project.protocol.*;
import java.io.*;
import java.net.*;

public class ServerThread extends Thread {
    private final Socket sock;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;
    private Process shellProc = null;
    private OutputStream procStdin = null;
    private volatile boolean running = true;

    public ServerThread(Socket s) throws IOException {
        this.sock = s;
        this.oos = new ObjectOutputStream(s.getOutputStream());
        this.ois = new ObjectInputStream(s.getInputStream());
        setDaemon(true);
    }

    public void run() {
        try {
            while (running) {
                Message m;
                try { m = (Message) ois.readObject(); } catch (SocketException | EOFException se) { break; }
                if (m == null) continue;
                switch (m.getID()) {
                    case Protocol.CMD_CONNECT:
                        handleConnect();
                        break;
                    case Protocol.CMD_DISCONNECT:
                        handleDisconnect();
                        return;
                    case Protocol.CMD_SHELL_START:
                        handleShellStart((MsgShellStart)m);
                        break;
                    case Protocol.CMD_SHELL_INPUT:
                        handleShellInput((MsgShellInput)m);
                        break;
                    case Protocol.CMD_PING:
                        break;
                    default:
                        System.err.println("Unknown message id: " + m.getID());
                }
            }
        } catch (Exception e) {
            System.err.println("Runtime error: " + e.getMessage());
        } finally {
            cleanup();
            try { sock.close(); } catch (IOException _) {}
            System.out.println("Client thread finished: " + sock.getRemoteSocketAddress());
        }
    }

    private void handleConnect() {
        try {
            oos.writeObject(new MsgConnect(true, "Connected to server"));
            oos.flush();
        } catch (IOException e) {
            System.err.println("Failed to send connect result: " + e.getMessage());
        }
    }

    private void handleDisconnect() {
        running = false;
    }

    private void streamProc(InputStream in, boolean isErr) {
        try {
            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) != -1) {
                byte[] out = new byte[r];
                System.arraycopy(buf, 0, out, 0, r);
                oos.writeObject(new MsgShellOutput(out, isErr));
                oos.flush();
            }
        } catch (IOException _) {}
    }

    private void handleShellStart(MsgShellStart m) throws IOException {
        if (shellProc != null) {
            oos.writeObject(new MsgConnect(false, "Shell already running"));
            return;
        }

        String shell = (m.shellPath == null || m.shellPath.isEmpty()) ? "/usr/bin/bash" : m.shellPath;
        try {
            ProcessBuilder pb = new ProcessBuilder(shell);
            pb.redirectErrorStream(false);
            shellProc = pb.start();
            procStdin = shellProc.getOutputStream();
            Thread stdoutPump = new Thread(() -> streamProc(shellProc.getInputStream(), false));
            Thread stderrPump = new Thread(() -> streamProc(shellProc.getErrorStream(), true));
            stdoutPump.setDaemon(true); stderrPump.setDaemon(true);
            stdoutPump.start(); stderrPump.start();
            new Thread(() -> {
                try {
                    int ec = shellProc.waitFor();
                    try { oos.writeObject(new MsgShellExit(ec)); oos.flush(); } catch (IOException _) {}
                } catch (InterruptedException _) {}
            }).start();
            oos.writeObject(new MsgConnect(true, "Shell started: " + shell)); oos.flush();
        } catch (IOException e) {
            oos.writeObject(new MsgConnect(false, "Unable to start shell: " + e.getMessage())); oos.flush();
        }
    }

    private void handleShellInput(MsgShellInput m) {
        if (procStdin == null) return;
        try {
            procStdin.write(m.data);
            procStdin.flush();
        } catch (IOException e) { System.err.println("Runtime error: " + e.getMessage()); }
    }

    private void cleanup() {
        running = false;
        try { if (shellProc != null) shellProc.destroy(); } catch (Exception _) {}
        try { if (ois != null) ois.close(); } catch (Exception _) {}
        try { if (oos != null) oos.close(); } catch (Exception _) {}
    }
}