package com.example.lab_9_project.protocol;

public class Protocol {
    public static final int PORT = 8072;

    public static final byte CMD_CONNECT = 1;
    public static final byte CMD_DISCONNECT = 2;
    public static final byte CMD_SHELL_START = 3;
    public static final byte CMD_SHELL_INPUT = 4;
    public static final byte CMD_SHELL_OUTPUT = 5;
    public static final byte CMD_SHELL_EXIT = 6;
    public static final byte CMD_PING = 7;
}