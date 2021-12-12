package com.handyedit.ant.listener.cmd;

import com.handyedit.ant.listener.BreakpointManager;
import com.handyedit.ant.listener.BreakpointPosition;
import com.handyedit.ant.listener.DebuggerCommand;
import com.handyedit.ant.listener.TempBreakpointType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class DebuggerCommandFactory {

    // commands sent to build listener from IDE
    public static final String CMD_SET_BREAKPOINT = "set";
    public static final String CMD_REMOVE_BREAKPOINT = "remove";
    public static final String CMD_RESUME_EXECUTION = "resume";
    public static final String CMD_RUN_TO_CURSOR = "run-to";
    public static final String CMD_SET_TEMP_BREAKPOINT = "temp-breakpoint";

    private final BreakpointManager myManager;

    public DebuggerCommandFactory(final BreakpointManager manager) {
        myManager = manager;
    }

    public DebuggerCommand create(final String... args) {
        if (args == null || args.length == 0) {
            return null;
        }
        String type = args[0];

        if (CMD_SET_BREAKPOINT.equals(type) && args.length == 3) {
            return createAddBreakpointCommand(Integer.parseInt(args[1]), args[2]);
        }
        if (CMD_REMOVE_BREAKPOINT.equals(type) && args.length == 3) {
            return createRemoveBreakpointCommand(Integer.parseInt(args[1]), args[2]);
        }
        if (CMD_RESUME_EXECUTION.equals(type)) {
            return createResumeCommand();
        }
        if (CMD_RUN_TO_CURSOR.equals(type) && args.length == 3) {
            return createRunToCommand(Integer.parseInt(args[1]), args[2]);
        }
        if (CMD_SET_TEMP_BREAKPOINT.equals(type) && args.length == 2) {
            return createTempBreakpoint(parseBreakpointType(args[1]));
        }
        return null;
    }

    private static TempBreakpointType parseBreakpointType(final String val) {
        try {
            int value = Integer.parseInt(val);
            TempBreakpointType result = TempBreakpointType.get(value);
            if (result != null) {
                return result;
            }
        } catch (final NumberFormatException ignored) {
        }

        return TempBreakpointType.OVER;
    }

    public DebuggerCommand createBreakpointCommand(final String... args) {
        return isBreakpointCommand(args) && args.length == 3
                ? createAddBreakpointCommand(Integer.parseInt(args[1]), args[2])
                : null;
    }

    public boolean isBreakpointCommand(final String @NotNull ... args) {
        return CMD_SET_BREAKPOINT.equals(args[0]);
    }

    @Contract(pure = true)
    private @NotNull DebuggerCommand createAddBreakpointCommand(final int loc, final String file) {
        return out -> myManager.add(new BreakpointPosition(loc, file));
    }

    @Contract(pure = true)
    private @NotNull DebuggerCommand createRemoveBreakpointCommand(final int loc, final String file) {
        return out -> myManager.remove(new BreakpointPosition(loc, file));
    }

    @Contract(pure = true)
    private @NotNull DebuggerCommand createResumeCommand() {
        return out -> myManager.resume();
    }

    @Contract(pure = true)
    private @NotNull DebuggerCommand createRunToCommand(final int loc, final String file) {
        return out -> myManager.addRunTo(new BreakpointPosition(loc, file));
    }

    @Contract(pure = true)
    private @NotNull DebuggerCommand createTempBreakpoint(final TempBreakpointType type) {
        return out -> myManager.addTemp(type);
    }
}
