/*
THE BABAKOTO PROJECT: BabaGui
-a- started: ~20/06/08


BabaPatcherGui is the class which represents the main GUI stuff.
It allows to control 90% of BabaPatcher from GUI.

Typically you acceed to its functionalities by  calling BabaPatcher.gui

Last updated: 030708

// andrea valle
// http://www.cirma.unito.it/andrea/
// andrea.valle@unito.it

*/


BabaGui {

	var <>patcher ;
	var <>synthList ;
		
	var <>synthUiDict ;
	var <>window, flow ;
	var <>widthInModules, <>height, <>vStep ;
// for placing algo
	var <>lines, <>linesPerCol ; // the total lines (by counting nextline) 
	var col, totalLines ;
	var <>startConnection ;

	
	*new { arg patcher, synthList, widthInModules = 3, height = 1000, vStep = 15, title, color ;
		^super.new.initBabaGui(patcher, synthList, widthInModules, height, vStep, title, color) 
	}

	initBabaGui { arg aPatcher, aSynthList, aWidthInModules, aHeight, aVStep, aTitle, aColor ;
		widthInModules = aWidthInModules ;
		height = aHeight ;
	 	vStep = aVStep ;
	 	linesPerCol = height/vStep ;
		patcher = aPatcher ;
		aTitle = aTitle ? patcher.name.asString ;
		aColor = aColor ? Color(0, 0, 0.3) ;
		synthList = if ( aSynthList.notNil) { aSynthList } 
			{ patcher.synthDict.keys } ;
		window = GUI.window.new(aTitle, 
			Rect(30,30, (400+vStep)*widthInModules, height)
			//, scroll:true
			) ;
		flow = FlowLayout.new( window.view.bounds );
	    	window.view.decorator = flow;
	     window.view.background_( aColor) ;
	     patcher.addDependant(this) ;
	     synthUiDict = IdentityDictionary.new ;
	}

	makeAllGui {
		var indDict = IdentityDictionary.new ;
		// we create a dict to maintain order of synths, indDict
		synthList.do ({ |name| 
			indDict[patcher.synthDict[name][1]] = name ;
			synthUiDict[name] = [] ;
		}) ;
		lines = 0 ; col = 0 ; totalLines = 0 ;
		indDict.keys.asArray.sort.do({ |ind| 
			synthUiDict[indDict[ind]] = this.makeGui(indDict[ind]) ;
			
		}) ;
		/*
		//
		window.onClose_({ synth.free }) ;
		*/
		window.front ;
	}
	
	
	// intended to be private
	makeGui { arg synthName ;
	// creates a GUI where synthDef's args 
	// can be controlled thru knobs
	// min|max define each slider's range
	// from code of 14/04/07
		var babaSynth = patcher.synthDict[synthName][0] ;
		var controlDict = babaSynth.controlDict, argDict = IdentityDictionary.new ;
		var controlList = babaSynth.controlList ;
		var st ;
		var index ;
		var rect ;
		var knob ;
		// estimation of total height in lines
		// a little narrower than needed: ?
		if ( (lines + controlDict.keys.size+6) > linesPerCol) 
			{ 

			totalLines = totalLines+lines + controlDict.keys.size+6 ;
			col = (totalLines/linesPerCol).asInteger ; 
			rect = Rect((300+vStep)*col, 0, 300+vStep, height) ;
			flow = FlowLayout(rect) ;
			window.view.decorator = flow ;
			lines = 0 ;
			} ;
		flow.nextLine ; lines = lines + 1 ;
		GUI.staticText.new(window, Rect(0,0, 0, vStep)) ; // blank line
		flow.nextLine ; lines = lines + 1 ;

		// labels
		GUI.staticText.new( window, Rect( 60, 0, 10, vStep ))
							.string_("in" ).align_( \left)
							.font_(GUI.font.new("Helvetica", vStep*0.75))
							.stringColor_(Color.white) ;

		GUI.staticText.new( window, Rect( 60, 0, 70, vStep ))
							.string_(babaSynth.name+" " ).align_( \right)
							.font_(GUI.font.new("Futura", vStep))
							.stringColor_(Color.yellow)
							 ;		

		argDict[\button] = 
		GUI.button.new(window, Rect( 20, 0, 40, vStep ))
			.states_([
				["stop", Color.white, Color.red],
				["start", Color.hsv(0.1,1,0.85), Color.white]
			])
			.action_({ arg butt ;
				if (butt.value == 0) {babaSynth.run(true)} {babaSynth.run(false)}
			} )	;
		GUI.staticText.new( window, Rect( 300, 0, 85, vStep ))
						.string_( "audio out -->" ).align_( \right) 						.font_(GUI.font.new("Helvetica", vStep*0.75))
						.stringColor_(Color.white) ;
		argDict[\output] = 
		GUI.textField.new(window, Rect( 860, 0, 30, vStep ))
			.font_(GUI.font.new("Helvetica", vStep*0.75))
			.boxColor_(this.colorFromName(babaSynth.name)) 
			.action_({ arg txt ;
				var val = txt.string ;
				var busIndex ;
				if ( val != "" ) { busIndex = val.asInteger } ;
			patcher.out(synthName, busIndex) ;
			}) ;
			
		argDict[\toButt] = 
		GUI.button.new(window, Rect( 20, 0, vStep, vStep ))
			.states_([
				["to", Color.white, this.colorFromName(babaSynth.name)],
				["to", Color.red, Color.black]
			])
			.action_({ arg butt ;
				if ( butt.value == 1)
				 { startConnection =  babaSynth.name } { startConnection = nil } ;
			} )	;
		
		flow.nextLine ; lines = lines + 1 ;						
		// GUI creation
		index = 1 ;
		controlList.do({ arg argName ;
				var item = controlDict[argName] ;
				var guiElement = [
				
				GUI.button.new(window, Rect( 20, 0, vStep, vStep ))
					.states_([
						["in", Color.black, Color.white],
					])	
				.action_({ arg butt ;
					if ( startConnection.notNil )
						{ patcher.in(synthName, argName, startConnection) } ;
					startConnection = nil ;
				}),
				
				GUI.textField.new( window, Rect( 20, 30*index, 40, vStep ))
					.string_("")
					.font_(GUI.font.new("Helvetica", vStep*0.75))
					.action_({ arg mapBox ;
						 patcher.in(synthName, argName, mapBox.value.asSymbol)
						}),
				GUI.staticText.new( window, Rect( 40, 30*index, 40, vStep ))
					.string_( argName ).align_( \right)					.font_(GUI.font.new("Helvetica", vStep*0.75))
					.stringColor_(Color.white),   
				GUI.numberBox.new( window, Rect( 100, 30*index, 40, vStep ))
					.value_(item[1])									.font_(GUI.font.new("Helvetica", vStep*0.75))
					.action_({ arg minBox ;
						patcher.setMin(synthName, argName, minBox.value) 
					}),
				GUI.numberBox.new( window, Rect( 240, 30*index, 40, vStep ))
					.font_(GUI.font.new("Helvetica", vStep*0.75)) 
					.value_(item[2])									.action_({ arg maxBox ;
						patcher.setMax(synthName, argName, maxBox.value) 
					}),
				// requires Knob quark, that setups GUI interface
				knob = JKnob.new( window, Rect( 310, 30*index, vStep, vStep ))
					.value_(
						item[0].linlin(item[1], item[2], 0, 1)
					)
					.action_({ arg sl ;
						var val = sl.value.linlin(0, 1, 							patcher.getMin(synthName, argName), 
							patcher.getMax(synthName, argName)
						) ;
						patcher.set(synthName, argName, val) 
					}),
				GUI.textField.new( window, Rect( 520, 30*index, 70, vStep ))
						.string_( item[0].trunc(0.001).asString )
						.align_( \right)
						.font_(GUI.font.new("Helvetica", vStep*0.75))
						.action_({ arg valBox ;
							patcher.set(synthName, argName, valBox.value.asFloat) 
						}) 
				] ;
				knob.color[0] = Color.red ;
				argDict[argName] = guiElement ;	
				flow.nextLine ; lines = lines + 1 ;	
				index = index + 1 ;
		}) ;				
		^argDict ;
	}
	
	drawCables {
		var from, argName ;
		var connList ;
		var outBox, inBox ;
		var inPoint, outPoint ;
		var color ;
		window.drawHook = {
			GUI.pen.width = 2;
			patcher.connectionDict.keys.do({
				arg synthName ;
				connList = patcher.connectionDict[synthName].clump(2) ;
				connList.do({ arg item ;
					from = item[1] ;
					argName = item[0] ;
					color = this.colorFromName(from)  ;
					// for cables
					if (synthList.includes(synthName) )
					{
						inBox = synthUiDict[synthName][argName][0] ;
						synthUiDict[synthName][argName][0].states_(
							[["in", Color.white, color]] ) ;
						synthUiDict[synthName][argName][1].boxColor_(color) ;
					if ( synthList.includes(from) )
						{	
						outBox = synthUiDict[from][\toButt] ;
						outBox.value_(0);
						outPoint = outBox.bounds.rightBottom.translate(0@(vStep.neg*0.5));
						inPoint =  inBox.bounds.origin.translate(0@(vStep*0.5)) ;
						GUI.pen.strokeColor = color ;
						GUI.pen.fillColor = color ;
						GUI.pen.line(outPoint, inPoint) ; 
						GUI.pen.fillOval(Rect(inPoint.x-5, inPoint.y-5, 10, 10)) ; 
						GUI.pen.stroke ;
						}
					}		
				}) ;
			}) ;
		}
	
	}
	
	// calculates a color from a name, 
	// by dividing the hue circle in n parts, where n is num of synths
	colorFromName { arg name ;
		var index, tot, hue ;
		index = patcher.synthDict[name.asSymbol][1] ;
		tot = patcher.createIndexList ;
		hue = tot.indexOf(index)/tot.size ;
		^Color.hsv(hue, 0.8, 0.8)	
	}

	update { arg theChanged, theChanger, more ;
		var argDict, guiArr ;
		var val, min, max ;
		argDict = synthUiDict[more[1]] ;
		if (argDict.notNil)
		{
		case 
		{ more[0] == \order }
			{ 
			window.view.children.copy.do({|i| i.remove }) ;
			window.view.refresh ;
			flow = FlowLayout.new( window.view.bounds );
		    	window.view.decorator = flow;
			this.makeAllGui ;	
			this.drawCables ;
			}
		// doesn't work as expected
		{ more[0] == \add }
			{ 
			window.view.children.copy.do({|i| i.remove }) ;
			window.view.refresh ;
			flow.reset ;
			this.makeAllGui ;	
			}
		{ more[0] == \set }
			{ 
			guiArr = argDict[more[2]] ;
			guiArr[0].states_([["in", Color.black, Color.white]]) ;
			guiArr[1].string_("").boxColor_(Color.white) ;
			guiArr[6].value_(more[3].trunc(0.0001)) ;
			guiArr[5].value = more[3].linlin
			(guiArr[3].value, guiArr[4].value, 0, 1) ;
			this.drawCables ;
			 }
		{ more[0] == \setMax }
			{ 
			guiArr = argDict[more[2]] ;
			guiArr[4].value_(more[3]) ;
			min = patcher.getMin(more[1], more[2]) ;
			max = patcher.getMax(more[1], more[2]) ;
			val = patcher.get(more[1], more[2]) ;
			guiArr[5].value_(val.linlin(min, max, 0, 1)) ;
			 }
		{ more[0] == \setMin }
			{ 
			guiArr = argDict[more[2]] ;
			guiArr[3].value_(more[3]) ;
			min = patcher.getMin(more[1], more[2]) ;
			max = patcher.getMax(more[1], more[2]) ;
			val = patcher.get(more[1], more[2]) ;
			guiArr[5].value_(val.linlin(min, max, 0, 1)) ;
			 }

		{ more[0] == \out }
			{
			if ( more[2].isNil ) 
				{ argDict[\output].value = "" } 
				{ argDict[\output].value = more[2] } ;
			this.drawCables ;
			}
		{ more[0] == \in }
			{ 
			guiArr = argDict[more[2]] ;
			guiArr[1].value_(more[3]) ;
			this.drawCables ;
			}
		{ more[0] == \run }
			{
			if ( more[2] )
				{ argDict[\button].value = 0}
				{ argDict[\button].value = 1} 
			}		
		}
	 }
		
}


