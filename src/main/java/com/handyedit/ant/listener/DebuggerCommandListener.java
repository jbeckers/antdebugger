package com.handyedit.ant.listener;

import com.handyedit.ant.listener.cmd.DebuggerCommandFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author Alexei Orischenko
 * Date: Nov 5, 2009
 */
public final class DebuggerCommandListener {

    private final DebuggerCommandFactory myFactory;
    private final BufferedReader myReader;
    private final BufferedWriter myWriter;

    private DebuggerCommandListener(final BreakpointManager manager,
                                    final BufferedReader reader,
                                    final BufferedWriter writer) {
        myFactory = new DebuggerCommandFactory(manager);
        myReader = reader;
        myWriter = writer;
    }

    public void run() throws IOException {
        DebuggerCommand c;
        while ((c = read()) != null) {
            c.execute(myWriter);
        }
    }

    private DebuggerCommand read() throws IOException {
        String line = myReader.readLine();
        String[] cmd = line.split(",");
        return myFactory.create(cmd);
    }

    private DebuggerCommand readBreakpoint() throws IOException {
        String line = myReader.readLine();
        String[] cmd = line.split(",");
        if (myFactory.isBreakpointCommand(cmd)) {
            if (cmd.length < 3) {
                throw new RuntimeException("readBreakpoint(): " + line);
            }

            return myFactory.createBreakpointCommand(cmd);
        } else {
            return null;
        }
    }

    // reads sequence of breakpoint commands terminated by other command
    private void readBreakpoints() throws IOException {
        DebuggerCommand c;
        while ((c = readBreakpoint()) != null) {
            c.execute(myWriter);
        }
    }

    static DebuggerCommandListener start(final BreakpointManager manager,
                                         final int port) throws IOException {
        try (ServerSocket listenSocket = new ServerSocket(port)) {
            listenSocket.setSoTimeout(60000);
            try (Socket s = listenSocket.accept()) {

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));

                final DebuggerCommandListener listener = new DebuggerCommandListener(manager, in, out);
                listener.readBreakpoints();

                new Thread(() -> {
                    try {
                        listener.run();
                    } catch (final IOException ignored) {
                    }
                }).start();

                return listener;
            }
        }
    }

    void sendCommand(final String... args) throws IOException {
        myWriter.write(String.join(",", args));
        myWriter.newLine();
        myWriter.flush();
    }

    void close() {
/*
        try {
            if (myReader != null) {
                myReader.close();
                myReader = null;
            }
        } catch (IOException e) {
        }
        try {
            if (myWriter != null) {
                myWriter.close();
                myWriter = null;
            }
        } catch (IOException e) {
        }
*/
        // todo: stop thread
    }
}
