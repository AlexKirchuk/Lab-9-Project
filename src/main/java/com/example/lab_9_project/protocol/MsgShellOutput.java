package com.example.lab_9_project.protocol;

import java.io.Serial;

public class MsgShellOutput extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    public final byte[] data;
    public final boolean isErr;
    public MsgShellOutput(byte[] data, boolean isErr) { super(Protocol.CMD_SHELL_OUTPUT); this.data = data; this.isErr = isErr; }
}