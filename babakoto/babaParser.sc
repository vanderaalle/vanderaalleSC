/*
THE BABAKOTO PROJECT: BabaParser
-a- started: 18/06/08

BabaParser defines the Roar language and it's its parser
The Roar language mimics the main methods of BabaPatcher.
It uses two different constructs:
- single tokens (synth name) as synth selectors
- setter methods (e.g. freq 1000) to set arg values
 
Modelled after GraphParser

Last updated: 030708

// andrea valle
// http://www.cirma.unito.it/andrea/
// andrea.valle@unito.it

*/


BabaParser {


	var <>babaPatcher ;
	// log a string containing all the commands
	// can be written to file or passed to display
	var <>log ; //, <>lastCmdString ;
	// for log
	var <>startime ;
	var <>selector ; // container for selector
	var <>cmdDict ;
	var <>controller ;
	
	*new { arg babaPatcher, controller ;  
		^super.new.initParser(babaPatcher, controller) 
	}

	initParser { arg aBabaPatcher, aController ;
		babaPatcher = aBabaPatcher ;
		controller = aController ;
		log = "" ;
		startime = thisThread.seconds ;
	}

	// split lines
	// use always this
	parse { arg lines ;
			lines.split($\n).reject({arg i ; i=="" }).postln
				.do({ |line| this.parseLine(line) })
		} 

	// PRIVATE
	parseLine { arg aString ;
		var arr, method, argName, val, to ;
		var singles = ['@', '.', '+', '-', \run] ;
		arr = aString.split($ ).reject({arg i ; i=="" ; }) ; // strip away spaces
		// single word: it's a selector
		if ( (arr.size == 1) && (singles.includes(arr[0].asSymbol).not ) )  
			{ selector = arr[0].asSymbol } 
			{
		if 
			// this forwards to an external object (controller)
			// the syntax def is left to the object
			( selector == \ext ) 	
				{ "si".postln ; controller.parse(arr.postln) }
			{
			case 
				{ arr[1].asSymbol == \min } 
				{ 	method = \setMin ; 
					argName = arr[0].asSymbol ;
					val = arr[2].asInteger ;
					babaPatcher.perform(method, selector, argName, val)
				}
				{ arr[1].asSymbol == \max } 
				{ 	method = \setMax ; 
					argName = arr[0].asSymbol ;
					val = arr[2].asInteger ;
					babaPatcher.perform(method, selector, argName, val)
				}
				{ arr[0].asSymbol == \in } 
				{ 	method = \in ; 
					argName = arr[1].asSymbol ;
					val = arr[2].asSymbol ;
					babaPatcher.perform(method, selector, argName, val)
				}
				{ arr[0].asSymbol == \to } 
				{ 	method = \in ; 
					argName = arr[2].asSymbol ;
					val = arr[1].asSymbol ;
					babaPatcher.perform(method, val, argName, selector)
				}

				{ arr[0].asSymbol == \out } 
				{ 	method = \out ; 
					if (arr[1].asSymbol == \p) { val = nil }
						{ val = arr[1].asInteger } ;
					babaPatcher.perform(method, selector, val)
				}

				{ arr[0].asSymbol == \run } 
				{ 	method = \run ; 
					val = arr[1].interpret ;
					babaPatcher.perform(method, selector, val)
				}

				{ arr[0].asSymbol == '-' } 
				{ 	method = \moveBefore ; 
					val = arr[1] ;
					babaPatcher.perform(method, selector, val)
				}

				{ arr[0].asSymbol == '+' } 
				{ 	method = \moveAfter ; 
					val = arr[1] ;
					babaPatcher.perform(method, selector, val)
				}
				
				{ arr[0].asSymbol == '@' } 
				{ 	method = \moveToHead ; 
					babaPatcher.perform(method, selector)
				}
				{ arr[0].asSymbol == '.' } 
				{ 	method = \moveToTail ; 
					babaPatcher.perform(method, selector)
				}

				// set: what remains
				{ arr[0].asSymbol != \out } 
				{ 	method = \set ; 
					argName = arr[0].asSymbol ;
					val = arr[1].asFloat ;
					babaPatcher.perform(method, selector, argName, val) ;
				}

			 }
			 }
	}
	
	toLog { arg arr ;
		var str = (thisThread.seconds-startime).asTimeString[3..7]  ;
		arr.do({ arg i ;
			str = str + i.asString ;
		}) ;
		log = log + "\n" + str ;
	}
	
		
}
