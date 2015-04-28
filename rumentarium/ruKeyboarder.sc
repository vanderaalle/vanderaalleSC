// BAD APPROACH - HERE FOR LEGACY


// for 24 setup
// creates array on the fly
// 

RuKeyboarder {
	
	var <>letterDict ;
	var <>map ;
	var <doc ;
	var <>mul ;
	var <>title, <>bounds, <>background, <>stringColor, <>font ;
	var <>log, <>rec, <>startTime ;
	var <>notes ;

	*new { arg mul = 1.0,
			title = "Scriptorium", 
			bounds = Rect(1440-640, 900-480, 640, 480),
			background = Color(0,0,0.4),
			stringColor = Color.white, 
			font = SCFont.new("Century Gothic", 30),
			rec = true ;
		^super.new.initKeyboarder([mul, title, bounds, background, stringColor, font, rec])
	}

	initKeyboarder { arg args ;
		# mul, title, bounds, background, stringColor, font, rec = args ;
		letterDict = Dictionary.new ;
		
		letterDict[\a] = [1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\b] = [0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\c] = [0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\d] = [0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\f] = [0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\g] = [0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\h] = [0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\j] = [0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\l] = [0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\m] = [0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0] ;
		letterDict[\n] = [0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0] ;
		letterDict[\p] = [0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0] ;
		letterDict[\q] = [0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0] ;
		letterDict[\w] = [0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0] ;
		letterDict[\r] = [0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0] ;
		letterDict[\s] = [0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0] ;
		letterDict[\t] = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0] ;
		letterDict[\v] = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0] ;
		letterDict[\x] = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0] ;
		letterDict[\y] = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0] ;
		letterDict[\z] = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1] ;
		
		letterDict[\a] = [1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\e] = [0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\i] = [0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\o] = [0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0] ;
		letterDict[\u] = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0] ;
		
		letterDict[\empty] = Array.fill(24, 0) ;
				
		doc = Document.new.title_(title)
			.bounds_(bounds)
			.background_(background)
			.stringColor_(stringColor) 
			.font_(font)
			.keyDownAction_({arg doc, key, modifiers, keycode;
//				[doc, key, modifiers, keycode].postln ;
				if (key.isAlpha)
					{ this.changed(this, [letterDict[key.toLower.asSymbol]*mul]) }
					{ this.changed(this, [letterDict[\empty]]) }
					 ;
				if (rec) { this.record(key) } ; // check me
			});
		log = [] ;
		startTime = thisThread.seconds ;
		notes = ""
	}

	reset {
		doc.string_("");
		startTime = thisThread.seconds ;		
		log = [] ;
	}	


	// a routine for some start up infos in the doc 
	presentation { arg string, presFont = Font.new("Gill Sans", 90), 
			color =  Color.new(1, 116/255,0);
		Routine({
			doc.stringColor_(color) 
				.font_(presFont) 
				.string_(string); 
			100.do({ 	arg j ;
				doc.stringColor_(color.alpha_((100-j)*0.01)) ;
				0.05.wait ;
			}) ;
			doc.string_("") 
				.stringColor_(Color.white) 
				.font_(font) ;
		}).play(AppClock) ;
		doc.front

	}

	record { arg key ;
		log = log.add([thisThread.seconds-startTime].add(key))
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
				key =  item[1]; 
				doc.string_(doc.string++key) ;
				this.changed(this, [key]);
				waitTime.wait ;
			})
		}).play(AppClock) ;
	}

}