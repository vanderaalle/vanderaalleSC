/*

// Custom dirty extension to plot pitch class reduction
+ SonaGraphLily {

	// split all bins from amp, then flop to group voiced
	splitIntoVoicesClass {|amp, thresh|
		^this.collectChords(amp, thresh)
		.collect{|i| (i%12+60)
			.asSet.asArray
		} // custom for class
		.collect{|i|
			this.splitChordStruct(i)
		}.flop
		// treble and bass: ONLY TREBLE HERE
	}

	makeLilyChordClass {|sonagraph, thresh, fromBin, toBin, path|
		var v ;
		var data, tempo ;
		toBin = if (toBin.isNil){sonagraph.amp.size-1}{toBin} ;
		v = this.splitIntoVoicesClass(sonagraph.amp[fromBin..toBin], thresh) ;
		data = [this.createSequence(v[0].postln)] ; // some useless operations
		tempo = 60/(sonagraph.anRate.reciprocal*4) ;
		this.makeLilyChordClassFile(data, sonagraph.amp, tempo, path)
	}



	makeLilyChordClassFile {|data, amp, tempo = 60, path|
		var template = "
\\version \"2.19.2\"

\\header {
tagline = \"\"  % removed
}

#(set! paper-alist (cons '(\"my size\" . (cons (* WIDTH in) (* HEIGHT in))) paper-alist))

\\paper {
#(set-paper-size \"my size\")
}

\\score {
<<
\\override Score.BarNumber.break-visibility = ##(#t #t #t)

\\new Staff {
\\set fontSize = -1
\\time 4/4

\% STARTBAR

TREBLE

\% ENDBAR
}

>>
}
"
		// check me
		.replace("WIDTH", amp.size.min(16)*0.75)
		.replace("HEIGHT", ((amp.size.asFloat/16).max(3))*0.75) ;
		var content = "" ;
		var f = File(path, "w") ;
		// reverse: we start from highest notes, down to bass
		content = template.replace("TREBLE", data[0])
		//.replace("BASS", data[1])
		.replace("\\new Staff {", "\\new Staff { \\tempo 4 =TEMPO\n".replace("TEMPO", tempo.asInteger)) ;
		f.write(content); f.close ;
	}


	makeSonagramChordClass {|sonagraph, thresh = -30, fromBin = 0, toBin, ext = "png", res = 72|
		toBin = if (toBin.isNil){sonagraph.amp.size-1}{toBin} ;
		{
			this.makeLilyChordClass(sonagraph, thresh, fromBin, toBin, "/tmp/sonoChordClassLily.ly") ;
			1.wait ;
			this.renderLily("/tmp/sonoChordClassLily.ly", ext:ext, res:res);
		}.fork

	}


/*
	showSonagramChord {|sonagraph, thresh = -30, fromBin = 0, toBin, res = 72
		width = 900, height = 600, buffer|
		var im, w, u, b, x ;
		var playSonoChord, stopSonoChord, pianoRt ;
		var boost = 0, amp = -30.dbamp ;
		//var width, height ;
		toBin = if (toBin.isNil){sonagraph.amp.size-1}{toBin} ;

		// internal func  usage, dirty
		playSonoChord = { |thresh = -30, fromBin = 0, toBin|
			if (pianoRt.notNil){pianoRt.stop} ;
			pianoRt = {
				var playing = [] ;
				sonagraph.sonoToChord(thresh, fromBin,toBin).do{|chord|
					if (chord.size>0) {
						chord.do{|note|
							if (playing.includes(note[0]).not){
								Synth(\mdaPiano,
									[\freq, note[0].midicps,
										\mul, (note[1]+boost).dbamp]
							) }
						}
					} ;
					playing = chord.collect{|i| i[0]} ;
					sonagraph.anRate.reciprocal.wait
				}
			}.fork ;
		} ;

		stopSonoChord = { if (pianoRt.notNil){pianoRt.stop} } ;

		{
			this.makeLilyChordClass(sonagraph, thresh, fromBin, toBin, "/tmp/sonoChordLily.ly") ;
			1.wait ;
			this.renderLily("/tmp/sonoChordClassLily.ly", res:res) ;
			2.wait ;
			im = Image.new("/tmp/sonoChordClassLily.png");
			im.interpolation = 'smooth';//.postln;
			// a bit shaky
			width = if (im.width>width){width}{im.width} ;
			height = if (im.height>height){height}{im.height} ;
			w = Window.new("", Rect(0, 800,  width, height), scroll:true).front;
			u = UserView(w, Rect(0,0, im.width, im.height)) ;
			u.backgroundImage_(im) ;
			b = Button(w, Rect(10, 10, 50, 30))
			.font_(Font("DIN Condensed", 10))
			.states_([["play", Color.red, Color.white],["stop", Color.white, Color.red]])
			.action_{|me|
				if(me.value==1)
				{
					playSonoChord.(thresh, fromBin, toBin) ;
					x = Synth(\player, [
						\buf, buffer,
						\start,
						fromBin.linlin(0, sonagraph.amp.size,
							0, buffer.numFrames),
						\dur, (toBin-fromBin).linlin(0, sonagraph.amp.size,0,
							buffer.numFrames/Server.local.sampleRate),
						\amp, amp]) ;
					NodeWatcher.register(x);
				}
				{
					stopSonoChord.();
					if(x.isRunning){x.free}
				}
			} ;
			StaticText(w, Rect(10, 50, 100, 20)).string_("spec")
			.font_("DIN Condensed", 8);
			StaticText(w, Rect(10, 150, 100, 20)).string_("snd")
			.font_("DIN Condensed", 8);
			Slider(w, Rect(10, 70, 20, 80))
			.action_{|me|
				boost = me.value.linlin(0.0, 1, -5, 15) ;
				amp = me.value.linlin(0.0, 1, 10, -30).dbamp ;
				x.set(\amp, amp) ;
			}.valueAction_(1) ;
		}.fork(AppClock)
	}
*/

}

/*
SonaGraph.prepare ;

a = SonaGraph.new ;
// read the log, may requires some time
a.readArchive("/Users/andrea/musica/scores/fegato/goodTimes.log") ;



SonaGraphLily.new.makeLilyChordClass(a, -26, path:"/tmp/sonoLilyClass.ly")

SonaGraphLily.new.makeSonagramChordClass(a, -26, ext:"pdf", res:nil)
*/
*/