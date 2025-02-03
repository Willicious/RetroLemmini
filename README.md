# RetroLemmini

A continuation of the Lemmini family of engines (Lemmini, SuperLemmini, RetroLemmini) which aims to fix a few bugs and generally update the engine.

# ================= V1.0 Updates ============================

 # Project overview
 + Renamed project to "RetroLemmini" everywhere relevant

 # File system
 + Resources folder is now included in the root directory of the .jar and is called "resources"
 + All necessary folders are included within the "resources" folder (mods, music, replays, sound, styles)
 + SetupInstructions.txt created which will be bundled with all future releases and kept up-to-date
 + Settings are now saved to "settings" folder in the root directory of the .jar
 + Icons are included in an "icons" folder
 + "root.lzp" has now been removed: RetroLemmini no longer requires resource extraction or streaming from zip

 # Menu
 + Updated menu ticker size & colour for readability (it's now yellow / yellow on blue for classic)
 + Updated logo
 
 # Skill Panel
 + Improved colour scheme and position of lemming button animations
 + VLock button is now hidden and inactive when not needed

# =============== PROPOSED FEATURES/BUGFIXES ================

# Timebombers:

Being player-side optional causes the following issues:

* Instabombers cannot be assigned during the first 5 seconds of the level, or the Timebomber option will break the level
* Replays currently record at point of assignment for both, so - when shared with another user - th replay will break if that user has the opposite option

Even if we could fix the above (there's really no sensible way to fix the first of these issues),
it seems more appropriate to make Timebombers/Instabombers a design choice rather than a player choice.

With that said, we could maybe fix the second issue by checking which option is active each time a Bomber is assigned,
and then write the countdown to the replay as well. Then, at least replays would be option-independent.

We could also adjust the player-side setting to "by level" or "always instabombers", then make it design-side optional.
The problem with this is that there is no dedicated level editor, so the designer would have to manually enter it into
the level's .ini file via a text editor.

I'll get community feedback on this before going ahead with any drastic changes, but may at least see about the replay bug
in the meantime.

# Settings Menu:

Improve layout
Add tooltips to all items

# =============== THANKS =====================

From Will (RetroLemmini developer):

Many thanks to Volker, Ryan, Charles and Jeremy for their hard work and support on the Lemmini project over the years. It's such a great program, and the
fact it's still used many years after its first version makes it worth the extra TLC to keep it up to date

I hope that it can be enjoyed for many years to come

# =============== THANKS =====================

From Charles (RetroLemmini developer):

I want to stress that this program was truly written by Volker Oth (Lemmini) and Ryan Sakowski (SuperLemmini), over a combined total of more than twenty years.
All I've done is hack a couple lines of code. None of this could be possible without the literally thousands of hours of work done by those two individuals,
and their making the source code freely available. Thanks you both for letting me re-live some joy from my childhood in a new way, and for letting me share
it with my kids

Also special thanks to WillLem from the LemmingsForums.net for providing the updated title graphic, and being all around supportive of this endeavour and
SuperLemmini in particular

Special thanks as well to jkapp76 from the LemmingsForums.net for making title icons. I modified them slightly to incorporate them into Icon Labels toggle
in RetroLemmini