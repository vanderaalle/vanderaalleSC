
// Adapter: one for each config you like

DSAdapter {
	
	// this one should be general
	// you can choose among solenoids, motors, rawspeakers

	var <>darkStore, <>offset ; // offset is busses you skip to reach private
	var <>synthArr, <>flagArr ; 
	var <>minDur, <>maxDur, <>maxAmp, <>minAmp, <>minFreq, <>maxFreq ;
	
	*new { arg darkStore, offset = 2 ;
			^super.new.initAdapter(darkStore, offset)
	}

	initAdapter { arg store, off  ;
		darkStore  = store ;
		offset = off ;
		darkStore.addDependant(this) ;
		maxDur = Array.fill(8, {1}) ; minDur =  Array.fill(8, {0.05}) ;
		maxAmp =  Array.fill(8, {-30}) ; minAmp =   Array.fill(8, {-80})  ;
		maxFreq =  Array.fill(8, {100}); minFreq =  Array.fill(8, {1}) ; 
				// ^ for solenoids we can put [3,3]
		synthArr = Array.fill(8, {0}); // default is speakers
		flagArr = Array.fill(8, {true}) ; // avoiding simultaneous signals to stuff
		// we add defs
		Server.local.waitForBoot{ {ToDCMotor.add ; ToLoudspeaker.add ; ToSolenoid.add }.fork } ;
	}


	// we assume that we start from 1, like in scatolaRossa
	// so we can do: a.setInterface(1, \speaker)
	setInterface { arg which, kind = \speaker;
		var id = [\speaker, \motor, \solenoid].indexOf(kind) ;
		synthArr[which-1] = id
	}

	update { arg theChanged, theChanger, more ;
		var which, selected, dur ;
		if(more[0] == \event){
						if (more[2].notNil){
			//more.postln ;
			// we need to map loudness, centroid, flatness
			// loudness = multiplier for amp
			which = more[2] ;
			selected = [ToLoudspeaker, ToDCMotor, ToSolenoid][synthArr[which]] ;
			dur = more[5].linlin(0, 1, minDur[which], maxDur[which]) ; // flatness
			if (flagArr[which]) {
				selected.play(
					out: which+offset, 
					amp: more[3].linlin(5, 50, minAmp[which], maxAmp[which]).dbamp, // loudness
					dur: dur, 
				// do we need it ? hmmm, it depends on each device
					freq: more[4].linlin(50, 150, minFreq[which], maxFreq[which])
					) ; // centroid
					} ;
			flagArr[which] = false ;
			// epsilon for sure
			{ (dur+0.05).wait ;  flagArr[which] = true }.fork ;
						}
					}
	}

}
