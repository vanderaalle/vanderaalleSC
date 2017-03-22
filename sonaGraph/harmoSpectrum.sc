// HarmoSpectrum encapsulate some funcs related to
// manipulation of an array of amps

HarmoSpectrum {

	var <>spectrum ;

	// start with an array of amps
	*newFrom { arg spectrum ;
		^super.new.initHarmoSpectrum(spectrum)
	}

	initHarmoSpectrum { arg aSpectrum ;
		spectrum = aSpectrum ;
	}

	plotSpectrum  {|step = 10|
		var w = Window.new("spectrum",
			Rect(100, 100, step*88, 96*4)).front ;
		w.drawFunc = {
			Pen.font = Font( "DIN Condensed", step );
			Array.series(9, -10, -10).do{|i,j|
				Pen.strokeColor = Color.gray(0.5) ;
				Pen.line(
					Point(0, i.neg*4),
					Point(w.bounds.width, i.neg*4)
				) ;
				Pen.stroke ;
				Pen.fillColor = Color.red ;
				Pen.stringAtPoint(i.asString,Point(0, i.neg*4)) ;
				Pen.fill
			} ;
			Pen.stroke ;
			spectrum.do{|i,j|
				Pen.fillColor = Color.red ;
				if(((j+21)%12) == 0){
					Pen.stringAtPoint(
						((j+21).midinote.last).asString, Point(step*j+step, 10)) ;
					Pen.fillStroke ;
					Pen.strokeColor = Color.gray(0.5) ;
					Pen.line(
						Point(step*j, 0),
						Point(step*j, 96*4)
					) ;
					Pen.stroke ;
				} ;
				Pen.strokeColor = Color.white ;
				Pen.line(
					Point(step*j, i.neg*4),
					Point(step*j, 96*4)
				) ;
				Pen.stroke ;
				Pen.addOval(
					Rect(step*j-(step*0.25), i.neg*4, step*0.5, step*0.5)
				) ;
				Pen.fill ;
				Pen.fillColor = Color.black ;
				Pen.stringAtPoint((j+21).midinote.toUpper[..1],Point(step*j-(step*0.5), i.neg*4+step)) ;
				Pen.stringAtPoint((j+21).asString,Point(step*j-(step*0.5), i.neg*4-step)) ;
				Pen.fill
			} ;
		} ;
		w.view.mouseDownAction_{|view, x, y, mod|
			var pitch =
			x.linlin(0, view.bounds.width, 0, 88).round + 21;
			Synth(\mdaPiano, [\freq, pitch.midicps]) ;
		}
	}

	// calculate the maxima arr as a num of spectral maxima and db
	specMaxima { |num = 4|
		var amps  ;
		var maxima = [] ;
		//if (spectrum.isNil){ this.calculateSpectrum } ;
		// we do a copy because of sort
		amps = spectrum.collect{|i| i};
		amps = amps.sort.reverse[..(num-1)] ;
		amps.do{|amp|
			maxima =
			maxima.add([spectrum.indexOf(amp)+21, amp]) ;
		} ;
		^maxima
	}

	// gives you back the chord of maxima, pitches and no dbs
	maximaChord { |num = 4| ^this.specMaxima(num).collect{|i| i[0]} }


	// calculate the over arr of spectral peaks over thresh and db
	specOver { |thresh = -30|
		var amps  ;
		var over = [] ;
		// we do a copy because of sort
		amps = spectrum.collect{|i| i};
		amps = amps.select{|i| i >= thresh} ;
		amps.do{|amp|
			over =
			over.add([spectrum.indexOf(amp)+21, amp]) ;
		} ;
		^over
	}

	// gives you back the chord of maxima, pitches and no dbs
	overChord { |thresh = -30| ^this.specOver(thresh).collect{|i| i[0]} }


	// plays back the maxima chord, db weighted
	playMaxima {|maxima, boost = 20| // lotta dbs coz typically low
		maxima.do{|i|
			Synth(\mdaPiano,
				[\freq, i[0].midicps, \mul, (i[1]+boost).dbamp])
		};
	}

	// spec to lily
	// PRIVATE
	createLilyNote {|midi|
		var post = "" ;
		var name = midi.midinote[..1] ;
		var oct = midi.midinote[2].asString.asInteger ;
		name = name.replace(" ", "").replace("#", "is") ;
		if (oct >= 5){
			(oct-4).do{|i|
				post = post++"'"
			}
		}{
			(4-oct).do{
				post = post++","
			}
		};
		^name++post;
	}

	createLilyChord  {|chord, dur=4|
		var treble = [], tCh = "" ;
		var bass = [], bCh = "";
		chord.postln.do{|midi|
			if (midi >= 60){
				treble = treble.add(this.createLilyNote(midi))
			}{
				bass = bass.add(this.createLilyNote(midi))
			}
		};
		if (treble == []) {
			tCh = "	\\hideNotes c'DUR \\unHideNotes  \\override Stem.transparent = ##t".replace("DUR",dur )
		}{
			treble.do{|n| tCh = tCh+n}};
		if (bass == []) {
			bCh = "	\\hideNotes c,DUR \\unHideNotes  \\override Stem.transparent = ##t".replace("DUR",dur )
		}{
			bass.do{|n| bCh = bCh+n}};
		if (bass.size > 0) { bCh = "<"+bCh+">"++dur };
		if (treble.size > 0) { tCh = "<"+tCh+">"++dur };
		^[tCh, bCh]
	}

	// this is for sonagraph sequence
	// should this graphic method move out?
	createLilyChordNoHide {|chord, dur, xtra|
		var treble = [], tCh = "" ;
		var bass = [], bCh = "";
		dur = if(dur.isNil) {""} {dur} ;
		xtra = if(xtra.isNil){""}{xtra} ;
		xtra = if(chord = [] ){""}{xtra} ;
		chord.postln.do{|midi|
			if (midi >= 60){
				treble = treble.add(this.createLilyNote(midi))
			}{
				bass = bass.add(this.createLilyNote(midi))
			}
		};
		treble.do{|n| tCh = tCh+n};
		bass.do{|n| bCh = bCh+n};
		bCh = if (bass.size > 0) { "<"+bCh+">"++dur++xtra }
		{"r"++dur.asString} ;
		tCh = if (treble.size > 0) { "<"+tCh+">"++dur++xtra}
		{"r"++dur.asString} ;
		^[tCh, bCh]
	}


	writeLilyChord {|chord, path|
		var treble = "" ;
		var bass = "" ;
		var score ;
		var lilyChFile, ch ;
		path = if (path.isNil){"/tmp/spectrumLily.ly"}{path} ;
		lilyChFile = File(path,"w") ;
		score = "

\\version \"2.19.2\"

\\header {
tagline = \"\"  % removed
}

#(set! paper-alist (cons '(\"my size\" . (cons (* 1.5 in) (* 2.5 in))) paper-alist))

\\paper {
#(set-paper-size \"my size\")
}

\\score {
<<
\\new PianoStaff

<<
\\new Staff

{\\override Staff.TimeSignature #'stencil = ##f
\\override Stem.transparent = ##t
\\set fontSize = -1
\\time 1/4

TREBLE



}

\\new Staff

{\\clef bass
\\override Staff.TimeSignature #'stencil = ##f
\\override Stem.transparent = ##t


\\set fontSize = -1
\\time 1/4

BASS


\\bar \"|.\"

}
>>
>>
}
" ;
		ch = this.createLilyChord(chord) ;
		treble = treble + ch[0]++"\n" ;
		bass = bass +ch[1] ++"\n" ;
		score = score.replace("TREBLE", treble)
		.replace("BASS", bass)
		;
		lilyChFile.write(score);
		lilyChFile.close;
	}

	// PUBLIC
	specToLily {|maximaChord, path|
		this.writeLilyChord(maximaChord, path)
	}

	renderLily {|path|
		path = if (path.isNil){"/tmp/spectrumLily.ly"}{path} ;
		(
			"Applications/LilyPond.app/Contents/Resources/bin/lilypond  -fpng --output="++path.splitext[0] + path
		).unixCmd
	}

	showSpectrumChord { |num = 6|
		var im, w ;
		var maxima = this.specMaxima(num) ;
		{
			this.specToLily(this.maximaChord(num)) ;
			1.wait ;
			this.renderLily ;
			1.wait ;

			im = Image.new("/tmp/spectrumLily.png");

			w = Window.new("", Rect(400, 400, 100, 180));
			w.view.background_(Color.white);
			w.view.backgroundImage_(im);
			w.front;
			w.view.mouseDownAction_{this.playMaxima(maxima)}
		}.fork(AppClock)
	}

	// END OF SPECTRUM METHODS
}

/*
SonaGraph.prepare ;
// something to analyze, i.e a buffer
~path ="/Users/andrea/musica/regna/fossilia/compMine/erelerichnia/fragm/snd/vareseOctandreP18M5N[8,9,0,7,11,6].aif"; ~sample = Buffer.read(s, ~path).normalize ;

// an istance
a = SonaGraph.new;
// now analyzing in real-time
a.analyze(~sample,15) ; // high rate!
a.gui
h = HarmoSpectrum.newFrom(a.calculateAvSpectrum)

h = HarmoSpectrum.newFrom(a.calculateAvSpectrum(79,95))
h.spectrum
h.plotSpectrum
h.specMaxima(6)
h.maximaChord(4)
h.showSpectrumChord(4)
*/