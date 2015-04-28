// Andrea Valle, started on Nov 2009

// VepSource encapsulates
// 	- a synth 
//		- playing audio 
//		- and processing in relation to distance
//		- TIP: do we hardcode the def or do we define a more general framework?
//		- it has a state: on/off 
// 	- a physical location expressed in theta/rho
//		- it is already referred to a certain position
//		- a Source knows nothing about the input space	

// here we specialize the class in order to use VBAP technique

VepSource	 {

	var <>defName ; // the synthDef name
	var <>synth ; // the synth created from synthDef
	var <>theta, <>rho ; // position  
	var <>state ; // can be \on or \off

	*new { arg defName ;
		^super.new.initSource(defName) 
	}

	initSource { arg aDefName ;
		defName = aDefName ;
		// we suppose someone has alredy sent the def (-> vepRunner)
		// at the end of the chain, so we're sure raks players are before
		synth = Synth.newPaused(defName, addAction:\addToTail) ;
		state = \off ;
	}

	// here we are doing strong assumptions about the def
	setAll { arg in, azimuth, dist, del, maxDel ; 
		synth.set(
			\in, in,
			\azimuth, azimuth,
			\dist, dist,
			\maxDel, del, 
			\del, del
			)
	}
	
	

// this was for linear panning 
// could be useful
/*
	calculateLocalizationCoeffs { arg theta, speakerPositions ;
		// it returns the 8-array
		// here we implement the algorithm discussed in the slides 
		var coeffs, loc ;
		coeffs = [] ;
		// if theta is radiants:
		coeffs = speakerPositions.collect { arg ref ;
			loc = abs(ref - theta) ; 
			if (loc > pi){ loc = 2pi - loc } ;
			loc
		} ;
		^coeffs
	}
*/
// synth interface	
	
	run { arg flag = true ;
		synth.run(flag)
	}

	set { arg anArg, aVal;
		this.synth.set(anArg, aVal) ;
	}

}