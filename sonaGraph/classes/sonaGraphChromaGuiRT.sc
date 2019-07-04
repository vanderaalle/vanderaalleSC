// GUI class for plotting chroma and interactive usage

SonaGraphChromaGuiRT {

	var <>sonaGraph ;
	var <>sf ;
	var <>amp, <>pitch, <>anRate ;
	var <>thresh ;
	var <>w, <>u ; // user window for sonagraph
	var <>hStep, <>vStep ;
	var <>cursor, <>cursorView, <>display ;
	//var <>attOn ;
	var <>pos ; // actual position where to draw
	var <>howManyBins ;
	var updtRT, <>chroma ;

	*new { arg sonaGraph, thresh = -96, hStep = 5, vStep = 20, howManyBins = 100  ;
		^super.new.initSonaGraphGuiRT(sonaGraph, thresh, hStep, vStep, howManyBins)
	}

	initSonaGraphGuiRT { arg aSonaGraph, aThresh, aHStep, aVStep, aHowManyBins ;
		var act ;
		sonaGraph = aSonaGraph ;
		thresh = aThresh ;
		//buffer = aBuffer ;
		hStep = aHStep; vStep = aVStep ;
		//sf = SoundFile.new ;
		//sf.openRead(buffer.path) ;
		amp = sonaGraph.amp ;
		pitch = sonaGraph.pitch ;
		anRate = sonaGraph.anRate ;
		pos = 0 ;
		howManyBins = aHowManyBins ;
		act = \C;
		updtRT = Routine{
			inf.do{
				var chord ;
				chroma = HarmoSpectrum.newFrom(sonaGraph.amp.last).chromaAmp ;
				pos = (pos+1) % howManyBins ;
				u.refresh ;
				if(pos == 0){ this.resetPlotting } ;
				chord = ChordAnalyzer.analyze(HarmoSpectrum.newFrom(chroma).overChord(thresh)) ;
				if (chord.notNil){
					ChordAnalyzer.convertName(chord).postln ;
					if(chord.asSymbol != act.postln){
					chord.do{|i|
							Synth(\mdaPiano, [\freq, (i+60).midicps]) ;
						} ;
					act = chord.asSymbol
					}
				} ;
				anRate.reciprocal.wait ;
			}
		} ;
		//attOn = false ;
	}


	makeGui { |sfViewH = 100, labView = 20, labStep = 5|
		// thresh: used to select active band AND pitch
		var  binBar, flag, slider, txt ;
		w = Window("SonaGraph",
			Rect(10, 100, hStep*howManyBins+60, vStep*13+labView))
		.background_(Color.gray)
		.front ;
		Button(w, Rect(hStep*howManyBins+10, 10,
			40, 40))
		.states_([["START", Color.red, Color.grey(0.1)],["STOP", Color.grey(0.1), Color.red]])
		.action_{|me|
			if(me.value == 1){
				this.startPlotting
			}{ this.stopPlotting
		}};
		Button(w, Rect(hStep*howManyBins+10, 60,
			40, 40))
		.states_([["RST", Color.red, Color.grey(0.1)]])
		.action_{ this.resetPlotting} ;
		slider = Slider(w, Rect(hStep*howManyBins+10, 110, 40, 100))
		.value_(thresh.linlin(-96, 0, 0, 1))
		.action_{|me|
			var v =
			me.value.linlin(0, 1, -96, 0).asInteger ;
			thresh = v ;
			txt.string_(v.asString+" dB") ;
		} ;
		txt = StaticText(w, Rect(hStep*howManyBins+10, 220, 40, 40))
		.string_(thresh.linlin(-96, 0, 0, 1).asString+" dB") ;
		u = UserView(w, Rect(0, 0, hStep*howManyBins, vStep*13))
		.clearOnRefresh_(false)
		.background_(Color.white)
		.drawFunc_{
			// this is if attacks exist, skip
			// Pen.strokeColor_(Color.new255(255, 127, 0)) ;//dark orange1
			// sonaGraph.att.do{|i|
			// 	Pen.line((i*hStep)@0, (i*hStep)@u.bounds.height)
			// } ;
			// Pen.stroke ;
			//amp.do{|pitchArr,i|
			chroma.do{|p, j|
				if (p > thresh){
				Pen.fillColor_(Color.hsv(
						hue: j.linlin(0, 11, 0, 0.999),
						sat: p.linlin(-96, -10, 0.0, 1.0),
						val: 1 //p.linlin(-60, -10, 0.0, 1.0)
					));
			//Pen.fillColor_(Color.gray(p.linlin(thresh, -10, 0.9, 0))) ;
					Pen.fillRect(Rect(pos*hStep,
						vStep*12-(j*vStep)-(vStep*0.5),
						hStep, vStep))
				} ;
			} ;
			//} ;
/*			chroma.size.do{|i|
				if (i%12 == 0){
					Pen.strokeColor_(Color.hsv(0.4, 0.9, 0.8,0.5)) ;
					Pen.line(0 @ (vStep*(i+1)), u.bounds.width @ (vStep*(i+1)) ) ;
					Pen.stroke
				}
			} ;*/
		} ;
		cursor = [0,0]; flag = true ;
		cursorView =  UserView(w, Rect(0, 0, hStep*howManyBins, vStep*13))
		.background_(Color(1,1,1,0));
		display = StaticText(w, Rect(5, 5, 20, 15))
		.font_(Font("DIN Condensed", 12)).align_(\left) ;
		cursorView.mouseDownAction_{|view, x, y, mod|
			var pitch = (21-(y/vStep)).round%12+60 ;
			//var time = (x/amp.size).round(0.001) ;
			Synth(\mdaPiano, [\freq, pitch.midicps]) ;
			display.string_((//time.asString++
				"N:"++pitch.midinote.toUpper.replace(" ", "")
				//++"  F:"++pitch.midicps.round(0.01).asString
			)
			);
			cursor = [x,y];
			cursorView.refresh ;
		}

		.drawFunc_{
			//~display.bounds_(Rect(~cursor[0]+3, ~cursor[1]+3, 100, 15));
			display.bounds_(Rect(cursor[0]-23, cursorView.bounds.height-15, 20, 15));
			Pen.strokeColor = Color.red ;
			Pen.line(0 @ cursor[1], cursorView.bounds.width @ cursor[1]);
			Pen.line(cursor[0] @ 0, cursor[0] @ cursorView.bounds.height);
			Pen.stroke
		} ;
		// skip file playback
		// .keyDownAction_{ |doc, char, mod, unicode, keycode, key|
		// 	if ((unicode == 32)&&(flag == true)){
		// 		flag = false ;
		// 		player = Synth(\player, [\buf, buffer,
		// 			\start, cursor[0]
		// 			.linlin(0, cursorView.bounds.width, 0, sf.numFrames),
		// 			\dur, buffer.numFrames -
		// 			cursor[0]
		// 			.linlin(0, cursorView.bounds.width, 0, sf.numFrames)
		// 		])
		// 	}
		// 	{
		// 		flag = true;
		// 		player.free;
		// 	}
		// } ;

		binBar = UserView.new(w, Rect(0, cursorView.bounds.height, hStep*howManyBins, labView))
		.background_(Color.grey(0.9))
		.drawFunc_{
			howManyBins.do{|i|
				if(i%labStep == 0){
					Pen.strokeColor = Color.red ;
					Pen.line( i*hStep@0, i*hStep@5) ;
					Pen.stringAtPoint(i.asString,i*hStep@12,Font("DIN Condensed", 8), Color.red)
				};
				Pen.stroke ;
			}

		}
		;
		//sfView = SoundFileView.new(w, Rect(0, cursorView.bounds.height+labView, hStep*amp.size, sfViewH))
		//.gridColor_(Color.gray(0.3)) ;

		//sfView.soundfile = sf;            // set soundfile
		//sfView.read(0, sf.numFrames);     // read in the entire file.
		//sfView.refresh;

	}

	startPlotting { updtRT.reset.play(AppClock) }

	resetPlotting { pos = 0 ; u.clearDrawing ; u.refresh }

	stopPlotting { updtRT.stop }

}

/*
SonaGraph.prepare ;
b = Bus.audio(s, 1) ;
x = {Out.ar([0, b], Mix(SoundIn.ar([0,1])))}.play
// an istance
a = SonaGraph.new ;
// now analyzing in real-time
a.analyzeRT(4,10, anBus:b) ; // rate depends on dur etc
x.free

a.stopAnalyzingRT

r = SonaGraphChromaGuiRT(a, thresh: -96, hStep: 3, vStep: 20, howManyBins: 300 ).makeGui
r.startPlotting

r.chroma
r.stopPlotting
r.resetPlotting
r.u.clearDrawing
r.thresh = -50
r.thresh
*/