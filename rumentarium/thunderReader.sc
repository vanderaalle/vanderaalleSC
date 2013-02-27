
// Modelled after BrailleScheduler but quite specific
// it handles a RuMaster to which it sends msgs
// more here the char has a dur. Each char triggers a dur
// after the dur has passed, a zero msg is sent to the id (see convert) 


ThunderReader {
	
	// on init:
	// - needs a text
	// - defines a scanning interval
	// on start:
	// - takes a char
	// - converts it to an ID
	// - send msg to ruMaster
	var <>ruMaster ; // the ruMastser to control
	var <>amp ; // the passed value tu ruMaster
	var <>duration ; // each char's dur
	var <>offset ; // the task offset (i.e. does it start immediately?)
	var <>iterations ; // how many times the text is repeated
	var <>scanInterval ; // the interval to wait for next scan
	var <>spaceFactor ; // a mul for the scanInterval, used for spaces, punctuations, tabs etc
	var <>dict ; // to map to ID for each char
	var <>inText; // the text to be parsed
	var <>task ; // the scheduling task

		
	// constructor: you can start with an existing graphDict
	*new { arg ruMaster ; 
		^super.new.initThunderReader(ruMaster) 
	}

	initThunderReader { arg aRuMaster ;
		ruMaster = aRuMaster ;
		// the only dubbed one is z, mapped onto x 
		// hapax: z, j, x
		dict  = ( 
			\a: 1,
			\b: 2,
			\c: 3,
			\d: 4,
			\e: 5,
			\f: 6,
			\g: 7,
			\h: 8,
			\i: 9,
			\j: 10,
			\k: 11,
			\l: 12,
			\m: 13,
			\n: 14,
			\o: 15,
			\p: 16,
			\r: 17,
			\s: 18,
			\t: 19,
			\u: 20,
			\v: 21,
			\w: 22,
			\x: 23,
			\y: 24,
			\z: 24 
		) ;	
		task = Task({
				"START!".postln ;
				offset.wait ;
				iterations.do{
					inText.do({ arg stringChar ;
					stringChar.postln ;
					// general case: if is a letter, convert
					if ( stringChar.isAlpha )
						{ 	this.convert(stringChar) ;
							scanInterval.wait 
						} 
						{ (scanInterval*spaceFactor).wait 
						};
							
					})
				} ;
				"DONE!".postln ;
			}) ;
		}

	convert { arg alphaChar ;
		var id = dict[alphaChar.asSymbol] ;	
		{ ruMaster.setByID(id, amp); duration.wait ; ruMaster.setByID(id, 0)}.fork
	}
	

	inspect { arg string; 
		var names = (
		1: \vibragomma,
		2: \vibraferro,
		3: \brioschi,
		4: \lapsang,
		5: \earlgrey,
		6: \spilli,
		7: \piccolopaiste,
		8: \mediopaiste,
		9: \griglia,
		10: \campanario,
		11: \macbaren,
		12: \ufip,
		13: \latakia,
		14: \mixture,
		15: \blackvanilla,
		16: \blackvelvet,
		17: \mais,
		18: \tuboviola,
		19: \boccia,
		20 : \sferamagica,
		21: \campanahi,
		22: \campanalo,
		23: \tubopiccolo,
		24: \tubogrande
		) ;
		
		string.do{|l| names[dict[l.asSymbol]].postln}
	}

// task interface
	play { arg text = "", scan = 0.1, repeat = 1, before = 0, spaceMul = 1, vol = 1, dur = 1;
		scanInterval = scan ;
		iterations = repeat ;
		offset = before ;
		// convert to lower and convert special spaces in weighted space
		inText = text.toLower.replace("\r", " ").replace("\n", " ").replace("\t", " ")  ;
		spaceFactor = spaceMul ;
		amp = vol ;
		duration = dur ;
		task.play ;
	}

	start {
		task.start ;
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


ThunderInspector {
	
	
	*inspect { arg string; 
		var dict  = ( 
			\a: 1,
			\b: 2,
			\c: 3,
			\d: 4,
			\e: 5,
			\f: 6,
			\g: 7,
			\h: 8,
			\i: 9,
			\j: 10,
			\k: 11,
			\l: 12,
			\m: 13,
			\n: 14,
			\o: 15,
			\p: 16,
			\r: 17,
			\s: 18,
			\t: 19,
			\u: 20,
			\v: 21,
			\w: 22,
			\x: 23,
			\y: 24,
			\z: 24 
		) ;	
		var names = (
		1: \vibragomma,
		2: \vibraferro,
		3: \brioschi,
		4: \lapsang,
		5: \earlgrey,
		6: \spilli,
		7: \piccolopaiste,
		8: \mediopaiste,
		9: \griglia,
		10: \campanario,
		11: \macbaren,
		12: \ufip,
		13: \latakia,
		14: \mixture,
		15: \blackvanilla,
		16: \blackvelvet,
		17: \mais,
		18: \tuboviola,
		19: \boccia,
		20 : \sferamagica,
		21: \campanahi,
		22: \campanalo,
		23: \tubopiccolo,
		24: \tubogrande
		) ;
		
		string.do{|l| names[dict[l.asSymbol]].postln}
	}
	
}
