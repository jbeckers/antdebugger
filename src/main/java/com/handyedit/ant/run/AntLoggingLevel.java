package com.handyedit.ant.run;

import java.util.Arrays;

/**
 * @author Alexei Orischenko
 *         Date: Feb 15, 2010
 */
public enum AntLoggingLevel {
  DEFAULT(0), QUIET(1), VERBOSE(2), DEBUG(3);

  private final int myValue;

  AntLoggingLevel(final int value) {
    myValue = value;
  }

  public int getValue() {
    return myValue;
  }

  public static AntLoggingLevel get(final int value) {
    return Arrays.stream(values()).filter(level -> level.getValue() == value).findFirst().orElse(DEFAULT);
  }
}
