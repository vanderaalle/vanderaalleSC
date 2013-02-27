RuMaster {

	// The idea here is that we put together a certain number fo arduinos, 
	// and we have a unique interface, a setup
	// with 3 arduinos we can have 1-18 ids
	// and we can add a symbolic name for each piece of the perc setup 
	// on init:
	var <>arduinoList ; // a list of arduinos
	// a list of names for the instruments, simbols or string
	var <>instrumentList ; 
	// an association between low level arduinos/ports and IDs 
	var <>mapDict ;
	// an association between instrNames and IDs 
	var <>instrDict ;
	var <>log ; // for event recording 
	var <>brailleMapping, <>brailleMul ; // fr braille mapping
	var <>brailleMappingStyle ; // can be \all or \single
	
	
	*new { arg arduinoList, instrumentList  ; 
		^super.new.initRuMaster(arduinoList, instrumentList) 
	}

	initRuMaster { arg anArduinoList, anInstrumentList ;
		arduinoList = anArduinoList ; 
		instrumentList = anInstrumentList ;
		this.createMapDict ;
	}

	close {
		arduinoList.do{|ard| ard.close}
	}
	
	// defines the association between low level ports and IDs/instrs
	createMapDict {
		// the numerical IDs for the whole setup
		// 6 is the hardcoded number of PWM ports of arduino
		// [3,5, 6,9,10,11] are their indices
		var iDs = Array.series(6*arduinoList.size) ;
		var arduino, port ;
		mapDict = IdentityDictionary.new ;
		instrDict = IdentityDictionary.new ;
		iDs.do{ arg id ;
			arduino = arduinoList[(id/6).asInteger] ;
			port = [3,5, 6,9,10,11][id%6] ;
			mapDict.put(id+1, [arduino, port])	;
		} ; 
		// could be optimized but we're dealing with little numbers
		instrumentList.do{ arg instr, ind ;
			instrDict.put(instr, ind+1)	;
			}
	}


// interface for control
// it assumes that you send voltage values normalized in [0.0, 1.0]
// remaps to 255

	// set a value to a port by ID or instr name
	set { arg idOrName, val ;
		val = val.min(1.0) ; // prevents problems
		if (idOrName.class == Integer)
			{ this.setByID(idOrName, val) }
			{ this.setByName(idOrName, val) } ;
	}

	// here you pass an arr
	setAll { arg arr, lag = 0.0 ;
		fork {
			arr.do{ arg val, ind ;
				//this.setByID(ind+1, val)
				this.set(ind+1, val) ;
				lag.wait ;
			}
		}
	
	}

/*
	// includes threshold
	setAll2 { arg arr, lag = 0.0, thresh = 0.2 ;
		fork {
			arr.do{ arg val, ind ;
				if (val >= thresh) { this.set(ind+1, val) } ;
				lag.wait ;
			}
		}
	
	}
*/

	
//  private
	// set a value to a port by ID	
	setByID { arg id, val ;
		var arduino, port ;
		#arduino, port = mapDict.at(id)	;
		arduino.send($w, $a, port, 255*val) ;
		this.changed(this, [id, val])
	}
	
	// set a value to a port by instr name
	// you can use strings or symbols
	setByName { arg name, val ;
		this.setByID(instrDict[name.asSymbol], val)
	}
// end of private	

	// reset all to zero  
	// fast low level interface, no use of set method
	zero {
		arduinoList.do{ arg ard ;
			[3,5,6,9,10,11].do{ arg port ;
				ard.send($w, $a, port, 0) }
			} ;
	}	

/*
	// for braille coding
	// for 18 elements
	brailleMapper { arg brailleArr ;
		case 
		{ brailleMapping == \all }
			{ ^(brailleArr.dup(3)*brailleMul).flat }
		{ brailleMapping == \first }
			{ ^(brailleArr*brailleMul++Array.fill(12, 0)) }
		{ brailleMapping == \second }
			{ ^(Array.fill(6, 0)++(brailleArr*brailleMul)++Array.fill(6, 0)) }
		{ brailleMapping == \third }
			{ ^(Array.fill(12, 0)++(brailleArr*brailleMul)) }
	}
*/

	// for braille coding
	// for 24 elements
	brailleMapper { arg brailleArr ;
		case 
		{ brailleMapping == \all }
			{ ^(brailleArr.dup(4)*brailleMul).flat }
		{ brailleMapping == \first }
			{ ^(brailleArr*brailleMul++Array.fill(18, 0)) }
		{ brailleMapping == \second }
			{ ^(Array.fill(6, 0)++(brailleArr*brailleMul)++Array.fill(12, 0)) }
		{ brailleMapping == \third }
			{ ^(Array.fill(12, 0)++(brailleArr*brailleMul)++Array.fill(6, 0)) }
		{ brailleMapping == \fourth }
			{ ^(Array.fill(18, 0)++(brailleArr*brailleMul)) }
	}

	// for braille coding, style \single
	// for 24 elements
	setSingleBrailleMapper { arg brailleArr ;
		case 
		{ brailleMapping == \all }
			{ this.setAll(this.brailleMapper(brailleArr).flat) } // like before, useless 
		{ brailleMapping == \first }
			{ (brailleArr*brailleMul).do{|i, j| this.setByID(j+1, i)} }
		{ brailleMapping == \second }
			{ (brailleArr*brailleMul).do{|i, j| this.setByID(j+7, i)} }
		{ brailleMapping == \third }
			{ (brailleArr*brailleMul).do{|i, j| this.setByID(j+13, i)} }
		{ brailleMapping == \fourth }
			{ (brailleArr*brailleMul).do{|i, j| this.setByID(j+19, i)} }
	}


	// for eventual notifications
	update { arg theChanged, theChanger, more;
		// it assumes that the sender's more is an arg for the chosen method
		case { theChanged.class == RuLooper24 } 
				{ if (more[0]==\event) { this.set(more[1], more[2]) } }
			{ theChanged.class == RuScore } 
				{ this.setAll(more[0]) }			
			{ theChanged.class == BrailleScheduler } 
				{
				if (brailleMappingStyle == \all) 
					{this.setAll(this.brailleMapper(more[0]).flat)}
					{this.setSingleBrailleMapper(more[0])} 
				}
// remove me
			{ theChanged.class == RuKeyboarder } 
				{ this.setAll(more[0]) }
// 
			{ theChanged.class == Keyboarder } 
				{ if (more[0].isAlpha) 
						{
							{
						this.setByID((more[0].toLower.ascii-97) % 24 + 1, more[1]) ;
						1.wait ; // hard coded
						this.setByID((more[0].toLower.ascii-97) % 24 + 1, 0) ;
							}.fork
						
						}
				  }
	}

	// Logger interface 
	recLog {
		log = RuLogger(this).rec ;		
	}

	stopRecLog {
		if (log.notNil) { log.stopRec}
	}

	playLog {
			if (log.notNil) { log.play }
	}

	saveLog { arg pathname ;
			if (log.notNil) { log.save(pathname) }
	}

	openLog { arg pathname ;
			log = RuLogger(this).open(pathname) 
	}

	
}



RuLogger {

	var <>master ;
	var <>logArr ;
	var <>startTime ;
	var <>active ;
	
	*new { arg master ; 
		^super.new.initRuMaster(master) 
	}

	initRuMaster { arg aMaster ;
		master = aMaster ;
		master.addDependant(this) ; 
	}

	update { arg theChanged, theChanger, more;
		var time ;
		if (active == true)
			{ 
			time = thisThread.seconds-startTime ;
			logArr = logArr.add(more.add(time)) ;
			startTime = thisThread.seconds
			 }
	}
	
	rec {
		startTime = thisThread.seconds ;
		logArr = [] ;
		active = true
	}


	stopRec {
		active = false
	}

	// writes/reads to file
	save { arg pathname ; 
		logArr.writeArchive(pathname) ;
	}
	
	open {  arg pathname ;
		logArr = Object.readArchive(pathname)
	}

	play {
		var port, value, time ;
		Task({
			logArr.do({ arg ev ;
				#port, value, time = ev ;
				time.wait ;
				// low lev
				master.setByID(port, value)
			})
		}).play
	}

}