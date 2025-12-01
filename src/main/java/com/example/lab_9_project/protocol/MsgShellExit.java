package com.example.lab_9_project.protocol;

import java.io.Serial;

public class MsgShellExit extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    public final int exitCode;
    public MsgShellExit(int exitCode) { super(Protocol.CMD_SHELL_EXIT); this.exitCode = exitCode; }
}