/*


s.reboot ;


(
k = SynthDef("frq", { arg out; Out.ar(out, SinOsc.ar(4, mul: 400, add: 500))}).send(s) ;

d = SynthDef("tst", 
	{ arg out , freq = 440, width = 0.5, mul = 1, add = 0 ;
	Out.ar(out, Pulse.ar(In.ar(freq), In.ar(width), In.ar(mul), In.ar(add)))}
).send(s) ;

h = SynthDef("clean", 
	{ arg out , freq , width, mul , add ;
	Out.ar(out, Pulse.ar(In.ar(freq), In.ar(width), In.ar(mul), In.ar(add)))}
).send(s) ;


f = SynthDef("flt", 
	{ arg out , in = 0, freq = 440, mul = 1, add = 0 ;
	Out.ar(out, LPF.ar(In.ar(in), In.ar(freq), In.ar(mul), In.ar(add)))}
).send(s) ;

e = SynthDef("sin", 
	{ arg out , in = 0, freq = 440, mul = 1, add = 0 ;
	Out.ar(out, SinOsc.ar(In.ar(freq), 0, In.ar(mul), In.ar(add)))}
).send(s) ;
)
	
(
b = BabaPatcher.new ;
b.add(k, \frq) ;
b.add(d, \tst) ;
b.add(f, \flt) ;
b.add(h, \clean) ;
b.setList(\clean, [\freq, 440, \mul, 0.5, \width, 0.2]) ;
b.add(e, \LFPulse_0) ;

g = BabaPatcherGui(b, height: 200) ;
//g.makeGui(b.synthDict[\tst][0])
g.makeAllGui ;
)

b.set(\tst, \freq, 430)
b.set(\tst, \mul, 0.7)
b.set(\tst, \add, 0)
b.set(\tst, \width, 0.5)
b.out(\tst, 1)

b.out(\tst)
b.set(\flt, \freq, 2000)
b.set(\flt, \mul, 1.4)
b.set(\flt, \add, 0)
// at the end
b.in(\flt, \in, \tst)
b.set(\flt, \in, 0)

g.drawCables

b.out(\flt, 1)
b.out(\flt)

b.remove(\tst)

b.moveBefore(\tst, \flt)

b.moveToTail(\flt)


b.set(\LFPulse_0, \freq, 10)
b.set(\LFPulse_0, \mul, 400)
b.set(\LFPulse_0, \add, 500)
b.out(\LFPulse_0, 0)
b.moveToTail(\tst)
b.in(\tst, \freq, \LFPulse_0)

*/