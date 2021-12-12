package com.handyedit.ant.listener;

import org.apache.tools.ant.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Breakpoints storage on Ant side.
 *
 * @author Alexei Orischenko
 *         Date: Nov 5, 2009
 */
public class BreakpointManager {

    // current debugger position
    private BreakpointPosition myCurrentTaskLine;
    private BreakpointPosition myCurrentTargetLine;

    private final List<BreakpointPosition> myTaskStack = new ArrayList<>();

    // sync objects
    private final Object myResumeObject = new Object();
    private final Object myTempSyncObject = new Object();

    // task and target breakpoints
    private final Set<BreakpointPosition> myBreakpoints = new HashSet<>(); // breakpoints
    private final Set<BreakpointPosition> myRunToBreakpoints = new HashSet<>(); // run to cursor breakpoints

     // next task breakpoint variables
    private boolean myTaskBreakpoint = false; // true if set
    private TempBreakpointType myTempBreakpointType;
    private int myTempTargetStackDepth;

    public boolean isBreakpoint() {
        return myBreakpoints.contains(myCurrentTaskLine) ||
                (myBreakpoints.contains(myCurrentTargetLine) && !isTask());
    }

    boolean isRunToBreakpoint(final BreakpointPosition pos) {
        return myRunToBreakpoints.contains(pos);
    }

    boolean isTempBreakpoint() {
        synchronized (myTempSyncObject) {
            if (isTask() && myTaskBreakpoint) {
                if (myTempBreakpointType == TempBreakpointType.INTO) {
                    return true;
                }
                int stackSize = getStackSize();
                if (myTempBreakpointType == TempBreakpointType.OVER) {
                    return stackSize <= myTempTargetStackDepth;
                }
                if (myTempBreakpointType == TempBreakpointType.OUT) {
                    return stackSize < myTempTargetStackDepth;
                }
            }
            return false;
        }
    }

    public void add(final BreakpointPosition loc) {
        myBreakpoints.add(loc);
    }

    public void remove(final BreakpointPosition loc) {
        myBreakpoints.remove(loc);
    }

    public void addRunTo(final BreakpointPosition loc) {
        myRunToBreakpoints.add(loc);
    }

    void removeRunTo(final BreakpointPosition pos) {
        myRunToBreakpoints.remove(pos);
    }

    public void addTemp(final TempBreakpointType type) {
        synchronized (myTempSyncObject) {
            myTaskBreakpoint = true;
            myTempBreakpointType = type;
            myTempTargetStackDepth = getStackSize();
        }
    }

    void removeTemp() {
        synchronized (myTempSyncObject) {
            myTaskBreakpoint = false;
        }
    }

    void waitResume() throws InterruptedException {
        synchronized (myResumeObject) {
            myResumeObject.wait();
        }
    }

    public void resume() {
        synchronized (myResumeObject) {
            myResumeObject.notifyAll();
        }
    }

    void setCurrentPosition(final BreakpointPosition taskLine,
                            final BreakpointPosition targetLine) {
        myCurrentTaskLine = taskLine;
        myCurrentTargetLine = targetLine;
    }

    private boolean isTask() {
        return myCurrentTaskLine != null;
    }

    void setCurrentPosition(final BreakpointPosition targetLine) {
        setCurrentPosition(null, targetLine);
    }

    public Set<BreakpointPosition> getBreakpoints() {
        return myBreakpoints;
    }

    void onTargetStart(final BreakpointPosition pos) {
        myTaskStack.add(pos);
    }

    void onTargetEnd() {
        if (!myTaskStack.isEmpty()) {
            myTaskStack.remove(myTaskStack.size() - 1);
        }
    }

    private int getStackSize() {
        return myTaskStack.size();
    }

    private BreakpointPosition getCurrentTarget() {
        return myTaskStack.isEmpty()
                ? null
                : myTaskStack.get(myTaskStack.size() - 1);
    }

    boolean isCurrentTarget(final Location loc) {
        BreakpointPosition currentPos = getCurrentTarget();
        if (currentPos != null && loc != null) {
            BreakpointPosition pos = new BreakpointPosition(loc);
            return pos.equals(currentPos);
        }
        return false;
    }
}
