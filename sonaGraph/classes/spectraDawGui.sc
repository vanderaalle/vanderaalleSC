// Still another variation on SonaGraphGui
// this time specialised on spec
// it allows to manipulate selections
// a selection is a boolean mask over a spec


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
	var <>selections ;
	var <>sel ;
	var <>actualSelection ;


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
		boost = 15 ;
		selections = [] ; // an array of selection masks
		sel = [0, 0] ;
		actualSelection = this.newSelection ;
	}

	makeGui { |sfViewH = 100, labView = 20, labStep = 5,
		pitchOn = true, window|
		// thresh: used to select active band AND pitch
		var flag, player, binBar ;
		var col ;
		var adv, advRt ;
		var time ;
		// if width is passed then it is used and the window will scroll if shorter
		var ww = if (width.isNil){hStep*spec.size}{width} ;
		w = if(window.isNil) {
			Window("SonaGraph",
				Rect(10, 100, ww, vStep*spec[0].size+sfViewH+labView), scroll:true)
			.background_(Color.gray)
			.front } { window } ;
		u = UserView(w, Rect(0, 0, hStep*spec.size, vStep*spec[0].size))
		.background_(Color.white)
		.drawFunc_{
			spec.do{|pitchArr,i|
				pitchArr.do{|p, j|
					Pen.fillColor_(Color.new255(255, 127, 0)) ;
					Pen.strokeColor_(Color.red) ;
					Pen.fillOval(Rect(sel[0]*hStep,
						vStep*spec[0].size-(sel[1]*vStep)-(vStep*0.5),
						hStep, vStep)) ;
					Pen.strokeOval(Rect(sel[0]*hStep,
						vStep*spec[0].size-(sel[1]*vStep)-(vStep*0.5),
						hStep, vStep)) ;
					if (p > -96){ // hard coded thresh for spec
						case
						{ selections == []}
						{ col = Color.gray(p.linlin(thresh, -10, 0.9, 0)) }
						{ this.checkSelections(i, j)[0] }
						{ col = Color.hsv(
							this.checkSelections(i,j)[1].linlin(0, selections.size, 0, 0.999)
								, 1, p.linlin(thresh, -10, 0.9, 0)) }
						{ this.checkSelections(i,j)[0] == false }
						{ col = Color.gray(p.linlin(thresh, -10, 0.9, 0)) } ;
						Pen.fillColor_(col) ;
						Pen.fillOval(Rect(i*hStep,
							vStep*spec[0].size-(j*vStep)-(vStep*0.5),
							hStep, vStep))
					}
				}
			} ;
			spec.size.do{|i|
				if(i%16 == 0) {
					Pen.strokeColor_(Color.hsv(0.4, 0.9, 0.8,0.5)) ;
					Pen.line((hStep*i) @ 0, (hStep*i) @ u.bounds.height ) ;
					Pen.stroke
				}
			};
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
			sel = [time, ((88)-(y/vStep)).asInteger].postln ;
			Synth(\mdaPiano, [\freq, (sel[1]+21).midicps]) ;
			display.string_((
			time.asString++
			"    M:"
				++pitch.asString
				++"  N:"++pitch.midinote.toUpper.replace(" ", "")
				++"  F:"++pitch.midicps.round(0.01).asString)
			);
			cursor = [x,y];
			u.refresh ;
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
			[unicode, keycode, key].postln ;
			case
			// classic playback from cursors with spacebar
			{ (unicode == 32)&&(flag == true)}
			{
				var from = cursor[0].linlin(0, cursorView.bounds.width,
				0, spec.size).asInteger ;
				player = SD.play(spec, rate, boost, from) ;
				flag = false ;
				#adv, advRt =  this.makeAdvancement(from, cursor[0]) ;
			} //;
			{ (unicode == 32)&&(flag == false)}
			{
				flag = true;
				player.stop;
				advRt.stop ;
				adv.close ;
			}
			// moving selected point with arrows
			{ keycode == 65361 }
			{
				"back".postln;
				sel = [sel[0]-1, sel[1]];
				u.refresh ;
				cursorView.refresh ;
			}
			{ keycode == 65363 } {
				"forward".postln;
				sel = [sel[0]+1, sel[1]] ;
				u.refresh ;
				cursorView.refresh ;
			}
			{ keycode == 65362 } {
				"up".postln;
				sel = [sel[0], sel[1]+1] ;
				u.refresh ;
				cursorView.refresh ;
			}
			{ keycode == 65364 }
			{
				"down".postln;
				sel = [sel[0], sel[1]-1];
				u.refresh ;
				cursorView.refresh ;
			}
			// with enter the actual flag is written in the actual selection
			{ unicode == 13 }
			{
				"enter".postln ;
				actualSelection[sel[0]][sel[1]] =
				actualSelection[sel[0]][sel[1]].not ;
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

	checkSelections {|bin, pitch|
		var ok = [] ;
		selections.do{|sel, k|
			if (sel[bin][pitch]){ok = ok.add([true, k])}
		};
		if (ok.size != 0 ){^ok[0]}{^[false]}
	}

	// advancement bar
	makeAdvancement { |from, cursorPos|
		var cp = from ;
		var u = UserView(w, Rect(0, 0, hStep*spec.size, vStep*spec[0].size))
		.drawFunc_{
			Pen.strokeColor = Color.cyan(0.7) ;
			Pen.line((cp*hStep) @ 0,
				(cp*hStep) @ cursorView.bounds.height);
			Pen.stroke
		} ;
		var r = {
			(spec.size-from).do{
				cp = (cp +1) ;
				u.refresh ;
				rate.reciprocal.wait ;
			};
			u.close ;
		}.fork(AppClock) ;
		^[u, r]
	}

	// selection

	// a selection is a mask of boolean flags over spec
	// new one is filled with false
	newSelection  {
		^spec.collect{|i|
			i.collect{|j| false}
		}
	}

	// adding a selection
	addSelection {|selection|
		selections = selections.add(selection) ;
		u.refresh ;
	}
	// an array of selections
	addSelections {|selections|
		selections.do{|sel| this.addSelection(sel)}
	}

	// adding an item to selection
	addToSelection {|selection, bin, pitch|
		selection[bin][pitch] = true ;
		u.refresh ;
	}

	// removing an item from selection
	removeFromSelection { |selection, bin, pitch|
		selection[bin][pitch] = false ;
		u.refresh ;
	}

	// selecting in range
	// low and high are midi values
	newSelectionInRange {|from, to, low, high|
		var l, h, newSel ;
		l = low-21 ; h = high -21 ;
		newSel = spec.collect{|i, j|
			if((j >= from).and(j <= to)){
				i.collect{|m, n|
					if((n >= l).and(n <= h))
					{true} {false}
				}
			}{
				i.collect{|m, n| false }
			}
		} ;
		u.refresh ;
		^newSel
	}

	// actual selection is added to selections
	addActualToSelections {
		selections = selections.add(actualSelection) ;
		u.refresh ;
	}

	// the new actual selection can be an existing selection
	// so you will not add it when done
	// or a new one, then you need to add it to selections
	newActualSelection{|selection|
		if(selection.isNil){
			actualSelection = this.newSelection }{
			actualSelection = selection
		};
	}

	// return a new spec from a selection mask
	specFromSelection {|selection|
		^spec.collect{|i, j|
			i.collect{|m, n|
				if (selection[j][n]){m}{-96}
			}
		}
	}

	playSelection {|selection, boost = 15, from = 0|
		^{
			var playing = [] ;
			spec[from..].do{|chord, j|
				chord.do{|dB, i|
					if ((playing.includes(i).not).and(dB > -96).and(selection[j+from][i])){
						Synth(\mdaPiano,
							[\freq, (i+21).midicps,
								\mul, (dB+boost).dbamp]
					) } ;
				} ;
				playing = [] ;
				chord.do{|i, j| if(i > -96){playing = playing.add(j)}} ;
				rate.reciprocal.wait
			}
		}.fork ;

	}

	writeSelection {|selection, path|
		selection.writeArchive(path) ;
	}

	readSelection {|path|
		^Object.readArchive(path)
	}

	unite {}

	complement {}

	// old stuff that has been integrated
/*
	switchToGui {|which = \selector|
		if(which == \selector){
				// clean up
			this.u.close ;
			this.cursorView.close ;
			this.display.close ;
			this.makeSelectorView ;
		}
		// else spectra
		{ this.makeGui (window:w)}
	}

	makeSelectorView {
		var flag, player, binBar ;
		var col ;
		var adv, advRt ;
		var time ;
		u = UserView(w, Rect(0, 0, hStep*spec.size, vStep*spec[0].size))
		.background_(Color.white)
		.drawFunc_{
			spec.do{|pitchArr,i|
				pitchArr.do{|p, j|
					if (p > -96){ // hard coded thresh for spec
						case
						{ selections == []}
						{ col = Color.gray(p.linlin(thresh, -10, 0.9, 0)) }
						{ this.checkSelections(i, j)[0] }
						{ col = Color.hsv(
							this.checkSelections(i,j)[1].linlin(0, selections.size, 0, 0.999)
								, 1, p.linlin(thresh, -10, 0.9, 0)) }
						{ this.checkSelections(i,j)[0] == false }
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
		}
		// keydown action for the selector view
		.keyDownAction_{ |doc, char, mod, unicode, keycode, key|
			[unicode, keycode, key].postln ;
		} ;

	}
*/
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
g = SonaGraphGui.new(a, -40, ~sample, hStep:5, vStep:6).makeGui ;

g.select(0, 40)
// now we have a spec
m = SpectraDaw.from(g.selected, -30); g.drawSelected ;
o = SpectraDawGui(m, 10).makeGui ;
o.addSelections([o.newSelectionInRange(0, 20, 21, 106), o.newSelectionInRange(21, 40, 21, 106)])

o.playSelection(o.selections[1], from:20)
o.selections = [] ;
o.addSelections([o.newSelectionInRange(0, 40, 21, 71), o.newSelectionInRange(0, 40, 71, 76), o.newSelectionInRange(0, 40, 77, 106)])
o.selections.size
o.addActualToSelections ;
o.newActualSelection(o.selections[0]) ;
o.u.refresh

o.newActualSelection ;
o.addActualToSelections ;

o.playSelection(o.selections[0])


~spec = o.specFromSelection(o.selections[1])

SD.play(m, 4, 20)
m.do{|i| i.postln}
m.size

k = SD.mix([n, m])

k = SD.from(k)


o.u.close
o.cursorView.close
o.display.close


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
