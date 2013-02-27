
// The Dark Store Project: inspired by Perec's Boutique obscure
// the Dark Store:
// 	- retrieves data from audio (thru Analyzer)
// 	- is inited with a text
// 	- scans the text like a sequencer
//	- forward data and mapp letters into a 8 matrix (see Cifre)

DarkStore {

	var <>analyzer;
	var <>text, <>tArr, <>current, <>active ;
	var <>loudness, <>centroid, <>flatness ; 
	var <>task, <>dur, <>letterRank ;
	var <>minEv, <>maxEv ;
	var <>doc ;

	*new { arg analyzer ;
		^super.new.initDarkStore(analyzer)
	}

	initDarkStore{ arg anAnalyzer ;
		analyzer = anAnalyzer ;
		analyzer.addDependant(this) ;
		//current = 0 ;
		active = true ; // this is a flag for not/increasing sentence
		//this.setupTask ;
		this.setupTask ;
		centroid = 60 ;
		dur = 1 ;
		// note that minEv and maxEv are number of events per sec (it's a rate)
		minEv = 1 ;
		maxEv = 1 ;
		// following the cifre mapping
		letterRank = (
			\a:0, \b:0, \k:0,
			\c:1, \f:1, \l:1,
			\d:2, \m:2, \t:2, \y:3,
			\e:3, \q:3, \z:3, \x:3,  
			\g:4, \r:4, \s:4, 
			\h:5, \i:5, \j:5,
			\n:6, \p:6, \u:6,
			\o:7, \v:7, \w:7) ;

		}



	
	pushText { arg path ;
		var file =  File(path, "r") ;
		this.setupText(file.readAllString)  ;
		file.close ;
	}

	// split into sentences, we iterate over the array
	setupText { arg aText ; 
		text = aText ;
		tArr = text.split($.).select{|i|i.size>0} ; // protecting againt a void element ("..")
		if (tArr == []) {tArr = [text]} ;
		current = 0 ;
		//this.setupTask ;
	}
	
	// force an event. THIS IS PRETTY DIRTY
//	bangEvent { arg key, keycode ;
//		var letter ;
//		if ( keycode == 13 )
//					{ this.changed(this, [\restart]) }
//				//{ index.includes(keycode) }
//					{ letter = key ;
//					this.changed(this, 
//					[\event, 
//						letter.asSymbol, letterRank[letter.toLower.asSymbol], 
//						loudness, centroid, flatness
//					]) }
//	}
	
	runAnalyzer { arg flag = true ;
		analyzer.onsets.run(flag).set(\thresh, 0.4) ;	
		analyzer.flatness.run(flag) ;
		analyzer.loudness.run(flag) ;
		analyzer.centroid.run(flag) ;
	}
	
//	setupDocument { arg factor = 1.0, transp = 0,
//			space = true, crlf = true,
//			map, 
//			title = "Scriptorium", 
//			bounds = Rect(1280-640, 800-480, 640, 480),
//			background = Color(0,0,0.4),
//			stringColor = Color.white, 
//			font = SCFont.new("Futura", 30) ;
//		var letter ;
//		var index = Array.series(91-65, 65).addAll(Array.series(91-65, 97)) ;
//		// protecting against auto syntax colorize
//		Document.globalKeyDownAction_({}) ;
//		doc = Document.new.title_(title)
//			.bounds_(bounds)
//			.background_(background)
//			.stringColor_(stringColor) 
//			.font_(font)
//			.keyDownAction_({ arg doc, key, modifiers, keycode;
////				keycode.postln ;
//				if ( keycode == 13 )
//					{ this.changed(this, [\restart]) }
//				//{ index.includes(keycode) }
//					{ letter = key ;
//					this.changed(this, 
//					[\event, 
//						letter.asSymbol, letterRank[letter.toLower.asSymbol], 
//						loudness, centroid, flatness
//					]) }
// 			})
//		
//		}
	
	// setting up the sequences	
	setupTask{
		// avoiding the changing the element over which we iterate
		task = Task({
			var cur = current ;
			inf.do{
			tArr[cur].do{|letter|
				[letter.asSymbol, letterRank[letter.toLower.asSymbol], 
						loudness, centroid, flatness].postln ;
				// we just forward the stuffa all around
				this.changed(this, 
					[\event, 
						letter.asSymbol, letterRank[letter.toLower.asSymbol], 
						loudness, centroid, flatness
					]) ;
				dur.postln.wait ;
				cur = current ;
				} ;
				this.changed(this, [\restart]) ;
			}
		}) ;
		}
	
	increaseCurrent {
		if (current.notNil)
			{ current = (current+1) % tArr.size };
		current.postln
		}
	
	update { arg theChanged, theChanger, more ;
		// we receive stuff and just round it up a little
		case 
			{ more[0] == \loudness }
				{ loudness = more[1].round(0.1)}
			{ more[0] == \centroid }
				{ centroid = more[1].cpsmidi.round }
			{ more[0] == \flatness }
				{ flatness = more[1].round(0.01)}
			{ more[0] == \onset }
				// event? so lets have a new sentence 
				{ 
					if (active){
						this.increaseCurrent ; 	
					} ;
					// in ev per sec
					dur = centroid.linlin(60, 120, minEv.reciprocal, maxEv.reciprocal)
				}
	}

}


