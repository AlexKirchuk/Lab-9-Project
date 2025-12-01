package com.example.lab_9_project.protocol;

import java.io.Serial;

public class MsgShellStart extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    public final String shellPath;
    public MsgShellStart(String shellPath) { super(Protocol.CMD_SHELL_START); this.shellPath = shellPath; }
}