// GUI class for plotting and interactive usage
SonaGraphGui {

	var <>sonaGraph ;
	var <>sf, <>buffer ;
	var <>amp, <>pitch, <>anRate ;
	var <>thresh ;
	var <>w, <>u ; // user window for sonagraph
	var <>hStep, <>vStep ;
	var <>cursor, <>cursorView, <>display, <>sfView;
	var <>attOn ;


	*new { arg sonaGraph, buffer, hStep = 2.5, vStep = 6 ;
		^super.new.initSonaGraphGui(sonaGraph, buffer, hStep, vStep)
	}

	initSonaGraphGui { arg aSonaGraph, aBuffer,
		aHStep, aVStep ;
		sonaGraph = aSonaGraph ;
		buffer = aBuffer ;
		hStep = aHStep; vStep = aVStep ;
		sf = SoundFile.new ;
		sf.openRead(buffer.path) ;
		amp = sonaGraph.amp;
		pitch = sonaGraph.pitch;
		anRate = sonaGraph.anRate;
		attOn = false ;
	}

	makeGui { |thresh = -96, sfViewH = 100, labView = 20, labStep = 5,
		pitchOn = true|
		// thresh: used to select active band AND pitch
		var flag, player, binBar ;
		w = Window("SonaGraph",
			Rect(10, 100, hStep*amp.size, vStep*amp[0].size+sfViewH+labView))
		.background_(Color.gray)
		.front ;
		u = UserView(w, Rect(0, 0, hStep*amp.size, vStep*amp[0].size))
		.background_(Color.white)
		.drawFunc_{
			Pen.strokeColor_(Color.new255(255, 127, 0)) ;//dark orange1
			sonaGraph.att.do{|i|
				Pen.line((i*hStep)@0, (i*hStep)@u.bounds.height)
			} ;
			Pen.stroke ;
			amp.do{|pitchArr,i|
				pitchArr.do{|p, j|
					if (p > thresh){
						Pen.fillColor_(Color.gray(p.linlin(thresh, -10, 0.9, 0))) ;
						Pen.fillOval(Rect(i*hStep,
							vStep*amp[0].size-(j*vStep)-(vStep*0.5),
							hStep, vStep))
					}
				}
			} ;
			amp[0].size.do{|i|
				if (i%12 == 0){
					Pen.strokeColor_(Color.hsv(0.4, 0.9, 0.8,0.5)) ;
					Pen.line(0 @ (vStep*(i+1)), u.bounds.width @ (vStep*(i+1)) ) ;
					Pen.stroke
				}
			} ;

			Pen.fillColor_(Color.red) ;
			if(pitchOn){
				pitch.do{|p, i|
					if(amp[i][p-21] > thresh){
						Pen.fillOval(Rect(i*hStep,
							vStep*amp[0].size-((p-21)*vStep)-(vStep*0.5),
							hStep, vStep))
					}
				}
			}
		} ;
		cursor = [0,0]; flag = true ;
		cursorView =  UserView(w, Rect(0, 0, hStep*amp.size, vStep*amp[0].size))
		.background_(Color(1,1,1,0));
		display = StaticText(w, Rect(5, 5, 200, 15))
		.font_(Font("DIN Condensed", 12)).align_(\left) ;
		cursorView.mouseDownAction_{|view, x, y, mod|
			var pitch = ((88+21)-(y/vStep)).round ;
			//var time = (x/amp.size).round(0.001) ;
			// should be related to buf dur
			var time = x.linlin(0, u.bounds.width,
				0, buffer.numFrames/Server.local.sampleRate).round(0.001) ;
			Synth(\mdaPiano, [\freq, pitch.midicps]) ;
			display.string_((time.asString++"    M:"
				++pitch.asString
				++"  N:"++pitch.midinote.toUpper.replace(" ", "")
				++"  F:"++pitch.midicps.round(0.01).asString)
			);
			cursor = [x,y];
			cursorView.refresh ;
		}
		.drawFunc_{
			//~display.bounds_(Rect(~cursor[0]+3, ~cursor[1]+3, 100, 15));
			display.bounds_(Rect(cursor[0]-23, cursorView.bounds.height-15, 200, 15));
			Pen.strokeColor = Color.red ;
			Pen.line(0 @ cursor[1], cursorView.bounds.width @ cursor[1]);
			Pen.line(cursor[0] @ 0, cursor[0] @ cursorView.bounds.height);
			Pen.stroke
		}
		.keyDownAction_{ |doc, char, mod, unicode, keycode, key|
			if ((unicode == 32)&&(flag == true)){
				flag = false ;
				player = Synth(\player, [\buf, buffer,
					\start, cursor[0]
					.linlin(0, cursorView.bounds.width, 0, sf.numFrames),
					\dur, buffer.numFrames -
					cursor[0]
					.linlin(0, cursorView.bounds.width, 0, sf.numFrames)
				])
			}
			{
				flag = true;
				player.free;
			}
		} ;

		binBar = UserView.new(w, Rect(0, cursorView.bounds.height, hStep*amp.size, labView))
		.background_(Color.grey(0.9))
		.drawFunc_{
			amp.size.do{|p, i|
				if(i%labStep == 0){
					Pen.strokeColor = Color.red ;
					Pen.line( i*hStep@0, i*hStep@5) ;
					Pen.stringAtPoint(i.asString,i*hStep@12,Font("DIN Condensed", 8), Color.red)
				};
				Pen.stroke ;
			}

		}
		;
		sfView = SoundFileView.new(w, Rect(0, cursorView.bounds.height+labView, hStep*amp.size, sfViewH))
		.gridColor_(Color.gray(0.3)) ;

		sfView.soundfile = sf;            // set soundfile
		sfView.read(0, sf.numFrames);     // read in the entire file.
		sfView.refresh;

	}
}