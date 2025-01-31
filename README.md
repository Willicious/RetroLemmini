# RetroLemmini

A continuation of the Lemmini family of engines (Lemmini, SuperLemmini, RetroLemmini) which aims to fix a few bugs and generally update the engine.

# === V1.0 Updates ===

+ Renamed project to "RetroLemmini" everywhere relevant
+ Resources folder is now created in the root directory of the .jar and is called "resources"
+ All necessary folders are created within the "resources" folder (mods, music, replays, sound, styles)

# === PROPOSED FEATURES/BUGFIXES ===

Java base:

Try to base the program on a later Java version, if at all possible. Something about Java 1.8 means that anyone running on 15 or later may experience
issues relating to .png file loading. The main goal with the next update will be to completely remove the need for exteral setup files; ALL necessary assets
will be included with the download, and the program will be as plug-and-play as possible

* Ideally, also detect the Java enironment before running the .jar (i.e. remove need for "Run with Java X.cmd" files)

Timebombers:

These will need to be updated
Either they should be level-side optional, or the two should exist as separate skills. Being player-side optional causes the following issues:

* Instabombers cannot be assigned during the first 5 seconds of the level, or the Timebomber option will break the level
* Replays currently record at point of assignment for both, so - when shared with another user - th replay will break if that user has the opposite option

Even if we could fix the above (there's really no sensible way to fix the first of these issues),
it seems more appropriate to make Timebombers/Instabombers a design choice rather than a player choice

Skill Panel:

Add the red squiggle to the original (un-enhanced) panel
Don't show the V-Lock icon if the level doesn't need it
Update the graphics to the ones with improved colour scheme (see SLToo topic)

Settings Menu:

Improve layout
Add tooltips to all items

# === THANKS ===

From Will (RetroLemmini developer):

Many thanks to Volker, Ryan, Charles and Jeremy for their hard work and support on the Lemmini project over the years. It's such a great program, and the
fact it's still used many years after its first version makes it worth the extra TLC to keep it up to date

I hope that it can be enjoyed for many years to come

# === THANKS ===

From Charles (RetroLemmini developer):

I want to stress that this program was truly written by Volker Oth (Lemmini) and Ryan Sakowski (SuperLemmini), over a combined total of more than twenty years.
All I've done is hack a couple lines of code. None of this could be possible without the literally thousands of hours of work done by those two individuals,
and their making the source code freely available. Thanks you both for letting me re-live some joy from my childhood in a new way, and for letting me share
it with my kids

Also special thanks to WillLem from the LemmingsForums.net for providing the updated title graphic, and being all around supportive of this endeavour and
SuperLemmini in particular

Special thanks as well to jkapp76 from the LemmingsForums.net for making title icons. I modified them slightly to incorporate them into Icon Labels toggle
in RetroLemmini