DarkTypo {
	
	var <>darkStore ;
	var <>doc, <>width, <>height ;
	var <>title, <>stringColor, <>background, <>font ;
	var <>string, <>func, <>x, <>y, <>inter, <>dim ;
	var <>ftSize, <>ftCol, <>levelCol ;
	var <>letterIndex ;
	
	*new { arg darkStore, width = 800, height = 600, title = "The Dark Store" ;
			^super.new.initDarkTypo(darkStore, width, height, title)
	}

	initDarkTypo{ arg store, w, h, title ;
		darkStore  = store ;
		darkStore.addDependant(this) ;
		width = w ; height = h ;
		doc = Window(title, Rect(50, 50, width, height)).front ;
		doc.view.background_(Color.black) ;
		x = 0 ; y = 0 ;
		inter = 20 ;
		dim = 20 ;
		letterIndex = 0 ;
		string = " " ;
		func = {	
			var x = 0 ; var y = height*0.5-dim ; 
			var last = string.size-1 ;
		     Pen.font = Font( "Didot", ftSize );
        		//Pen.strokeColor = Color.red;
        		string[..(last-1)].do{|l|
	        		Pen.fillColor = Color.grey(ftCol) ;
	        		x = x + dim ;
	        		if (x > width) { x = 0; y = y + inter } ;
	        		Pen.stringAtPoint( l.asString, x @ y ) ;
	        		if(letterIndex.notNil){
		        		doc.view.background_(Color.grey(levelCol))
	     	   		}
	        	};
	        	// last char is orange cursor
			Pen.fillColor = Color(1, 0.569, 0.196) ;
			Pen.font = Font( "Didot", ftSize*1.5 );
	        	x = x + (dim*1.5)	 ;
	        	if (x > width) { x = 0; y = y + inter } ;
	        	Pen.stringAtPoint( string[last].asString, x @ y );
	        	};
	}

	
	update { arg theChanged, theChanger, more ;
		case 
			{ more[0] == \event }
				{ 
					
					{
				string = string + more[1] ;
				letterIndex = more[2] ; 
				ftSize = more[3].linlin(5, 50, 5, 50) ; // loudness
				levelCol = more[3].linlin(5, 50,0,1) ;
				dim = more[4].linlin(50, 150, 5, 30) ; // centroid
				ftCol = more[5].linlin(0, 1, 1, 0) ;	// flatness
				doc.drawFunc = func ;
				doc.refresh ;
					}.defer
				}
			{ more[0] == \restart }
				{ {string = ""}.defer }
	}



}



