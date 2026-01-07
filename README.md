# Laboratory Work No. 9 – Remote Shell over TCP

## Topic: Client–Server Remote Shell Communication

This laboratory work focuses on implementing a **remote shell system** using a classic **client–server architecture**. The system allows clients to connect to a server over TCP, start a shell on the server, and interact with it remotely as if they were typing commands locally.

The project is implemented in **Java**, uses **JavaFX** for GUI clients, and relies on **object serialization** for communication between client and server.

---

## Task Description

### 1. Server Requirements

The server process must perform the following actions:

1. Listen for incoming client connections on a specified TCP port (default: 8072).
2. Accept multiple concurrent client connections, spawning a separate **ServerThread** for each client.
3. Support the following commands from clients:

    * **Connect** – validate and acknowledge client connection.
    * **Disconnect** – gracefully close the client session.
    * **Start Shell** – start a system shell (e.g., `/bin/bash` on Linux, `powershell.exe` on Windows) and stream its input/output.
    * **Shell Input** – accept commands typed by the client and send them to the shell’s stdin.
    * **Shell Output** – stream stdout and stderr from the shell back to the client.
    * **Ping** – optional heartbeat messages for connectivity check.
4. Manage shell processes per client, forwarding input and output through serialized messages.
5. Handle client disconnects and clean up resources properly.

---

### 2. Client Requirements

Each client (console or GUI) must perform the following actions:

1. Connect to the server using its IP address and port.
2. Send a **Connect** message and wait for server acknowledgment.
3. Provide a user interface (console or GUI) to:

    * Start a remote shell session.
    * Send commands to the server shell.
    * Receive and display shell output in real time.
    * Exit remote shell or disconnect gracefully.
4. Continuously listen for messages from the server in a separate thread to prevent UI blocking.
5. Serialize and deserialize messages using the shared **protocol** classes.

---

## Protocol Design

The **protocol** defines communication between client and server:

* **Message (base class)** – all messages inherit from this class and contain an `ID`.
* **Connect/Disconnect messages** – for session establishment and termination.
* **Shell messages**:

    * `MsgStartShell` – request to start a shell.
    * `MsgShellInput` – client input to the shell.
    * `MsgShellOutput` – server output from stdout/stderr.
    * `MsgShellExit` – shell exit code.
* All messages implement **Serializable** for transmission over Object streams.

**Note:** Classes must match exactly on client and server sides.

---

## Project Structure

```

Lab-9-Project/
├── README.md
├── src/
│   ├── module-info.java
│   ├── client/
│   │   ├── ClientFX.java
│   │   └── ClientMain.java
│   ├── protocol/
│   │   ├── Message.java
│   │   ├── MsgConnect.java
│   │   ├── MsgDisconnect.java
│   │   ├── MsgShellExit.java
│   │   ├── MsgShellInput.java
│   │   ├── MsgShellOutput.java
│   │   ├── MsgShellStart.java
│   │   └── Protocol.java
│   └── server/
│       ├── ServerMain.java
│       └── ServerThread.java
```

---

## Technologies Used

### Runtime Implementation

* **Java 17+**
* **JavaFX** for GUI client
* TCP Sockets for communication
* Java Object Serialization for message exchange

### Development Environment

* IntelliJ IDEA (or other Java IDE)
* Works on Linux and Windows (shell path must match OS)

---

## Build Instructions

1. Open the project in IntelliJ IDEA.
2. Make sure JavaFX libraries are added to the module path.
3. Build the project using Maven or IDE build system:

```sh
mvn clean install
```

---

## Execution Workflow Example

1. Launch **ServerMain** on the server machine.
2. Launch **ClientFX** (or **ClientMain**) on the client machine.
3. Enter server IP and port in the client GUI and click **Connect**.
4. If connected successfully, start a shell session with the **shell** command or GUI button.
5. Type shell commands (e.g., `ls`, `pwd`, `echo "Hello"`).
6. Observe output streamed from the server shell in real time.
7. Disconnect or exit the shell gracefully with `::exit` or `quit`.

---

## Testing

* Test console client first to verify basic connectivity and message handling.
* Test GUI client for real-time output streaming and command input.
* Ensure multiple clients can connect to the server concurrently without conflicts.

---

## Notes and Tips

* On Linux, default shell path: `/bin/bash`.
* On Windows, default shell path: `powershell.exe`.
* Ensure firewall allows incoming connections on the chosen port.
* Messages must be serialized consistently; mismatched classes will cause `ClassNotFoundException`.

---

## License

This project is provided for educational purposes in the context of **client-server communication**, **remote shell execution**, and **object serialization in Java**.