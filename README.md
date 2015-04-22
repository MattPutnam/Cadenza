Cadenza
=======

Cadenza is a live MIDI performance manager, designed specifically to handle the large number of patches (and quick patch changes) that most modern musicals call for.  It is designed to minimize cost--the software is free to use, and it's designed to require as few outside purchases as possible.  If you already have a laptop capable of running Java and a keyboard with MIDI I/O, you're already almost there.  If the keyboard has USB MIDI, you just need a USB cable, otherwise you need a MIDI interface (for example the Roland UM-ONE at around $40).  For more sounds, I recommend picking up a Roland JV-1080, which can be found on eBay for around $200.  No MacBook Pro and ProTools, no expensive VST libraries, no specialty pedals.

To install, copy the relevant .zip file out of the root directory to wherever (likely Program Files on Windows, Applications on Mac OS X, but it doesn't actually matter) and unzip it.  This results in a directory with the runnable .JAR and some other stuff inside.  The .JAR MUST be in the same directory as the "resources" subdirectory in order to work properly.

Collaborators:
---

* Importing into an IDE will likely pick up a ton of unnecessary JARs.  The only JARs that should be on the build path for development are in <code>lib/import</code>.  It may also pick up <code>thirdPartySrc</code> as a source folder; take that off too.
* Copy <code>resources/preferences.default</code> and rename to <code>preferences.txt</code> (in the same directory).  Otherwise preferences will fail to load.
* Add <code>lib/resources</code> as a source folder to allow log4j logging.