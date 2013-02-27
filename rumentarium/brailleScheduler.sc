/*

(
~quot = "De facie autem Lune, que ad aspectum nostrum vergit, primo loco dicamus. Quam, facilioris intelligentie gratia, in duas partes distinguo, alteram nempe clariorem, obscuriorem alteram: clarior videtur totum hemispherium ambire atque perfundere, obscurior vero, veluti nubes quedam, faciem ipsam inficit maculosamque reddit. Iste autem macule, subobscure et satis ample, unicuique sunt obvie, illasque evum omne conspexit; quapropter magnas, seu antiquas, eas appellabimus, ad differentiam aliarum macularum amplitudine minorum, at frequentia ita consitarum, ut totam Lunarem superficiem, presertim vero lucidiorem partem, conspergant; he vero a nemine ante nos observate fuerunt: ex ipsarum autem sepius iteratis inspectionibus in eam deducti sumus sententiam, ut certo intelligamus, Lune superficiem, non perpolitam, equabilem, exactissimeque sphericitatis existere, ut magna philosophorum cohors de ipsa deque reliquis corporibus celestibus opinata est, sed, contra, inequalem, asperam, cavitatibus tumoribusque confertam, non secus ac ipsiusmet Telluris facies, que montium iugis valliumque profunditatibus hinc inde distinguitur. Apparentie vero, ex quibus haec colligere licuit, eiusmodi sunt." ;

~brs = BrailleScheduler(~quot, 2) ;

o = ArduinoSMS("/dev/tty.usbserial-A9007LwD", 115200); // blue
p = ArduinoSMS("/dev/tty.usbserial-A1001N7X", 115200); // green
q = ArduinoSMS("/dev/tty.usbserial-A9007LnA", 115200);  // white

~instrList = [
	\tintinnabolum1,
	\tintinnabolum2,
	\patella1,
	\patella2,
	\campana1,
	\campana2,
	
	\sistrum1,
	\sistrum2,
	\sphera,
	\tympanum,
	\campanarium,
	\globus,
	
	\crepitacolum1,
	\crepitacolum1,
	\tubus1,
	\tubus2,
	\discus1,
	\discus2
] ;

k = RuMaster([o,p,q], ~instrList) ;
~brs.addDependant(k) ;


~brs.play(0.005, 0.2)

)


*/

BrailleScheduler {
	
	// on init:
	// - needs a text
	// - defines a scanning interval
	// on start:
	// - takes a char
	// - converts it to an arr via dic
	// - updates
	var <>scanInterval ; // the interval to wait for next scan
	var <>spaceMul ; // a mul for the scanInterval, used for spaces
	var <>brailleDict ; // to map to braille the char
	var <>inText; // the text to be parsed
	var <>task ; // the scheduling task
	var <>actualArray ; // the actual arr in scheduling. Can be retrieved from dependants
		
	// constructor: you can start with an existing graphDict
	*new { arg inText, spaceMul ; 
		^super.new.initBrScheduler(inText, spaceMul) 
	}

	initBrScheduler { arg aInText, aSpaceMul ;
		inText = aInText.replace("\r", "   ").replace("\n", "   ").replace("\t", " ")  ;
		spaceMul = if (aSpaceMul.isNil) { 1 }{ aSpaceMul } ;
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
			// space
			\32:		[0,0,0,0,0,0],
			// ASCII num for punct (not exactly clear for debugging...)
			\44: 	[0,1,0,0,0,0],
			\45: 	[0,1,0,0,0,0],
			\59: 	[0,1,1,0,0,0],
			\58: 	[0,1,0,0,1,0],	
			\46: 	[0,1,0,0,1,1],		
			\33: 	[0,1,1,0,1,0],		
			\63: 	[0,1,1,0,0,1],
			\39: 	[0,1,1,0,1,1],
			\40: 	[0,1,1,0,1,1],		
			\95:		[1,1,1,1,1,1],
			\41: 	[0,1,1,0,1,1]		// the same
			);
		task = Task({
				inText.do({ arg stringChar ;
				stringChar.postln ;
				// the Upper case
				if (stringChar.isAlphaNum && stringChar.isUpper) {
						stringChar = stringChar.toLower ;
						this.convert($@);
						scanInterval.wait ;
				} ;
				// general case
				if ( stringChar.isAlphaNum || stringChar.isPunct || stringChar.isSpace )
						{ 	this.convert(stringChar) ;
							scanInterval.wait 
						} ;
				// space handling
				if ( stringChar.isSpace ) { (scanInterval*spaceMul).wait };
							
			})
		}) ;
		}

	convert { arg stringChar ;
		if (stringChar.isAlphaNum.not ) { stringChar = stringChar.ascii } ;
		actualArray = brailleDict[stringChar.asSymbol] ;
		this.changed(this, [actualArray]) ;
	}
	

// task interface
	play { arg scan = 0.1;
		scanInterval = scan ;
		task.play(AppClock) ;
	}

	start {
		task.start(AppClock) ;
	}

	pause {
		task.pause ;
	}

// the same
	stop {
		task.stop ;
	}

	reset {
		task.reset ;
	}


}