// monodirectional for the darkstore and an adapter
DarkStoreGUI{
	
	var <>width, height, <>title, <>window ; 
	var <>pref ; // store init prefsm quick hack
	var <>darkStore, <>txtDict, <>txtArr ;
	var <>adapter ;
	var <>guiStuff ; // arr containing configurable elements
	
	*new { arg darkStore, adapter, width = 490+410, height = 530+40, title = "DarkStore", txtArr, pref ;
			^super.new.initDarkGUI(darkStore, adapter, width, height, title, txtArr, pref)
	}

	initDarkGUI{ arg store, ada, w, h, t, arr, pr ;
		darkStore = store ;
		adapter = ada ;
		txtArr = arr ;
		if (pr.isNil)
			{ pref = Array.fill(8, {[\speaker, 1, 0.05, -30, -80, 100,1]}) }
			{ pref = pr } ;
		txtDict = Dictionary.new ;
		width = w ; height = h ; title = t ;
		guiStuff = [] ;
		txtArr.do
			{|p| this.pushText(p.basename, p)};
		this.setupGUI(width, height, title) ;
	}
	
	
	pushText { arg key, path ;
		var file =  File(path, "r") ;
		[key, path].postln ;
		txtDict[key.asSymbol] =  file.readAllString  ;
		file.close ;
		txtDict.postln ;
	}
	
	setupGUI { arg w, h, t ; 
		var he = 40*3/5*6 ;
		var offset = 200 ;
		var more = 100 ;
		var vOff = 60 ;
		var letter ;
		var pushText ;
		window = Window.new(title, Rect(10, 10, w, h)).front ;
		
		TextView(window, Rect(480, 30, 400, h-20-120))
			.keyDownAction_({ arg doc, key, modifiers, keycode ;
				darkStore.bangEvent(key, keycode)
 			}) ;
 		StaticText(window, Rect(480, 10, 400,20)) 
 			.string_("--> each letter bangs an event, return clears") ;

		pushText = TextView(window, Rect(480, 500, 400, 60)).action_{|me|
			if (me.string != "") {darkStore.setupText(me.string)};
			} ;
 		StaticText(window, Rect(480, 480, 400, 20)) 
			.string_("--> text to be sequenced here!") ;
		StaticText(window, Rect(10,10, 200,20)).string_("Event/sec:") ;

		StaticText(window, Rect(10,30, 200,20)).string_("min:") ;
		NumberBox(window, Rect(40, 30, 50, 20)).value_(1) 
				.action_{|me| darkStore.minEv = me.value} ;
		StaticText(window, Rect(110, 30, 200,20)).string_("max:") ;
		NumberBox(window, Rect(140, 30, 50, 20)).value_(3) 
			.action_{|me|  darkStore.maxEv = me.value} ;

		8.do{|i|
			StaticText(window, Rect(10,i*vOff+10+50,20,20)).string_(i+1+":") ;
			guiStuff = guiStuff.add(

			PopUpMenu(window,Rect(25,i*vOff+10+50,180,20))
			.items_(["speaker","motor","solenoid"])
			.value_([\speaker,\motor,\solenoid].indexOf(pref[i][0]))
			.action_({|me|  adapter.setInterface(i+1, me.item.asSymbol)})
				)
			} ;
		guiStuff = guiStuff.clump(1) ;
		StaticText(window, Rect(220,20,40,20)).string_("dur") ; 
		StaticText(window, Rect(220+more,20,40,20)).string_("amp") ; 
		StaticText(window, Rect(220+more+more,20,40,20)).string_("freq") ; 

		8.do{|i|
			guiStuff[i] = guiStuff[i].addAll([
			NumberBox(window, Rect(220,i*vOff+15+50,40,20))
				.action_{|me| adapter.minDur[i] = me.value}
				.valueAction_(pref[i][1]),
			NumberBox(window, Rect(220,i*vOff-5+50,40,20))
				.action_{|me| adapter.maxDur[i] = me.value} 
				.valueAction_(pref[i][2]) ,
			
			NumberBox(window, Rect(220+more,i*vOff+15+50,40,20))
				.action_{|me| adapter.minAmp[i] = me.value}
				.valueAction_(pref[i][3]) , 
			NumberBox(window, Rect(220+more,i*vOff-5+50,40,20))
				.action_{|me| adapter.maxAmp[i] =  me.value}
				.valueAction_(pref[i][4]) , 
					
			NumberBox(window, Rect(more+220+more,i*vOff+15+50,40,20))
				.action_{|me| adapter.minFreq[i] = me.value }
				.value_(pref[i][5]) , 
			NumberBox(window, Rect(more+220+more,i*vOff-5+50,40,20))
				.action_{|me| adapter.maxFreq[i]  = me.value }
				.valueAction_(pref[i][6]) 
			]) ;
			StaticText(window, Rect(55+220,i*vOff+15+50,40,20)).string_("min") ;
			StaticText(window, Rect(55+220,i*vOff-5+50,40,20)).string_("max") ;

			StaticText(window, Rect(155+220,i*vOff+15+50,40,20)).string_("min") ;
			StaticText(window, Rect(155+220,i*vOff-5+50,40,20)).string_("max") ;
			} ;
		Button(window, Rect(10, 520, 100, 20))
			.states_([["Task is  ON", Color.black, Color.red], 
					["Task is OFF", Color.red, Color.black]])
					.action_{|me| if (me.value == 0) 
						{darkStore.task.play} {darkStore.task.pause}}.valueAction_(1) ;
		Button(window, Rect(120, 520, 100, 20))					.states_([["Increase is ON", Color.black, Color.red], 
					["Increase is OFF", Color.red, Color.black]])
					.action_{|me| if (me.value == 0) 
						{darkStore.active = true} {darkStore.active = false}} ;
						
		Button(window, Rect(230, 520, 100, 20))					.states_([["Analyzer is ON", Color.black, Color.red], 
					["Analyzer is OFF", Color.red, Color.black]])
					.action_{|me| if (me.value == 0) 
						{darkStore.runAnalyzer} {darkStore.runAnalyzer(false)}} ;
		
		PopUpMenu(window, Rect(350, 520, 100, 20))
			.items_(txtDict.keys.asArray)
			.action_({|me| darkStore.setupText(txtDict[me.item.asSymbol]) })			
	}
	
	setPref { arg pref ;
		pref.do{|item, index|
			guiStuff[index].do{|it, ind|
				if (pref[index][ind].class == Symbol)
					{ it.valueAction_([\speaker, \motor, \solenoid].indexOf(pref[index][ind])) }
					{ it.valueAction_(pref[index][ind]) }
				}
			}
		}	
}



