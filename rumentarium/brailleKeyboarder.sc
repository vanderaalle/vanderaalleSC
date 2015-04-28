/*

// create a special Document
~brk = BrailleKeyboarder.new ;

// visualize the Braill Cell while writing
~br = Brailler(~brk)
~ru = RuController(~brk).mapMethod_(\braille)
*/



BrailleKeyboarder {

	var <doc ;
	var <>title, <>bounds, <>background, <>stringColor, <>font ;
	var <>log, <>rec, <>startTime ;
	var <>notes ;
	var <>brailleDict, <>actualArray ;

	*new { arg
			title = "Scriptorium", 
			bounds = Rect(1440-640, 900-480, 640, 480),
			background = Color(0,0,0.4),
			stringColor = Color.white, 
			font = SCFont.new("Century Gothic", 30),
			rec = false ;
		^super.new.initKeyboarder([title, bounds, background, stringColor, font, rec])
	}

	initKeyboarder { arg args ;
		# title, bounds, background, stringColor, font, rec = args ;
		
		brailleDict = (
			\1: [1,0,0,0,0,0],
			\2: [1,1,0,0,0,0],
			\3: [1,0,0,1,0,0],	
			\4: [1,0,0,1,1,0],		
			\5: [1,0,0,0,1,0],		
			\6: [1,1,0,1,0,0],		
			\7: [1,1,0,1,1,0],		
			\8: [1,1,0,0,1,0],		
			\9: [0,1,0,1,0,0],		
			\0: [0,1,0,1,1,0],		
		
			\a: [1,0,0,0,0,0],
			\b: [1,1,0,0,0,0],
			\c: [1,0,0,1,0,0],	
			\d: [1,0,0,1,1,0],		
			\e: [1,0,0,0,1,0],		
			\f: [1,1,0,1,0,0],		
			\g: [1,1,0,1,1,0],		
			\h: [1,1,0,0,1,0],		
			\i: [0,1,0,1,0,0],		
			\j: [0,1,0,1,1,0],		
		
			\k: [1,0,1,0,0,0],
			\l: [1,1,1,0,0,0],
			\m: [1,0,1,1,0,0],	
			\n: [1,0,1,1,1,0],		
			\o: [1,0,1,0,1,0],		
			\p: [1,1,1,1,0,0],		
			\q: [1,1,1,1,1,0],		
			\r: [1,1,1,0,1,0],		
			\s: [0,1,1,1,0,0],		
			\t: [0,1,1,1,1,0],		
		
			\u: [1,0,1,0,0,1],
			\v: [1,1,1,0,0,1],
			\w: [0,1,0,1,1,1],	
			\x: [1,0,1,1,0,1],		
			\y: [1,0,1,1,1,1],		
			\z: [1,0,1,0,1,1],		
			
			// upper selector
			\64: 	[0,1,1,0,0,1],
			// space and tab
			\32:		[0,0,0,0,0,0],
			\tab:	[0,0,0,0,0,0],
			// ASCII num for punct (not exactly clear for debugging...)
			\44: 	[0,1,0,0,0,0],
			\59: 	[0,1,1,0,0,0],
			\58: 	[0,1,0,0,1,0],	
			\46: 	[0,1,0,0,1,1],		
			\33: 	[0,1,1,0,1,0],		
			\63: 	[0,1,1,0,0,1],
			\40: 	[0,1,1,0,1,1],		
			\41: 	[0,1,1,0,1,1]		// the same
			);
		
		doc = Document.new.title_(title)
			.bounds_(bounds)
			.background_(background)
			.stringColor_(stringColor) 
			.font_(font)
			.keyDownAction_({ arg doc, stringChar, modifiers, keycode;
				stringChar = stringChar.asString
					.replace("\r", "   ")
					.replace("\n", "   ")
					.replace("\t", " ") [0] ;
				// the Upper case
				if (stringChar.isAlphaNum && stringChar.isUpper) {
						stringChar = stringChar.toLower ;
						this.convert($@);
				} ;
				// general case
				if ( stringChar.isAlphaNum || stringChar.isPunct || stringChar.isSpace )
						{ this.convert(stringChar) } ;
				if (rec) { this.record([stringChar, modifiers, keycode]) } ; // check me
			});
			log = [] ;
			startTime = thisThread.seconds ;

	}



	convert { arg stringChar ;
		case { stringChar == $\t} 
				{ stringChar = $ } 
			{ stringChar == $\r } 
				{ stringChar = $ } 
			 { stringChar == $\n } 
				{ stringChar = $ } ;
		if (stringChar.isAlphaNum.not ) { stringChar = stringChar.ascii } ;
		actualArray = brailleDict[stringChar.asSymbol] ;
		this.changed(this, [actualArray]) ;
	}




	reset {
		doc.string_("");
		startTime = thisThread.seconds ;		
		log = [] ;
	}	


	record { arg arr ;
		log = log.add([thisThread.seconds-startTime].addAll(arr))
	}

	writeLog { arg path = "/log.arc";
		log = [notes].addAll(log) ;
		log.writeArchive(path)
	}
	
	openLog { arg path = "/log.arc";
		log = Object.readArchive(path) ;
		notes = log[0] ;
		log = log[1..] ;
	}

/*
	playFromLog { 
		var key, time ;
		var width, pitch, amp = 0.2 ;
		var waitTime, actualTime, nextTime ;
		Routine({
			log[..log.size-2].do({ arg item, index ;
			item.postln ;
				actualTime = item[0] ;
				nextTime = log[index+1][0] ;
				waitTime = nextTime - actualTime ;
				// should protect against not-Ascii
				key = if (item[4] <= 126) { item[2] } { " " } ; 
				doc.string_(doc.string++key) ;
				
				pitch = item[1] ;				
				width = 0.5/127*pitch.clip2(127) ;
				Synth(\keySquare, [\freq, pitch.midicps, \amp, amp, \width, width]) ;
				
				waitTime.wait ;
			})
		}).play(AppClock) ;
	}
*/
}


Brailler {
	
	// on init:
	// - gets a dataSender, generic object sending "changed" with pollable array
	// - becomes a dependant and start listening
	var <>dataSender ;
	var <>brailleArray ; // the actual arr in scheduling. 
	var <>width, <>height, <>w, <>u;
	
	*new { arg dataSender ; 
		^super.new.initBrailler(dataSender) 
	}

	initBrailler { arg aDataSender ;
		dataSender = aDataSender ;
		dataSender.addDependant(this) ;
		width = 400; height = 600 ;
		w = Window.new("Braille Cell", Rect(99, 99, width, height), false) ;
		u = GUI.userView.new(w, Rect(0, 0, width, height)) ;
		brailleArray = [0,0,0,0,0,0] ;
		u.drawFunc = {
			var vert, hor ;
			Pen.width= 1;
			Pen.strokeColor= Color.black;
			brailleArray.do{ |val, ind|	
					vert = ind%3 ;
					hor = (ind/3).asInteger ;
					Pen.fillColor = if ( val == 1) { Color.black } { Color.white };
					Pen.strokeOval(Rect(200*hor+15, 200*vert+15, 170, 170));
					Pen.fillOval(Rect(200*hor+15, 200*vert+15, 170, 170));
			};
		w.view.background = Color.white ;
		w.front ;
		}
	}


	update { arg theChanged, theChanger, more;
		brailleArray = more[0] ;
		u.refresh ;
	}


}
