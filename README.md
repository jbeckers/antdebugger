# antdebugger

![Build](https://github.com/opticyclic/antdebugger/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

IntelliJ IDEA Plugin - Ant Debugger

<!-- Plugin description -->
Ant Debugger enables Apache Ant build script debugging:
- Open build file in editor
- Set debug breakpoints
- Right click on editor to show context menu and select the "Debug" menu item to launch Ant debugger
- Wait until the Ant debugger stops on breakpoint then use step or resume debugger commands, investigate Ant variables and execution stack

You can add Ant debug configuration from the Edit configurations dialog. Then you select Ant file, Java SDK for debugging session.

**Note:** Plugin requires Java SDK to launch Ant: plugin uses build file module, project or any SDK for JDK list (the plugin searches for Java SDK in this order and uses first found).
<!-- Plugin description end -->

This is a fork of AntDebugger v1.1.6 from [http://plugins.jetbrains.com/plugin?pr=idea&pluginId=4526](http://plugins.jetbrains.com/plugin?pr=idea&pluginId=4526)
It has been updated to compile and run with IDEA v13.

See <a href="http://handyedit.com/antdebugger.html">http://handyedit.com/antdebugger.html</a> for 1.1.6 and earlier versions.

v1.2.0 was the first version that was compiled to work with IDEA v12. However, it had bugs and the extensions in IDEA have now changed enough in v13 to make it difficult work with IDEA v12 and v13.

v.1.3.0 is the current version. The plugin name has v1.2.0 in it because it wasn't renamed well during the fork.

Please post an issue on <a href="https://github.com/opticyclic/antdebugger/issues">GitHub</a> if there are errors/problems when using Ant debugger

Hopefully this can get folded into [IDEA Community core](https://github.com/JetBrains/intellij-community/tree/master/plugins/ant). 
