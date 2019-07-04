// Still another variation on SonaGraphGui

SpectraDawGui {

	var <>spec ;
	var <>spectraDaw ;
	var <>spec, <>rate ;
	var <>thresh ;
	var <>w, <>u ; // user window for sonagraph
	var <>hStep, <>vStep, <>width ;
	var <>cursor, <>cursorView, <>display, <>sfView;
	var <>boost ;
	// selection support
	var <>selected, <>from, <>to ;


	*new { arg spec, hStep = 2.5, vStep = 6, width ;
		^super.new.initSonaGraphGui(spec, hStep, vStep, width)
	}

	initSonaGraphGui { arg aSpec,
		aHStep, aVStep, aWidth ;
		spec = aSpec ;
		hStep = aHStep; vStep = aVStep ;
		rate = 10; //default
		width = aWidth ;
		thresh = -40 ; // we use it to scale grey
		boost = 15
	}

	makeGui { |sfViewH = 100, labView = 20, labStep = 5,
		pitchOn = true|
		// thresh: used to select active band AND pitch
		var flag, player, binBar ;
		var col ;
		// if width is passed then it is used and the window will scroll if shorter
		var ww = if (width.isNil){hStep*spec.size}{width} ;
		w = Window("SonaGraph",
			Rect(10, 100, ww, vStep*spec[0].size+sfViewH+labView), scroll:true)
		.background_(Color.gray)
		.front ;
		u = UserView(w, Rect(0, 0, hStep*spec.size, vStep*spec[0].size))
		.background_(Color.white)
		.drawFunc_{
			spec.do{|pitchArr,i|
				pitchArr.do{|p, j|
					if (p > -96){ // hard coded thresh for spec
						case
						{ (from.isNil).or(to.isNil)}
						{ col = Color.gray(p.linlin(thresh, -10, 0.9, 0)) }
						{ (i >= from).and(i <= to) }
						{ col = Color.hsv(0.6, 1, p.linlin(thresh, -10, 0.9, 0)) }
						{ (i < from).or(i > to) }
						{ col = Color.gray(p.linlin(thresh, -10, 0.9, 0)) } ;
						Pen.fillColor_(col) ;
						Pen.fillOval(Rect(i*hStep,
							vStep*spec[0].size-(j*vStep)-(vStep*0.5),
							hStep, vStep))
					}
				}
			} ;
			spec[0].size.do{|i|
				if (i%12 == 0){
					Pen.strokeColor_(Color.hsv(0.4, 0.9, 0.8,0.5)) ;
					Pen.line(0 @ (vStep*(i+1)), u.bounds.width @ (vStep*(i+1)) ) ;
					Pen.stroke
				}
			} ;

			Pen.fillColor_(Color.red) ;
		} ;
		cursor = [0,0]; flag = true ;
		cursorView =  UserView(w, Rect(0, 0, hStep*spec.size, vStep*spec[0].size))
		.background_(Color(1,1,1,0));
		display = StaticText(w, Rect(5, 5, 200, 15))
		.font_(Font("DIN Condensed", 12)).align_(\left) ;
		cursorView.mouseDownAction_{|view, x, y, mod|
			var pitch = ((88+21)-(y/vStep)).round ;
			//var time = (x/amp.size).round(0.001) ;
			// should be related to bin pos
			var time = x.linlin(0, u.bounds.width,
				0, spec.size).asInteger ;
			Synth(\mdaPiano, [\freq, pitch.midicps]) ;
			display.string_((
			time.asString++
			"    M:"
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
		// to be done
		.keyDownAction_{ |doc, char, mod, unicode, keycode, key|
			if ((unicode == 32)&&(flag == true)){
				var from = cursor[0].linlin(0, cursorView.bounds.width,
				0, spec.size).asInteger ;
				player = SD.play(spec, rate, boost, from) ;
				flag = false ;
			}
			{
				flag = true;
				player.stop;
			}
		} ;
		binBar = UserView.new(w, Rect(0, cursorView.bounds.height, hStep*spec.size, labView))
		.background_(Color.grey(0.9))
		.drawFunc_{
			spec.size.do{|p, i|
				if(i%labStep == 0){
					Pen.strokeColor = Color.red ;
					Pen.line( i*hStep@0, i*hStep@5) ;
					Pen.stringAtPoint(i.asString,i*hStep@12,Font("DIN Condensed", 8), Color.red)
				};
				Pen.stroke ;
			}

		}

	}
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

// easy way to select
g = SonaGraphGui.new(a, ~sample, hStep:5, vStep:6).makeGui(-40) ;
g.select(40, 55) ; g.drawSelected ;


n = SpectraDaw.from(g.selected, -30)
SpectraDawGui(n, 10).makeGui


SD.check(n)
//n.filterAbove
SD.play(n, 4, 20)
n.spec.size

g.select(0, 40)
m = SpectraDaw.from(g.selected, -30); g.drawSelected ;

SD.play(m, 4, 20)
m.do{|i| i.postln}
m.size

k = SD.mix([n, m])

k = SD.from(k)


k = SD.from(SD.concatenate([m, n]))
SD.play(k, 4, 15)
j = SD.transpose(m, 12)
SD.play(j, 4, 20)

l = SD.transpose(m, -12)

SD.play(l, 4, 15)

h = SD.mix([k, j, l])
SD.play(h, 4, 10)
u = SD.concatenate(Array.fill(6, {h}))
SD.play(u, 4, 10)

SpectraDawGui(u, 10).makeGui
*/
