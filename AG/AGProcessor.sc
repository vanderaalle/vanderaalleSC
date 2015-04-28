AGProcessor {


// converted from Praat

	importPitch { arg pitchContourFileName ;
		var header = [], line, arr = [] ;
		var pitchContourFile ;
		pitchContourFileName = pitchContourFileName ? 
							(String.scDir++"/sounds/praatTmp.pitchContour") ;
		pitchContourFileName = if ( pitchContourFileName[0]==$/, { pitchContourFileName }, 
									{ String.scDir++"/"++pitchContourFileName }) ;
		
		pitchContourFile = File.new(pitchContourFileName, "r") ;		14.do({arg i; header = header.add(pitchContourFile.getLine); }) ; // eliminate header info
		header.postln ;
		//("xmin, xmax, nx, dx, x1, maxnFormants\n"+ 
		//	 header[3..].asFloat+ "\n\n").postln ;
		line = pitchContourFile.getLine.asFloat ;
		while({line.notNil}, {	arr = arr.add(line.asFloat) ; 
							line = pitchContourFile.getLine ;
							}) ;
		pitchContourFile.close ;
		^arr ;
		} 



	importFormant { arg formantFileName ;
		var formant ;
		var freqList, bwList, intensityList ; 
		var header = [], line, arr = [] ;
		var intensity, nF, couples, i ;
		var frameFreq = [], frameBw = [] ;
		formantFileName = formantFileName ? (String.scDir++"/sounds/praatTmp.formant") ;
		formantFileName = if ( formantFileName[0]==$/, { formantFileName }, 
									{ String.scDir++"/"++formantFileName }) ;
		
		formant = File.new(formantFileName, "r") ;			9.do({arg i; header = header.add(formant.getLine); }) ; // eliminate header info
		header.postln ;
		// # xmin, xmax, nx, dx, x1, maxnFormants = header[3..].asFloat ;
		("xmin, xmax, nx, dx, x1, maxnFormants\n"+ 
			 header[3..].asFloat+ "\n\n").postln ;
		line = formant.getLine.asFloat ;
		//arr = [line] ;
		while({line.notNil}, {arr = arr.add(line.asFloat); line = formant.getLine; }) ;
		// arr.postln;
		i = 0 ;
		while {i != arr.size} { frameFreq = [] ; frameBw = [] ;
							intensityList = intensityList.add(arr[i]) ; 
							nF = arr[i+1].asInt; 
							couples = arr[i+2..i+2+(nF*2)-1] ;
							forBy(0, couples.size-1, 2, 
								{ arg i; 
								frameFreq = frameFreq.add(couples[i]) ;
								frameBw = frameBw.add(couples[i+1]) ;
								 }
								) ;
							freqList = freqList.add(frameFreq) ;
							bwList = bwList.add(frameBw) ;
							i = (i+2+(nF*2)); 
							} ;
					
		formant.close ;
		//[freqList, "\n", bwList, "\n",intensityList,"\n",].do({arg i; i.postln}) ;
		^[freqList, bwList, intensityList] ; 
		} 





// import 
// this is for Praat Pitch Log
// i.e. manual annotation of pitch contour exported to log file
// NOTE: could be moved to Praat class?	
// option for conversion set to true: easier for pitch operations
	
	importPitchLog { arg pitchLogFileName, toMidi = true ;
		var line, arr = [] ;
		var pitchLog, time, pitch ;
		pitchLogFileName = pitchLogFileName ? 
							(String.scDir++"/sounds/praatTmp.pitchLog") ;
		pitchLogFileName = if ( pitchLogFileName[0]==$/, { pitchLogFileName }, 
									{ String.scDir++"/"++pitchLogFileName }) ;
		pitchLog = File.new(pitchLogFileName, "r") ;
		line = pitchLog.getLine ; 
		while({line.notNil}, {	
					time = (line.split($ )[1].asFloat) ;
					pitch = (line.split($ )[4].asFloat) ;
					if (pitch != 0, { arr = arr.add(time) ;
								   arr = arr.add(pitch)} ;
									 ) ;
					line = pitchLog.getLine ; 
							}) ;
		// arr.postln;
		pitchLog.close ;
		arr = if (toMidi == true, {
			arr = [arr.clump(2).flop[1].cpsmidi, arr.clump(2).flop[0]].flop.flat ;},
			{arr = [arr.clump(2).flop[1], arr.clump(2).flop[0]].flop.flat ;}) ;
		^arr	
	}
	
// couples freq/time: arr.clump(2)
// if freqs are needed: arr.clump(2).flop[0]
// if times are needed: arr.clump(2).flop[1]



	importFormantLog { arg formantLogFileName, toMidi = true ;
		var line, arr = [], lineArr ;
		var formantLog, time, formants, formant1, formant2,formant3  ;
		formantLogFileName = formantLogFileName ? 
							(String.scDir++"/sounds/praatTmp.formantLog") ;
		formantLogFileName = if ( formantLogFileName[0]==$/, { formantLogFileName }, 
									{ (String.scDir++"/"++formantLogFileName) }) ;
		formantLog = File.new(formantLogFileName, "r") ;
		line = formantLog.getLine ; 
		while({line.notNil}, {	
					lineArr = line.split($	) ; // white space is TAB
					time = (lineArr[0].asFloat) ;
					formant1 = (lineArr[2].asFloat) ;
					formant2 = (lineArr[3].asFloat) ;					formant3 = (lineArr[4].asFloat) ;					formants = [formant1, formant2, formant3] ;
					if (toMidi == true, { formants = formants.cpsmidi}) ;
					arr = arr.add(formants) ;
					arr = arr.add(time) ;
					line = formantLog.getLine ; 
							}) ;
		formantLog.close ;
		^arr	
	}



	importIntensity { arg intensityFileName ;
		var intensity ;
		var header = [], line, arr = [] ;
		intensityFileName = intensityFileName ? 
							(String.scDir++"/sounds/praatTmp.intensity") ;
		intensityFileName = if ( intensityFileName[0]==$/, { intensityFileName }, 
									{ String.scDir++"/"++intensityFileName }) ;
		
		intensity = File.new(intensityFileName, "r") ;			13.do({arg i; header = header.add(intensity.getLine); }) ; // eliminate header info
		header.postln ;
		line = intensity.getLine.asFloat ;
		while({line.notNil}, {	arr = arr.add(line.asFloat) ; 
							line = intensity.getLine ;
							}) ;
		arr.postln;
		intensity.close ;
			// now intensity becomes the right data structure
		^arr ;
		}


// output a pitch/duration selecting one of the formants
// can be notated via createDurationPitchNotation
	extractFormantFromFormantLog { arg formantArr, id = 0 ;
		var arr = [], newArr = [] ;
		var time, pitch ;
		arr = formantArr.clump(2) ;
		arr.do({ arg item, index ;
			pitch = item[0][id] ;
			time = item[1] ;
			newArr = newArr.add(pitch) ;
			newArr = newArr.add(time) ;
		}) ;
		^newArr
	}


	stretchDurations { arg arr, factor = 1 ;
		var pitches = arr.clump(2).flop[0] ;
		var durations = arr.clump(2).flop[1] ; 
		durations = durations * factor ;
		^[pitches, durations].flop.flat
	}

	transposePitches { arg arr, delta = 0 ;
		var pitches = arr.clump(2).flop[0] ;
		var durations = arr.clump(2).flop[1] ; 
		pitches = pitches + delta ; 
		^[pitches, durations].flop.flat
	}

	changePitchRange { arg arr, factor = 1 ;
		var pitches = arr.clump(2).flop[0] ;
		var durations = arr.clump(2).flop[1] ; 
		pitches = (pitches - pitches.minItem)*factor +  pitches.minItem ; 
		^[pitches, durations].flop.flat
	}


// for synthesis: it outputs [pitches, durations].
// You can pass it to ArrayPlayer
// OLD
	createPlayingArray { arg arr, samplingUnit = 0.25, quarterTone = true ;
		var round = if (quarterTone == true, {0.5}, {1}) ;
		var pitches = [0].addAll(arr.clump(2).flop[0].round(round)[0..((arr.size/2).asInt-2)]) ;
		var before = 0, newDurations = [];
		var durations = arr.clump(2).flop[1].round(samplingUnit) ; // semiquaver
		durations.do({arg i; 
		 			newDurations = newDurations.add(i-before) ;		 			before = i ;
		 			}) ;			
		newDurations = newDurations/samplingUnit ;
		^[pitches.midicps, newDurations]
	}


// call me AFTER createPitchDurationArray
// Just to avoid long pauses before playback
	removeStartRest { arg arr ;
		var pitches = arr[0] ;
		var durations = arr[1] ; 
		^[pitches[1..], durations[1..]]
	}


}

// what is [slot] ? Well, it works

AGArray[slot] : Array {

	

}
