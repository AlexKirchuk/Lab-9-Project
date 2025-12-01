package com.example.lab_9_project.protocol;

import java.io.Serial;

public class MsgShellInput extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    public final byte[] data;
    public MsgShellInput(byte[] data) { super(Protocol.CMD_SHELL_INPUT); this.data = data; }
}
