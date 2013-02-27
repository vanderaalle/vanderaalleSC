/*
r = RuLogPainter("/musica/vedute/ruLog.txt", width:5000).draw.nb.displayCode

*/

RuLogPainter {

	var <>path, <>width, <>height ;
	var <>step, <>yStep ;
	var <>nb, <>log, <>dur ;
	var <>stateDict ;

	*new { arg path, width = 800, height = 600, yStep = 10 ;
	^super.new.initKeyboarder(path, width, height, yStep)
	}

	initKeyboarder { arg aPath, aWidth, aHeight, aYStep ;
		path = aPath;
		width = aWidth ;
		height = aHeight ;
		nb = NB.new("test", width, height) ;
		log = Object.readArchive(path) ;
		dur = this.calcDur ;
		step = width/dur ;
		yStep = aYStep ;
	}

	calcDur {
		var dur = 0 ; 
		log.do({ arg msg ;
			dur = dur+msg[2]
		}) ;
		^dur ;
	}
	
	createStateDict {
		var port, val, delta ;
		var xPlace ;
		var x = 0 ;
		stateDict = Dictionary.new ;
		18.do({ arg port ; stateDict[port+1] = [] }) ;
		log.do({ arg msg, ind ;
			#port, val, delta = msg ; 
			xPlace = (delta+x)*step ;
			stateDict[port] = stateDict[port].add([val, xPlace]) ; 
			x = x + delta ;
		})
	}
	
	drawMsg {
		var stateArr = Array.fill(18, false) ;
		var port, val, delta ;
		var x, y ;
		var xPlace ;
		x = 0 ;
		log.do({ arg msg, ind ;
			#port, val, delta = msg ; 
			xPlace = (delta+x)*step ;
			
			nb.fill(val) ;
			nb.rect(xPlace, port*yStep, step, step) ;
			x = x + delta ;
		})
	}
	
	
	draw {
		var val ;
		var oldState ;
		var stateArr = [], states ;
		this.createStateDict ;
		// init state
		nb.strokewidth(10) ;
		stateDict.keys.asArray.sort.do{ arg key ;
			states = stateDict[key] ;
			oldState = states[0] ;
			states[1..].do({ arg state, ind ;
				nb.stroke(state[0]) ;
				nb.line(oldState[1] , key*yStep, state[1], key*yStep) ;
				oldState = state.copy ;
			})
		}		
	}

	draw2 {
		var val ;
		var oldState ;
		var dist = 10, off, col ;
		var stateArr = [], states ;
		this.createStateDict ;
		// init state
		nb.strokewidth(10) ;
		stateDict.keys.asArray.sort.do{ arg key ;
			states = stateDict[key] ;
			oldState = states[0] ;
			states[1..].do({ arg state, ind ;
				nb.stroke(state[0]) ;
				col = state[1]/400.trunc ;
				off = col*dist ;
				nb.line(oldState[1] %400, key*yStep+col+off, state[1]%400, key*yStep+col+off) ;
				oldState = state.copy ;
			})
		}		
	}
	
}