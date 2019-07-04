// extension to select, draw, play selection, and export it

+ SonaGraphGui {

	select { |fromBin = 0, toBin|
		to = if (toBin.isNil){sonaGraph.amp.size-1}{toBin} ;
		from = fromBin ;
		selected = sonaGraph.amp[from..to]
	}

	// could be reduced to refresh if we had an if in relation to to and from
	// in selecting grey (-> becomes color)
	drawSelected{ u.refresh } // is it useful?

	// this is a bit spaghetti as it can be done directly from sonagraph
	playSelected{ |thresh= -30, boost = 15|
		sonaGraph.playSonoChord(thresh, from, to, boost)
	}

	stopPlayingSelected { sonaGraph.stopSonoChord }

	// how do we archive? Should be compatible with sona format
	// so we can load it directly
	// but we should be able to filter out as a function of thresh
	writeSelected{}

}

/*
// here we start up server and defs
SonaGraph.prepare ;

// something to analyze, i.e a buffer
~path = Platform.resourceDir +/+ "sounds/a11wlk01.wav";
~sample = Buffer.read(s, ~path).normalize ;

// an istance
a = SonaGraph.new ;
// now analyzing in real-time
a.analyze(~sample,15) ; // rate depends on dur etc

g = SonaGraphGui.new(a, ~sample, hStep:5, vStep:6).makeGui(-30) ;
S
g.select(40, 55)

g.playSelected(-30)

g.drawSelected


blue
g.to =nil
g.from = nil
*/