
SonaGraphLily {

	/*
	*new { arg graphDict = IdentityDictionary.new ;
	^super.new.initSonaGraphLily(graphDict)
	}

	initSonaGraphLily { arg aGraphDict ;

	}
	*/

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

	makeQuarter {|qSeq, midinote|
		var symb, str ;
		qSeq.do{|i|
			str = if(i==96.neg){str++"r1@"}{str++"n1@"}
		} ;
		// this is a damn cool trick
		str = str
		.replace("r1@r1@r1@r1@", "r4@")
		.replace("n1@n1@n1@n1@", "n4@")
		.replace("r1@r1@r1@", "r3@")
		.replace("n1@n1@n1@", "n3@")
		.replace("r1@r1@", "r2@")
		.replace("n1@n1@", "n2@") ;
		^str
	}

	makeVoice {|vSeq, midinote|
		var str ;
		vSeq.do{|qSeq|
			str = str++(this.makeQuarter(qSeq, midinote))
		} ;
		^str
	}

	createLilyVoice {|voice, midinote|
		var arr = voice.split($@).select{|i| i.size>0} ;
		var str = "" ;
		var measure = 0 ;
		var dur ;
		var note = this.createLilyNote(midinote) ;
		(arr.size).do{|i|
			measure = measure + arr[i][1].asString.asInteger ;
			str = str + arr[i]
			.replace("1", "16")
			.replace("2", "8")
			.replace("3", "8.") ;
			if(i<= (arr.size-2)){
				if(
					(arr[i+1][0].asSymbol == arr[i][0].asSymbol).and
					(arr[i+1][0].asSymbol != \r)
				)
				{ str = str++"~" }
			};
			if(measure == 16) { str = str + "|\n"; measure = 0}
		};
		^str
		// grouping, another trick working, luckily
		.replace("r4 r4 r4 r4", "r1")
		.replace("r4 r4 r4", "r2.")
		.replace("r4 r4", "r2")
		.replace("n4~ n4~ n4~ n4", "n1")
		.replace("n4~ n4~ n4", "n2.")
		.replace("n4~ n4", "n2")
		.replace("n", note)
	}

	//~createLilyVoice.(~vstr, 61)

	createLilyVoices { |amp, thresh = -40|
		var data = amp.flop
		.collect{|i| i.collect{|amp| if (amp <= thresh) {-96}{amp}}
			.clump(4)} ;
		var strings = [] ;
		var clef ;
		^data.collect{|layer, i|
			if(layer.flat.asSet != Set[-96]){
				clef = if ((i+21) >= 60){"treble\n"}{"bass\n"} ;
				"\\new Staff {\n"++
				"\\clef"+clef++
				this.createLilyVoice(this.makeVoice(layer, i+21), i+21)
				++ "}\n" ;
			}{ nil }
		}.select{|i| i.notNil} ;
	}

	makeLilyFile {|data, amp, tempo = 60, path|
		var template = "
\\version \"2.18.2\"


\\header{
tagline = \"\"  % removed

}

#(set! paper-alist (cons '(\"my size\" . (cons (* WIDTH in) (* HEIGHT in))) paper-alist))

\\paper {
#(set-paper-size \"my size\")
}

\\score {
<<

% CONTENT

>>
}
".replace("HEIGHT", data.size*0.75).replace("WIDTH", (amp.size/4)*0.75) ;
		var content = "" ;
		var f = File(path, "w") ;
		// reverse: we start from highest notes, down to bass
		data.reverse.do{|v, i|
			if (i == 0){
				v = v.replace("\\new Staff {", "\\new Staff { \\tempo 4 =TEMPO\n".replace("TEMPO", tempo))} ;
			content = content++v
		} ;
		template = template.replace("% CONTENT", content) ;
		f.write(template); f.close ;
	}


	makeLily {|sonagraph, thresh = -40, fromBin = 0, toBin, path|
		var tempo, data ;
		toBin = if (toBin.isNil){sonagraph.amp.size-1}{toBin} ;
		// no float, on macosx worked, ??
		tempo = (60/(sonagraph.anRate.reciprocal*4)).asInteger ;
		data = this.createLilyVoices(sonagraph.amp[fromBin..toBin], thresh) ;
		this.makeLilyFile(data, sonagraph.amp[fromBin..toBin], tempo, path)
	}

	renderLily {|path, ext = "png", res|
		// linux vs OSX, no win
		var pt = if(thisProcess.platform.name == \linux)
		{"lilypond  -fEXT RES --output="}
		{"Applications/LilyPond.app/Contents/Resources/bin/lilypond  -fEXT RES --output="} ;
		path = if (path.isNil){"/tmp/sonoLily.ly"}{path} ;
		res = if (res.notNil){"-dresolution=RES".replace("RES", res)}{""} ;
		(
			// OSX only!
			//"Applications/LilyPond.app/Contents/Resources/bin/lilypond  -fEXT RES --output="++path.splitext[0] + path
			//"lilypond  -fEXT RES --output="++path.splitext[0] + path
			pt++path.splitext[0] + path
		).replace("EXT", ext).replace("RES", res.postln).postln.unixCmd
	}


	makeSonagram {|sonagraph, thresh = -30, fromBin = 0, toBin, ext = "png", res = 72|
		toBin = if (toBin.isNil){sonagraph.amp.size-1}{toBin} ;
		{
			this.makeLily(sonagraph, thresh, fromBin, toBin, "/tmp/sonoLily.ly") ;
			1.wait ;
			this.renderLily(ext:ext, res:res);
		}.fork

	}



	showSonagram {|sonagraph, thresh = -30, fromBin = 0, toBin, res = 72
		width = 800, height = 600, buffer|
		var im, w, u, b, x ;
		//var width, height ;
		var playSonoChord, stopSonoChord, pianoRt ;
		var boost = 0, amp = -30.dbamp ;

		toBin = if (toBin.isNil){sonagraph.amp.size-1}{toBin} ;
		// internal func usage, dirty
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
			// clean up
			if (File.exists( "/tmp/sonoLily.ly"))
			{ File.delete( "/tmp/sonoLily.ly") } ;
			if (File.exists( "/tmp/sonoLily.png"))
			{ File.delete( "/tmp/sonoLily.png") } ;
			//not sure if it's synchronous
			while {File.exists( "/tmp/sonoLily.ly")
			&& 	File.exists( "/tmp/sonoLily.png")} { 0.1.wait } ;
			this.makeLily(sonagraph, thresh, fromBin, toBin, "/tmp/sonoLily.ly") ;
			//1.wait ;
			while { File.exists( "/tmp/sonoLily.ly").not }{ 0.1.wait } ;
			this.renderLily(res:res) ;
			while { File.exists( "/tmp/sonoLily.png").not }{ 0.1.wait }  ;
			//2.wait ;
			im = Image.new("/tmp/sonoLily.png");
			im.interpolation = 'smooth';
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

	// Chord representation rather then voices
	// more compact but piano stuff might be messy

	// only pitches, no durs
	createLilyChord {|chord|
		var chor = [], ch = "" ;
		chord.do{|midi|
			chor = chor.add(this.createLilyNote(midi))
		} ;
		chor.do{|n| ch = ch+n};
		ch = if (chord.size > 0) { "<"+ch+">" }{"r"};
		^ch
	}

	// given a chord, split it into treble and bass
	splitChordStruct {|chord, pThresh = 60| // middle c
		^[chord.select{|i| i>= pThresh}, chord.select{|i| i< pThresh}]
	}

	// given amp and thresh, convert into a chord
	collectChords {|amp, thresh|
		^amp.collect{|i|
			HarmoSpectrum.newFrom(i).overChord(thresh) ;
		}
	}

	// split all bins from amp, then flop to group voiced
	splitIntoVoices {|amp, thresh, pThresh = 60|
		^this.collectChords(amp, thresh).collect{|i|
			this.splitChordStruct(i, pThresh)
		}.flop
		// treble and bass
	}

	// voice is a layer, treble or bass
	createSequence {|voice|
		var cnt = 0;
		var actual, chordStr = "" ;
		var durs = ["16","8","8.","4"] ;
		var v = voice.collect{|i| i.sort} ;
		// maybe because of this, it is a bit shorter than
		// sonogram as voices. Neverthelesss, last bins are alwasy silence
		v[..v.size-2].do{|i, j|
			if(v[j+1] == i ) {
				if(j%4 == 3) {
					chordStr = chordStr+this.createLilyChord(i)
					++durs[cnt] ;
					// not a pause
					if((i != []).and(v[j+1] != [])){ chordStr = chordStr++"~" };
					chordStr = chordStr++"\n" ;
					cnt = 0;
				}{
					cnt = cnt + 1
				}
			}{
				chordStr = chordStr+this.createLilyChord(i)
				++durs[cnt] ;
				// intersection between two adjacent chord is not empty
				if((i.asSet & v[j+1].asSet) != Set[])
				{ chordStr = chordStr++"~" } ;
				chordStr = chordStr++"\n" ;
				cnt = 0
			} ;
			if(j%16 == 15){ chordStr = chordStr+"|\n" }
		} ;
		^chordStr
	}


	makeLilyChord {|sonagraph, thresh, fromBin, toBin, path|
		var v, data, tempo ;
		toBin = if (toBin.isNil){sonagraph.amp.size-1}{toBin} ;
		v = this.splitIntoVoices(sonagraph.amp[fromBin..toBin], thresh) ;
		data = [this.createSequence(v[0]), this.createSequence(v[1])] ;
		// on macosx it worked as float, ??
		tempo = (60/(sonagraph.anRate.reciprocal*4)).asInteger ;
		this.makeLilyChordFile(data, sonagraph.amp, tempo, path)
	}



	makeLilyChordFile {|data, amp, tempo = 60, path|
		var template = "
\\version \"2.18.2\"

\\header {
tagline = \"\"  % removed
}

#(set! paper-alist (cons '(\"my size\" . (cons (* WIDTH in) (* HEIGHT in))) paper-alist))

\\paper {
#(set-paper-size \"my size\")
}

\\score {
<<
\\new PianoStaff

\\new Staff {
\\set fontSize = -1
\\time 4/4

TREBLE

}

\\new Staff

{\\clef bass

\\set fontSize = -1
\\time 4/4

BASS

\\bar \"|.\"

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
		.replace("BASS", data[1])
		.replace("\\new Staff {", "\\new Staff { \\tempo 4 =TEMPO\n".replace("TEMPO", tempo)) ;
		f.write(content); f.close ;
	}


	makeSonagramChord {|sonagraph, thresh = -30, fromBin = 0, toBin, ext = "png", res = 72|
		toBin = if (toBin.isNil){sonagraph.amp.size-1}{toBin} ;
		{
			this.makeLilyChord(sonagraph, thresh, fromBin, toBin, "/tmp/sonoChordLily.ly") ;
			// should be made synchronous
			1.wait ;
			this.renderLily("/tmp/sonoChordLily.ly", ext:ext, res:res);
		}.fork

	}


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
						// clean up
			if (File.exists( "/tmp/sonoChordLily.ly"))
			{ File.delete( "/tmp/sonoChordLily.ly") } ;
			if (File.exists( "/tmp/sonoChordLily.png"))
			{ File.delete( "/tmp/sonoChordLily.png") } ;
			// not sure if it's synchronous
			while {File.exists( "/tmp/sonoChordLily.ly")
			&& 	File.exists( "/tmp/sonoChordLily.png")} { 0.1.wait } ;
			this.makeLilyChord(sonagraph, thresh, fromBin, toBin, "/tmp/sonoChordLily.ly") ;
			while { File.exists( "/tmp/sonoChordLily.ly").not } { 0.1.wait } ;
			this.renderLily("/tmp/sonoChordLily.ly", res:res) ;
			while {File.exists( "/tmp/sonoChordLily.png").not} { 0.1.wait } ;
			im = Image.new("/tmp/sonoChordLily.png");
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


}


/*
SonaGraph.prepare ;
// something to analyze, i.e a buffer
~path = Platform.resourceDir +/+ "sounds/a11wlk01.wav";
~sample = Buffer.read(s, ~path).normalize ;

// an istance
a = SonaGraph.new;
// now analyzing in real-time
a.analyze(~sample,15) ; // high rate!
|//
//SonaGraphLily.new.makeLily(a, -30, 45, 60,"/tmp/sonoLily.ly")
//SonaGraphLily.new.renderLily(res:50)
SonaGraphLily.new.showSonagram(a, -30)
//SonaGraphLily.new.makeSonagram(a, -30, ext:"png")
SonaGraphLily.new.showSonagramChord(a, -30, buffer:~sample)

SonaGraphLily.new.makeLilyChord(a, -30, path:"/Users/andrea/Desktop/sonoChord.ly")
SonaGraphLily.new.makeSonagramChord(a, -40, ext:"pdf")

~path ="/Users/andrea/musica/regna/fossilia/compMine/erelerichnia/fragm/octandreExc2.aif"; ~sample = Buffer.read(s, ~path).normalize ;
SonaGraphLily.new.makeLilyChord(a, -30, path:"/Users/andrea/Desktop/sonoChord.ly")

SonaGraphLily.new.makeSonagramChord(a, -35,  ext:"pdf")

// here we start up server and defs
SonaGraph.prepare ;

// something to analyze, i.e a buffer
~path = Platform.resourceDir +/+ "sounds/a11wlk01.wav";
~sample = Buffer.read(s, ~path).normalize ;

// an istance
a = SonaGraph.new ;
a.gui(hStep:6)
a.showSonagramChord(-40, fromBin: 10)
SonaGraphLily.new.showSonagramChord(a, -40, buffer:~sample)
a.showSonagramChord(-30)
a.showSonagram(-30)
*/