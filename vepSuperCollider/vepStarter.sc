// A starter interface for the VEP8 Audio Machine
VepStarter {

	var <>folder, <>project, <>numSources, <vol, <subVol, <noiseVol ;
	var <>runner, <>dsp, <>gui ;
	
	*new { arg folder, project, numSources, vol, subVol, noiseVol = 0 ;
		^super.new.initVepStarter(folder, project, numSources, vol, subVol, noiseVol) ;
	}
	
	initVepStarter { arg aFolder, aProject, aNumSources, aVol, aSubVol, aNoiseVol ;
		#folder, project, numSources, vol, subVol, noiseVol 
		= [aFolder, aProject, aNumSources, aVol, aSubVol, aNoiseVol ] ;
	 	runner = VepRunner.newPaused(folder, project, numSources:350) ;
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
		{ theChanged.class == VepRunner && more[0] == \initOk }
			{ this.startDsp  }		
		{ theChanged.class == VepDSP && more[0] == \initOk }
			{ this.startGui ;
			// no need to being notified
			runner.removeDependant(this) ;
			dsp.removeDependant(this) ;
			 }		
	}

// FORWARDER after dB conversion
	vol_ { arg val ;
		dsp.vol_(val.dbamp) ;
	}


	subVol_ { arg val ;
		dsp.subVol_(val.dbamp) ;
	}

	noiseVol_ { arg val ;
		dsp.noiseVol_(val.dbamp) ;
	}

	spread_ { arg val ;
		runner.spread_(val) ;
	}
	
}