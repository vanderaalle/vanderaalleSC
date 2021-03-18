// Still another variation on SonaGraphGui
// this time specialised on events from spec
// it allows to manipulate selections
// a selection is a boolean mask over an event list


SDEvGui {

	var <>evList ;
	var <>w, <>u ; // user window for sonagraph
	var <>maxExt ;
	var <>hStep, <>vStep, <>width ;
	var <>cursor, <>cursorView, <>display ;// <>sfView;

	// selection support
	var <>selections ;
	var <>actualSelection ;

	var <>sel, <>newSel ;
	var <>rate, <>dbVol ;
	var randCols, <>maxSels ;

	var <>editType = \duration; // \rew or \fwd
	/*
	var <>spectraDaw ;
	var <>spec, <>rate ;
	var <>thresh ;

	var <>boost ;
	*/

	*new { |evList, maxSels = 20| // just for colors
		^super.new.initSDEvGui(evList, maxSels)
	}

	initSDEvGui { |anEvList, aMaxSels|
		evList = anEvList ;
		maxExt = this.getMaxExt(evList) ;
		selections = [] ; // an array of selection masks
		sel = [0, 0] ;
		dbVol = -15 ;
		actualSelection = this.newSelection ;
		maxSels = aMaxSels ;
		thisThread.randSeed = 1993;
		// random sel color on init
		randCols = Array.fill(maxSels, {|i| Color.hsv(i/maxSels, rrand(0.8,1), rrand(0.75, 1))})
		.scramble;
	}

	resetEvList {|anEvList|
		evList = anEvList ;
		maxExt = this.getMaxExt(evList) ;
		u.refresh ;
	}

	// here we have an idea of max width in bins
	getMaxExt {|evList|
		var max = 0 ;
		evList.select{|v|v != [] }
		.do{|v| v.do{|e| if((e[0]+e[1]) > max){ max = e[0]+e[1] }} };
		^max
	}


	makeGui { |width = 600, vStep = 6, hStep = 2.5, selViewH = 100, window,
		labView = 50, labStep = 5|
		var flag, player, binBar ;
		var col, strCol ;
		var adv, advRt ;
		var time ;
		var selW, selX, selY ;
		var selView ; // container for sel color legenda
		// if width is passed then it is used and the window will scroll if shorter
		var ww = if (width.isNil){hStep*maxExt}{width} ;
		w = if(window.isNil) {
			Window("Event Selecter",
				Rect(10, 100, ww, vStep*evList.size+selViewH), scroll:true)
			.background_(Color.gray)
			.front } { window } ;
		// u is the main canvas for events
		u = UserView(w, Rect(0, 0, hStep*maxExt, vStep*evList.size))
		.background_(Color.grey(0.1))
		.drawFunc_{
			evList.do{|events,i|
				events.do{|p, j|
					Pen.fillColor_(Color.grey(0.95)) ;
					Pen.strokeColor_(Color.grey(0.2)) ;
					case
					{ selections == []}
					{ col  = Color.grey(0.95);
						strCol = Color.hsv(0,1,1) ;
					}
					{ this.checkSelections(i, j)[0] }
					{ col = randCols[this.checkSelections(i,j)[1]] ;
						strCol = Color.grey(0.2) ;
					}
					{ this.checkSelections(i,j)[0] == false }
					{ col  = Color.grey(0.95) ;
						strCol = Color.hsv(0,1,1) ;
					} ;

					if (actualSelection[i][j])
					{col = Color.hsv(0,1,1, 0.5);
						strCol = Color.hsv(0,1,1)
					} ;


					Pen.fillColor_(col) ;
					Pen.strokeColor_(strCol) ;
					Pen.fillRect(Rect(
						p[0]*hStep,
						vStep*(evList.size-i)-(vStep*0.5),
						p[1]*hStep, vStep )
					);
					Pen.strokeRect(Rect(
						p[0]*hStep,
						vStep*(evList.size-i)-(vStep*0.5),
						p[1]*hStep, vStep )
					);


				} ;
				//Pen.fillColor_(Color.new255(255, 127, 0)) ;
				Pen.fillColor_(Color.green) ;
				Pen.strokeColor_(Color.red) ;
				Pen.fillOval(Rect(sel[0]*hStep,
					vStep*evList.size-(sel[1]*vStep)-(vStep*0.5),
					hStep, vStep)) ;
				Pen.strokeOval(Rect(sel[0]*hStep,
					vStep*evList.size-(sel[1]*vStep)-(vStep*0.5),
					hStep, vStep)) ;
			} ;
			// grid plotting
			maxExt.do{|i|
				if(i%16 == 0) {
					Pen.strokeColor_(Color.hsv(0.4, 0.9, 0.8,0.5)) ;
					Pen.line((hStep*i) @ 0, (hStep*i) @ u.bounds.height ) ;
					Pen.stroke
				}
			};
			evList.size.do{|i|
				if (i%12 == 0){
					Pen.strokeColor_(Color.hsv(0.4, 0.9, 0.8,0.5)) ;
					Pen.line(0 @ (vStep*(i+1)), u.bounds.width @ (vStep*(i+1)) ) ;
					Pen.stroke
				}
			} ;
			selView.refresh ;
		} ;
		// bin numbering on x axis
		binBar = UserView.new(w, Rect(0, u.bounds.height, hStep*maxExt, labView))
		.background_(Color.grey(0.9))
		.drawFunc_{
			maxExt.do{|p, i|
				if(i%labStep == 0){
					Pen.strokeColor = Color.red ;
					Pen.line( i*hStep@0, i*hStep@5) ;
					Pen.stringAtPoint(i.asString,i*hStep@11,Font("DIN Condensed", 8), Color.red)
				};
				if(i%16 == 0){
					Pen.strokeColor = Color.hsv(0.4, 0.9, 0.8,0.5) ;
					Pen.line( i*hStep@0, i*hStep@22) ;
					Pen.stringAtPoint(
						((i/16).asInteger+1).asString,
						i*hStep@25,Font("DIN Condensed", 10), Color.hsv(0.4, 0.9, 0.5,1))
				};

				Pen.stroke ;
			}

		} ;
		selView = UserView(w, Rect(0, u.bounds.height+labView, u.bounds.width, selViewH))
		.drawFunc_{
			var left, top ;
			selections.do{|i, j|
				top = [30, 60][j%2] + 5; // hard code a bit
				left  = (j/2).asInteger*50 + 15 ;
				Pen.fillColor_(
					randCols[j]
				) ;
				Pen.strokeColor_(Color.grey(0.2)) ;
				Pen.fillRect(Rect(left, top, 20, 20)) ;
				Pen.strokeRect(Rect(left, top, 20, 20)) ;
				Pen.stringAtPoint(j.asString,(left+24)@(top+2),Font("DIN Condensed", 12), Color.black)
			}
		} ;

		// cursor support
		cursor = [0,0]; flag = true ;
		cursorView =  UserView(w, Rect(0, 0, hStep*maxExt, vStep*evList.size))
		.background_(Color(1,1,1,0));
		display = StaticText(w, Rect(5, 5, 200, 15))
		.font_(Font("DIN Condensed", 12)).align_(\left) ;
		cursorView.mouseDownAction_{|view, x, y, mod|
			var pitch = ((88+21)-(y/vStep)).round ;
			var time = x.linlin(0, u.bounds.width,
				0, maxExt).asInteger ;
			sel = [time, ((88)-(y/vStep)).asInteger] ;
			Synth(\mdaPiano, [\freq, (((88)-(y/vStep)).asInteger+21).midicps]) ;
			display
			.stringColor_(Color.white)
			.string_((
				time.asString++
				"    M:"
				++pitch.asString
				++"  N:"++pitch.midinote.toUpper.replace(" ", "")
				++"  F:"++pitch.midicps.round(0.01).asString)
			);
			cursor = [x,y];
			//u.refresh ;
			cursorView.refresh ;
			selX = 0; selY = 0 ;
			selW = UserView(w, Rect(x, y, u.bounds.width-x, u.bounds.height))
			.drawFunc_{
				Pen.strokeColor_(Color.red) ;
				Pen.strokeRect(Rect(0, 0, selX-x, selY-y))
			};
			u.refresh ;
		}
		.mouseMoveAction_{|view, x, y, mod|
			selX = x; selY = y ;
			selW.refresh ;
			u.refresh ;
		}
		.mouseUpAction_{|view, x, y, mod|
			var pitch = ((88+21)-(y/vStep)).round ;
			var time = x.linlin(0, u.bounds.width,
				0, maxExt).asInteger ;
			newSel = [time, ((88)-(y/vStep)).asInteger] ;
			evList.do{|p, i|
				p.do{|e, j|
					if( (e[0] >= sel[0]) && (e[0] <= newSel[0])
						&& (i <= sel[1]) && (i >= newSel[1])
					) {
						this.whetherSelection(actualSelection, e[0], i+21) ;

					}
				}
			} ;
			u.refresh ;
			selW.close ;
		}

		.drawFunc_{
			display.bounds_(Rect(cursor[0]-23, cursorView.bounds.height-15, 200, 15));
			Pen.strokeColor = Color.red ;
			Pen.line(0 @ cursor[1], cursorView.bounds.width @ cursor[1]);
			Pen.line(cursor[0] @ 0, cursor[0] @ cursorView.bounds.height);
			Pen.stroke
		}

		.keyDownAction_{ |doc, char, mod, unicode, keycode, key|
			//[unicode, keycode, key].postln ;
			case
			// classic playback from cursors with spacebar
			{ (unicode == 32)&&(flag == true)}
			{
				var from = cursor[0].linlin(0, cursorView.bounds.width,
					0, maxExt).asInteger ;
				player = SD.playEvents(evList, rate, dbVol, from) ;
				flag = false ;
				#adv, advRt =  this.makeAdvancement(from, cursor[0], hStep, vStep) ;
			} //;
			{ (unicode == 32)&&(flag == false)}
			{
				flag = true;
				player.stop;
				advRt.stop ;
				adv.close ;
			}
			// moving selected point with WASZ up back fwd down
			// arrows trigger scrolling so useless
			//{ keycode == 65361 }
			{ unicode == 97 } {
				//"back".postln;
				sel = [sel[0]-1, sel[1]];
				u.refresh ;
				cursorView.refresh ;
			}
			// { keycode == 65363 }
			{ unicode == 115 } {
				//"forward".postln;
				sel = [sel[0]+1, sel[1]] ;
				u.refresh ;
				cursorView.refresh ;
			}
			// { keycode == 65362 }
			{ unicode == 119 }{
				//"up".postln;
				sel = [sel[0], sel[1]+1] ;
				u.refresh ;
				cursorView.refresh ;
			}
			//{ keycode == 65364 }
			{ unicode == 122 }
			{
				//"down".postln;
				sel = [sel[0], sel[1]-1];
				u.refresh ;
				cursorView.refresh ;
			}
			// with enter the actual flag is written in the actual selection
			{ unicode == 13 }
			{ var fl ;
				//"enter".postln ;
				// sel is a xy
				// check for mapped into selection
				this.whetherSelection(actualSelection, sel[0], sel[1]+21) ;
				u.refresh ;
			}
			// by pressing a number key you change the dur
			// of all selected events. Remember to save evList
			// a bit graphically buggy
			{ Array.series(9, 49).includes(unicode) }
			{
				var val = char.asString.asInteger ;
				char.postln ; sel.postln;
				evList.do{|p, i|
					p.do{|e, j|
						if( (e[0] >= sel[0]) && (e[0] <= newSel[0])
							&& (i <= sel[1]) && (i >= newSel[1])
						) {
							case
							{ editType == \duration }
							{ evList[i][j][1] = val }
							{ editType == \rew }
							{ evList[i][j][0] = evList[i][j][0] - val }
							{ editType == \fwd }
							{ evList[i][j][0] = evList[i][j][0] + val }
						}
					}
				} ;
				this.resetEvList(evList) ;
			}
		}
	}

	modifyEvent {|top, bottom, val, type = \duration|
		if ((top.isNil).and(bottom.isNil)){
			top = [sel[0], sel[1]+21] ;
			bottom = [newSel[0], sel[1]+21] ;
		};
		[top, bottom].postln;
		evList.do{|p, i|
			p.do{|e, j|
				if( (e[0] >= top[0]) && (e[0] <= bottom[0])
					&& (i <= (top[1]-21)) && (i >= (bottom[1]-21))
				) {
					case
					{ type == \duration }
					{ evList[i][j][1] = val }
					{ type == \rew }
					{ evList[i][j][0] = evList[i][j][0] - val }
					{ type == \fwd }
					{ evList[i][j][0] = evList[i][j][0] + val }
				}
			}
		} ;
		this.resetEvList(evList) ;
	}

	// things are getting messy...
	whetherSelection {|selection, bin, pitch|
		var fl = this.checkIntoSelection(selection,
			bin, pitch) ;
		if(fl.notNil){
			// if it's true then false and vice versa
			if (fl == false) {
				this.addToSelection(selection,
					bin, pitch)
			}{
				this.removeFromSelection(selection,
					bin, pitch)
			} ;
		}
	}


	// advancement bar
	// issue: once playing, then stop to get mouse
	// or you loose it
	makeAdvancement { |from, cursorPos, hStep, vStep|
		var cp = from ;
		var u = UserView(w, Rect(0, 0, hStep*maxExt, vStep*evList.size))
		.drawFunc_{
			Pen.strokeColor = Color.cyan(0.7) ;
			Pen.line((cp*hStep) @ 0,
				(cp*hStep) @ cursorView.bounds.height);
			Pen.stroke
		} ;
		var r = {
			(maxExt-from).do{
				cp = (cp + 1) ;
				{u.refresh}.defer ;
				rate.reciprocal.wait ;
			};
			{u.close}.defer ;
		}.fork ;
		^[u, r]
	}


	// SELECTION SUPPORT
	// clean design would move it to a model-like section, not view/cnt
	// a selection is a mask of boolean flags over spec
	// new one is filled with false
	newSelection  {
		var e =
		evList.collect{|i|
			if ( i!=[] ) {
				i.collect{|j| false}
			}{i}
		} ;
		if(u.notNil){u.refresh} ;
		^e
	}

	checkSelections {|pitch, ev|
		var ok = [] ;
		selections.do{|sel, k|
			if (sel[pitch][ev]){ok = ok.add([true, k])}
		};
		if (ok.size != 0 ){^ok[0]}{^[false]}
	}

	// return the flag for an event, via bin
	checkIntoSelection {|selection, bin, pitch|
		var index, p ;
		p = pitch-21 ;
		// this does not prevent
		evList[p].do{|i,j|
			if ((i[0] <= bin) && ((i[0]+i[1]) > bin)) {
				index = j
			} ;
		} ;
		if (index.notNil) {
			^selection[p][index]
		}{^nil}
	}


	// adding a selection
	addSelection {|selection|
		selections = selections.add(selection) ;
		u.refresh ;
	}


	// an array of selections
	addSelections {|selectionArray|
		selectionArray.do{|selection| this.addSelection(selection)}
	}

	removeSelection {|selection|
		selections.removeAt(selections.indexOf(selection)) ;
		u.refresh ;
	}

	removeSelections {|selectionArray|
		selectionArray.do{|selection| this.removeSelection(selection) } ;
	}

	// adding an item to selection
	addToSelection {|selection, bin, pitch|
		var index, p ;
		p = pitch-21 ;
		evList[p].do{|i,j|
			if ((i[0] <= bin) && ((i[0]+i[1]) > bin)) {
				index = j
			} ;
		} ;
		selection[p][index] = true ;
		u.refresh ;
	}

	// removing an item from selection
	removeFromSelection { |selection, bin, pitch|
		var index, p ;
		p = pitch -21 ;
		evList[p].do{|i,j|
			if ((i[0] <= bin) && ((i[0]+i[1]) > bin)) {
				index = j
			} ;
		} ;
		selection[p][index] = false ;
		u.refresh ;
	}


	// the new actual selection can be an existing selection
	// so you will not add it when done
	// or a new one, then you need to add it to selections
	newActualSelection{|selection|
		if(selection.isNil){
			actualSelection = this.newSelection
		}{
			actualSelection = selection
		};
	}

	// actual selection is added to selections
	addActualToSelections {
		selections = selections.add(actualSelection) ;
		actualSelection = this.newSelection ;
		u.refresh ;
	}

	// selecting in range
	// low and high are midi values
	newSelectionInRange {|from, to, low, high|
		var l, h, newSel  = this.newSelection ;
		l = low-21 ; h = high -21 ;
		evList.do{|pitch, i|
			if ( (i >= l )&&(i <= h) ){
				if( pitch !=[] ){
					"something".postln ;
					pitch.do{|e, j|
						e.postln ;
						if( (e[0] >= from) && ( (e[0]+e[1]) < to ))
						{ "TRUE!".postln; newSel[i][j] = true}
					}
				}
			}
		} ;
		^newSel
	}

	newSelectionFromMerge {|selections|
		var newSel  = this.newSelection ;
		selections.do{|sel|
			sel.do{|pitch, i|
				if (pitch != []) {
					pitch.do{|e, j|
						if(e) {
							newSel[i][j] = true ;
						}
					}
				}
			}
		}
		^newSel
	}

	mergeSelections {|selections|
		var newSel = this.newSelectionFromMerge(selections) ;
		this.removeSelections(selections) ;
		this.addSelection(newSel) ;
		actualSelection = this.newSelection ;
	}

	// similar but very different from before
	addActualToSelection {|selection|
		actualSelection.do{|p, i|
			p.do{|e, j|
				if(e){
					selection[i][j] = true
				}
			}
		} ;
		actualSelection = this.newSelection ;
		u.refresh ;
	}

	// this remove actual to a certain selection
	removeActualFromSelection {|selection|
		actualSelection.do{|p, i|
			p.do{|e, j|
				if(e){
					selection[i][j] = false
				}
			}
		} ;
		actualSelection = this.newSelection ;
		u.refresh ;
	}

	playSelection {|selection, from|
		var cnt ;
		if (from.isNil) {
			from = cursor[0].linlin(0, cursorView.bounds.width,
				0, maxExt).asInteger } ;
		cnt = from ;
		^{
			(this.getMaxExt(evList)-from).do{
				evList.do{|i, j|
					if(i != []){
						i.do{|ev, n|
							if((ev[0] == cnt)
								&& (selection[j][n])){
								Synth(\mdaPiano,
									[\freq, (j+21).midicps,
										\mul, dbVol.dbamp]
								)
							}
						}
					}
				} ;
				cnt = cnt +1 ;
				rate.reciprocal.wait ;
			}
		}.fork
	}

	cleanSelections {
		this.selections = [] ; this.u.refresh
	}

	// archiving
	writeSelection {|selection, path|
		selection.writeArchive(path) ;
	}

	readSelection {|path|
		^Object.readArchive(path)
	}

	newEvListFromSelection {|selection|
		^evList.collect{|p, i|
			if (p == []){
				p
			}{
				p.select{|e, j|
					selection[i][j] ;
				}
			}

		}
	}
}

