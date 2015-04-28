VepStarterStereo {

	var <>folder, <>numSources, <vol, <subVol, <noiseVol ;
	var <>runner, <>dsp, <>gui ;
	
	*new { arg folder, numSources, vol, subVol, noiseVol ;
		^super.new.initVepStarterStereo(folder, numSources, vol, subVol, noiseVol) ;
	}
	
	initVepStarterStereo { arg aFolder, aNumSources, aVol, aSubVol, aNoiseVol ;
		#folder, numSources, vol, subVol, noiseVol 
		= [aFolder, aNumSources, aVol, aSubVol, aNoiseVol ] ;
	 	runner = VepRunnerStereo.newPaused(folder, numSources:350) ;
	 	runner.addDependant (this) ;
	 	runner.syncToServer
	}
	
	startDsp  {
		dsp = VepDSP.newPaused(runner) ;
		dsp.addDependant (this) ;
		dsp.vol_(vol) ; 
		dsp.subVol_(subVol) ; 
		dsp.noiseVol_(noiseVol) ;
		dsp.syncToServer ;
	}
	
	startGui {
		gui = VepGui(runner, dsp) ;
	}
	
	update { arg theChanged, theChanger, more ;
		case 
		{ theChanged.class == VepRunnerStereo && more[0] == \initOk }
			{ this.startDsp  }		
		{ theChanged.class == VepDSP && more[0] == \initOk }
			{ this.startGui ;
			// no need to being notified
			runner.removeDependant(this) ;
			dsp.removeDependant(this) ;
			 }		
	}

// FORWARDER 
	vol_ { arg val ;
		dsp.vol_(val) ;
	}


	subVol_ { arg val ;
		dsp.subVol_(val) ;
	}

	noiseVol_ { arg val ;
		dsp.noiseVol_(val) ;
	}

	spread_ { arg val ;
		runner.spread_(val) ;
	}
	
}