/*

s.reboot ;

u = Bus.audio(Server.local) ; 
//
x = { Out.ar(u, SoundIn.ar(0))}.play ;
x = { Out.ar(u, SinOsc.ar(MouseY.kr(50, 5000,1))*MouseX.kr(0,1))}.play ;

a = Analyzer(u) ;
{Out.ar(0, In.ar(u))}.play(addAction:\addToTail)

(
a.onsets.run(true).autogui.set(\thresh, 0.4) ;
a.flatness.run(true) ;
a.loudness.run(true) ;
a.centroid.run(true) ;

~perec = "The measure-height (whose name escapes me: metronome, auction), where you must stand still for hours ad libitum. As if it were obvious. The cabinet (the two caches). The theatrical performance. The humiliation. ?. The arbitrary.
It is a room with many people. In one corner there is a height rod. I know thakt the risk of having to spend many hours in, rather than a real punishment is an injustice, but extremely embarrassing, because nothing holds the rod and the height rod in the long run, it is likely to shrink.
As if it were obvious, and I know I'm dreaming dreams, as if it were obvious, being in a concentration camp. This is not really a field, of course, is the image of a field, the dream of a field, a field-metaphor, a field which I know is just a familiar image, as if the tireless rifacessi same dream, as if I did not ever dream of another, as if he never did more than dream this field.
17
It is clear that the threat of the first height rod enough to concentrate in itself all the terror of the field. Then, it seems that it is not so terrible. On the other hand, I escape this threat is not realized. But it is precisely this threat is avoided that the strongest evidence of the field: what saves me is only the indifference of the torturer, his freedom to do or not do, they are completely submissive to his will (just as they are subjected to this dream I know it is only a dream, but I can not escape from this dream).
The second sequence takes up these issues as soon modified. Two characters (one of which, no doubt, is myself) opened a closet in which they were divided into two hiding places where they amassed the wealth of the deportees. For the wealth should understand each capable of increasing the safety and the survivability of the owner, whether of objects or objects of prime necessity with an exchange value. The first cache contains wool clothing, lots of wool clothing, old, moth-eaten and faded colors. The second cache, which contains money, consists of a tilt mechanism: one of the shelves of the cabinet is carved into its lid is raised as the floor of a school desk. Yet this cache is considered unsafe, and I'm going to drive the mechanism that reveals him to remove the money when someone enters. It is a
18
Journal. Suddenly we realize that in any case, it's useless. At the same time, it becomes obvious that to die and leave the room are the same thing.
The third sequence might have made it possible, if I had almost completely forgotten, give a name to this field or Treblinka or Terezienbourg Katowicze. The theater was perhaps the Requiem de Terezienbourg (Les Temps Modernes, 196 No? Pp). The moral of this episode seems to refer to dreams erased most archaic: You save (sometimes) playing" ;

d = DarkStore(a, ~perec) ;
)

t = DarkTypo.new(d)

d.setupDocument
// or
d.task.play ;
d.setupText("le.sputo. in faccia")

d.setupText("amen. dico. vobis")

d.setupText(~perec)
d.tArr
*/	

/*

~ada = DSAdapter(d)

~gui = DarkStoreGUI(d, ~ada)
*/
