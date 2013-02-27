/*

k = SpecKeyboarder.new ;



// Da Il gatto bigotto

k.factor_(0.75).transp_(-12).map_(Array.series(58, 0, 16)%57).notes_("Dolente, ma composto") 
// create a special Document

// visualize the Braill Cell while writing
~br = Brailler(k)
~ru = RuController(k).mapMethod_(\braille)

*/

SpecKeyboarder {

	var <>factor, <>transp, <>space, <>crlf, <>map ;
	var <>server ;
	var <doc ;
	var <>title, <>bounds, <>background, <>stringColor, <>font ;
	var <>log, <>rec, <>startTime ;
	var <>notes ;
	var <>brailleDict, <>actualArray ;

	*new { arg factor = 1.0, transp = 0,
			space = true, crlf = true,
			map, 

			title = "Scriptorium", 
			bounds = Rect(1440-640, 900-480, 640, 480),
			background = Color(0,0,0.4),
			stringColor = Color.white, 
			font = SCFont.new("Century Gothic", 30),
			rec = false ;
		^super.new.initKeyboarder([factor, transp, space, crlf, map,title, bounds, background, stringColor, font, rec])
	}

	initKeyboarder { arg args ;
		# factor, transp, space, crlf, map, 
		title, bounds, background, stringColor, font, rec = args ;
		
		map = map ? Array.series(58) ;
		server = Server.local ;
		server.waitForBoot({
			SynthDef(\keySquare, { arg freq, amp = 0.1, transp = 0, out = 0, width=0.5 ;
				Out.ar(out, 
				Pan2.ar(
				EnvGen.kr(Env.perc, doneAction:2)
				*Pulse.ar(freq, mul:amp, width:width)),
				LFNoise1.ar(3))
			}).send(server) ;
		}) ;

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
			.keyDownAction_({ arg doc, key, modifiers, keycode;
				var stringChar = key.asString
					.replace("\r", "   ")
					.replace("\n", "   ")
					.replace("\t", " ") [0] ;
				var width, pitch, amp = 0.2 ;
				if (keycode == 32 and: {space.not}) { amp = 0 } ;
				if (keycode ==  13 and: {crlf.not} ) { amp = 0 } ;
				"key, keycode: ".post ; [key, keycode].postln ;
				if ( keycode >= 65 and: { keycode < 123 } )
					{ keycode = map[keycode-65] + 65 } ;
				"remapped to: ".post ; keycode.postln ;
				width = 0.5/127*keycode.clip2(127) ;
				pitch = (keycode*factor+transp).clip2(136) ;
				"midi, note: ".post ; pitch.post ; ", ".post; pitch.midinote.postln ;
				"\n\n".postln ;
				//pitch.midinote.postln ;
				if (rec) { this.record([pitch, key, modifiers, keycode]) } ; // check me
				Synth(\keySquare, [\freq, pitch.midicps, \amp, amp, \width, width]) ;

				
					
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

}
