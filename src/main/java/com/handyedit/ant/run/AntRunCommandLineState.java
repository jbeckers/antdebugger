package com.handyedit.ant.run;

import com.handyedit.ant.util.ConsoleUtil;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Orischenko
 *         Date: Nov 4, 2009
 */
public class AntRunCommandLineState extends CommandLineState {

    private final AntRunConfiguration myConfig;

    AntRunCommandLineState(final ExecutionEnvironment environment,
                           final AntRunConfiguration config) {
        super(environment);
        myConfig = config;
    }

    @Override
    protected @NotNull OSProcessHandler startProcess() throws ExecutionException {
        return AntProcessFactory.getInstance().createProcess(myConfig);
    }

    @Override
    public @NotNull ExecutionResult execute(@NotNull final Executor executor, @NotNull final ProgramRunner runner) throws ExecutionException {
      ProcessHandler processHandler = startProcess();
      ConsoleView console = ConsoleUtil.createAttachedConsole(myConfig.getProject(), processHandler);

      return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler));
    }
}
