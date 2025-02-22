# RetroLemmini

A continuation of the Lemmini family of engines (Lemmini, SuperLemmini, SuperLemminiToo) which aims to fix a few bugs and generally update the engine.

## Build Instructions

### Via the Command Line

1. Install a JDK.
2. Install Ant, the Java build tool.
3. Navigate to RetroLemmini's root directory (it contains `build.xml`).
4. Run `ant` to build.
5. Run `ant run` to play.

### Via Eclipse IDE

1. Add dependencies to build path
   (Project > Properties > Java Build Path > Libraries > Add Library/Add JARs)
   Navigate to "dependencies" folder and add each one
2. Run
   RetroLemmini should compile and run without issues, but to get a runnable JAR file it's necessary to Export from Eclipse:

   (File > Export > Java > Runnable JAR file > Next)
   Recommended: Copy required libraries into a sub-folder

If you experience any issues, visit the
[Lemmini board on Lemmings Forums](https://www.lemmingsforums.net/index.php?board=10.0).

### Tips for exporting with libraries (if using Eclipse)

By default, when exporting from Eclipse as a Runnable JAR File and selecting "Copy libraries into a sub-folder", the resulting
folder will be named "RetroLemmini_lib", which can make the directory look somewhat cluttered. However, if you rename the
folder to something else, the program won't run.

To bypass this, follow these steps:

1) Export as normal but select "Save as ANT script" and choose a name for the .xml script.
2) Open the script in a text editor and change all instances of "RetroLemmini_lib" to "lib" (or whatever you'd prefer).
3) From Eclipse, browse to your .xml script in the project explorer, right-click it, and select "Run As > ANT Build".
4) The .jar will now be built to look for "lib" as opposed to "RetroLemmini_lib".

You can then use this .xml script to export from now on.

## Updates

For a full overview of the updates between (Super)Lemmini(Too) and RetroLemmini, and the planned updates for the future,
please visit [RetroLemmini's release topic on Lemmings Forums](https://www.lemmingsforums.net/index.php?msg=105514).

## Thanks

### From Will (RetroLemmini developer):

Many thanks to Volker, Ryan, Charles and Jeremy for their hard work and support on the Lemmini project over the years. It's such a great program, and the
fact it's still used many years after its first version makes it worth the extra TLC to keep it up to date.

I hope that it can be enjoyed for many years to come.

### From Charles (SuperLemminiToo developer):

I want to stress that this program was truly written by Volker Oth (Lemmini) and Ryan Sakowski (SuperLemmini), over a combined total of more than twenty years.
All I've done is hack a couple lines of code. None of this could be possible without the literally thousands of hours of work done by those two individuals,
and their making the source code freely available. Thanks you both for letting me re-live some joy from my childhood in a new way, and for letting me share
it with my kids.

Also special thanks to WillLem from the LemmingsForums.net for providing the updated title graphic, and being all around supportive of this endeavour and
SuperLemmini in particular.

Special thanks as well to jkapp76 from the LemmingsForums.net for making title icons. I modified them slightly to incorporate them into Icon Labels toggle
in SuperLemminiToo.
