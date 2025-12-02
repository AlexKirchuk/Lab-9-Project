package com.example.lab_9_project.client;

import com.example.lab_9_project.protocol.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientFX extends Application {
    private TextArea terminal;
    private TextField input;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    @Override
    public void start(Stage stage) {
        var hostField = new TextField("localhost");
        var portField = new TextField("8072");
        var nameField = new TextField("client1");
        var connectBtn = new Button("Connect");

        var form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.addRow(0, new Label("Host:"), hostField);
        form.addRow(1, new Label("Port:"), portField);
        form.addRow(2, new Label("Name:"), nameField);
        form.add(connectBtn, 1, 3);

        stage.setScene(new Scene(form, 320, 180));
        stage.setTitle("RemoteShell Connect");
        stage.show();

        connectBtn.setOnAction(_ -> connect(stage, hostField.getText(), portField.getText(), nameField.getText()));
    }

    private void connect(Stage stage, String host, String port, String name) {
        try {
            socket = new Socket(host, Integer.parseInt(port));
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(new MsgConnect(true, name));
            out.flush();

            Object resp = in.readObject();
            if (!(resp instanceof MsgConnect connRes)) {
                throw new IOException("Unexpected response from server: " + resp);
            }

            boolean ok = false;
            String message = null;
            try {
                ok = connRes.ok;
                message = connRes.message;
            } catch (Throwable t) {
                try {
                    message = connRes.toString();
                } catch (Throwable ignored) {}
            }

            if (!ok) {
                System.err.println("Connect failed: " + message);
                socket.close();
                return;
            }

            openTerminal(stage);
            startReaderThread();

            out.writeObject(new MsgShellStart(null));
            out.flush();

        } catch (Exception e) {
            System.err.println("Runtime error: " + e.getMessage());
            if (socket != null && !socket.isClosed()) {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }
    }

    private void openTerminal(Stage stage) {
        terminal = new TextArea();
        terminal.setEditable(false);
        terminal.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13;");

        input = new TextField();
        input.setPromptText("Type command or text; press Enter to send. Use '::exit' to stop remote-input mode or 'quit' to disconnect.");
        input.setOnAction(_ -> sendInput());

        var root = new BorderPane();
        root.setCenter(terminal);
        root.setBottom(input);
        BorderPane.setMargin(input, new Insets(5));

        stage.setScene(new Scene(root, 900, 600));
        stage.setTitle("Remote Shell - " + (socket != null ? socket.getRemoteSocketAddress().toString() : ""));
        stage.show();
    }

    private void sendInput() {
        try {
            String txt = input.getText();
            input.clear();

            Alexander Kirchuk, [02.12.2025 21:15]
            if (txt == null) return;
            if ("quit".equalsIgnoreCase(txt) || "q".equalsIgnoreCase(txt)) {
                out.writeObject(new MsgDisconnect());
                out.flush();
                try { socket.close(); } catch (IOException ignored) {}
                Platform.runLater(() -> terminal.appendText("\n[Disconnected by client]\n"));
                return;
            }

            if ("::exit".equals(txt)) {
                Platform.runLater(() -> terminal.appendText("\n[Client-side: exit command acknowledged]\n"));
                return;
            }

            byte[] data = (txt + "\n").getBytes();
            out.writeObject(new MsgShellInput(data));
            out.flush();

        } catch (IOException e) {
            System.err.println("Runtime error: " + e.getMessage());
            Platform.runLater(() -> terminal.appendText("\n[Error sending input]\n"));
        }
    }

    private void startReaderThread() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    Object o = in.readObject();
                    switch (o) {
                        case null -> {
                            continue;
                        }
                        case MsgShellOutput msgShellOutput -> {
                            final String outText = getString(msgShellOutput);
                            Platform.runLater(() -> terminal.appendText(outText));
                            continue;
                        }
                        case MsgShellExit exitMsg -> {
                            int exitCode = 0;
                            try {
                                exitCode = exitMsg.exitCode;
                            } catch (Throwable ignored) {
                            }
                            final int ec = exitCode;
                            Platform.runLater(() -> terminal.appendText("\n[Shell exited with code=" + ec + "]\n"));
                            continue;
                        }
                        default -> {}
                    }
                    Platform.runLater(() -> terminal.appendText("\n[Ignored message: " + o.getClass().getSimpleName() + "]\n"));
                }
            } catch (EOFException | SocketException eof) {
                Platform.runLater(() -> terminal.appendText("\n[Disconnected]\n"));
            } catch (Exception e) {
                System.err.println("Runtime error: " + e.getMessage());
                Platform.runLater(() -> terminal.appendText("\n[Reader thread error]\n"));
            } finally {
                try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private static String getString(MsgShellOutput o) {
        byte[] bytes = null;
        try {
            bytes = o.data;
        } catch (Throwable _) {}
        return (bytes != null) ? new String(bytes) : "[<no-data>]";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
