package com.example.lab_9_project.protocol;

import java.io.Serial;

public class MsgConnect extends Message {
    @Serial
    private static final long serialVersionUID = 1L;
    public final boolean ok;
    public final String message;
    public MsgConnect(boolean ok, String message) { super(Protocol.CMD_CONNECT); this.ok = ok; this.message = message; }
}