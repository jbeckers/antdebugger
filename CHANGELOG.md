<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# antdebugger Changelog

## [Unreleased]
- Convert project to [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- Support for versions newer than 2019
- Add a plugin icon

## [1.3.0]
- v1.2.0 never properly worked. Test that it actully works with IDEA v13
- Name now doesn't reflect version :) 

## [1.2.0]
- Forked on GitHub!
- Fix compile errors so that it works with IDEA v12

## [1.1.6]
- Run setting: logging level (default, quiet, verbose, debug).
- Launch targets from context menu fix

## 1.1.5
- Show pathes in Ant variables (also avaliable in the Evaluate dialog by Alt-F8). The variable name is path element ID.
- Don't stop on property tasks when stepping only if the task is outside the current target

## 1.1.4
- Breakpoints fix for tasks with multiline empty tag
- Don't stop on patternset, path, tstamp tasks when stepping

## 1.1.3
- Create configuration from context menu: 'make' is disabled
- Don't stop on property, typedef, taskdef tasks when stepping
- Breakpoints fix for tasks with multiline open tag

## 1.1.2
- Step in / over / out support
- Run to cursor fixes

## 1.1.1
- Run configuration fix: create configuration

## 1.1
- Variable value tooltip, expression evaluation (Alt-F8)
- Run configuration: show targets from imported files
- Run configuration fix: reload targets list on build file change

## 1.0.8
- Set breakpoint fix for Windows
- Run configuration fix: create configuration in project without JDK
- Fix: determine that Ant process terminated on debugger connect

## 1.0.7
- Run configuration: build target selection
- Can debug target right clicking on it
- Run configuration: VM parameters

## 1.0.6
- Fix: projects with JDK 1.5
- Fix: step into macro-def
- Multiple files support fixes

## 1.0.5
- Ant launch fix for Windows: quotes

## 1.0.4
- Ant launch fix for Windows

## 1.0.3
- Fix for Windows: removed Java path checking
- Ant launch fix for Windows

## 1.0.2
- Configuration: custom Ant tasks folder
- Fix: don't block Ant build if Idea hasn't connected to it (timeout in build listener)
- More feedback for connect error

## 1.0.1
- Multiple XML files support
- UI enhancements

[Unreleased]: https://github.com/opticyclic/antdebugger/compare/50658ae083df244726aa9b663682a1abf0bdc17a...HEAD
[1.3.0]: https://github.com/opticyclic/antdebugger/compare/80911c140195d330488e37582675255381076e02..50658ae083df244726aa9b663682a1abf0bdc17a
[1.2.0]: https://github.com/opticyclic/antdebugger/compare/ec4b231c8ba086744ff991ceb5e5859e8931040c..80911c140195d330488e37582675255381076e02
[1.1.6]: https://plugins.jetbrains.com/plugin/4526-ant-debugger

