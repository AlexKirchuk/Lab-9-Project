package com.example.lab_9_project.protocol;

import java.io.Serial;
import java.io.Serializable;
public abstract class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final byte id;
    protected Message(byte id) { this.id = id; }
    public byte getID() { return id; }
}