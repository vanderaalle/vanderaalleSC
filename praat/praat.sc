/*
Generic class to interface with Praat.
  Allows to script Praat, sound analysis sw.

 Writes a tmp praat script file and a soundfile
 Calls Praat via shell passing a script
 Deletes tmp files

 About Praat:
 - http://www.praat.org/

 -> andrea valle, 16/01/07
 http://www.fonurgia.unito.it
*/


Praat {

// very generic interface to Praat:
// write tmp .aiff and .praat files
// delete it

	var <>script ;
	var <>sndFileName ;
	var <>mode ;
	var <>array ;

	var <>spectrogram, <>melFilter ;


	var <>formant ;
	var <>xmin, <>xmax, <>nx, <>dx, <>x1, maxnFormants ;
	var <>intensityList, <>freqList, <>bwList ; // 3 correlated lists for storing formant data
	var <>pitchList, <>pitchDict, <>pitchFilter, <>pitchKeys ;

	var <>pitchContour, <>intensity ;

/*
	*new { 	arg sndName, append = false ;
			^this.super.new.init(sndName, append);
	}
*/
	init { 	arg sndName, append = false;
			this.mode = if (append == false, {"w"}, {"a"}) ;
			sndName = sndName ? ("/tmp/praatTmp.aiff") ;
			sndName = if ( sndName[0]==$/, {sndName},
									{ "/tmp/"++sndName}) ;
			this.sndFileName = sndName ;
			this.writeSound(sndName) ;
			this.script = format("Read from file... %\n",				 sndFileName ) ;//.basename.splitext[0]) ;
			//String.scDir++"/sounds/praatTmp.aiff") ;


	}

	// could be useful per se
	writeSound { arg sndName ;
			var sig, soundFile ;
			sig = Signal.new ;
			this.array.do ({ arg item ;
						sig = sig.add(item) ;
						}) ;
			soundFile = SoundFile.new ;
			soundFile.headerFormat_("AIFF").sampleFormat_("int16").numChannels_(1) ;
			//soundFile.openWrite(String.scDir++"/sounds/praatTmp.aiff") ;
			soundFile.openWrite(sndName) ;
			soundFile.writeData(sig) ;
			soundFile.close ;
	}



	writeScript {  arg scriptPath ;
		var file ;
		scriptPath = scriptPath ? ("/tmp/praatTmp.praat") ;
		file = File.new("/tmp/praatTmp.praat", this.mode) ;
		script.postln;
		file.write(script) ;
		file.close ;
	}

	sendPraat { arg scriptPath, praatPath ;
			// new
			var p, l;
			// __
			scriptPath = scriptPath ? ("/tmp/praatTmp.praat") ;
			praatPath = praatPath ?  "/Applications/Praat.app/Contents/MacOS/Praat" ;
			//new
			p = Pipe.new(praatPath + scriptPath, "r") ;
			l = p.getLine;
			while({l.notNil}, {l.postln; l = p.getLine; }) ;
			p.close ;
			"done".postln
			// __
			//unixCmd(praatPath + scriptPath) ;
	}

	send {
		this.writeScript ;
		this.sendPraat ;
	}


	deleteTmp {
			//File.delete(this.sndFileName) ;
			//File.delete(String.scDir++"/sounds/praatTmp.praat") ;
			if ( File.exists(this.sndFileName), { File.delete(this.sndFileName)});
			if ( File.exists("/tmp/praatTmp.praat"),
							{ File.delete("/tmp/praatTmp.praat") }) ;
	}


//////////////////


	toSpectrogram { arg windowLength, maxFreq, timeStep, freqStep, windowShape ;

			// analysis
			windowLength = windowLength ? 0.05 ; // original: 0.005
			maxFreq = maxFreq ? 5000 ;			// original: 5000
			timeStep = timeStep ? 0.002 ;
			freqStep = freqStep ? 20 ;
			windowShape = "Hanning (sine-squared)" ; // original: "Gaussian"
			script = script+format("select Sound %\n", sndFileName.basename.splitext[0]) ;
			script = script+format("To Spectrogram... % % % % %\n", 				windowLength, maxFreq, 	timeStep, freqStep, windowShape) ;

	}


	paintSpectrogram { arg timeRange, freqRange, maxDB, autoscaling, dynamicRange,
						preEmphasis, dynamicCompression, garnish ;

			// painting
			timeRange = timeRange ? [0, 0] ; // 0, 0 means all the file
			freqRange = freqRange ? [0, 0] ; // 0, 0 means all the analyzed freq
			maxDB = maxDB ? 100 ;
			autoscaling = autoscaling ? "yes" ;
			dynamicRange = dynamicRange ? 50 ;
			preEmphasis = preEmphasis ? 6.0 ;
			garnish = garnish ? "yes" ;
			dynamicCompression = dynamicCompression ? 0.0 ;
			script = script+format("select Spectrogram % \n", sndFileName.basename.splitext[0]) ;
			script = script +format("Paint... % % % % % % % % % %\n",
					timeRange[0], timeRange[1], freqRange[0], freqRange[1], maxDB,
					autoscaling, dynamicRange,preEmphasis, dynamicCompression, garnish) ;

	}


	writeSpectrogram { arg  long, fileName ;
		var tag ;
		long = long ? false ;
		tag = if(long == false, {" short"}, {""}) ;
		fileName = fileName ? ("/tmp/praatTmp.spectrogram") ;
		fileName = if ( fileName[0]==$/, {fileName},
									{ "/tmp/"++fileName}) ;
		script = script+format("select Spectrogram % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("Write to% text file... %\n", tag, fileName) ;

	}





	writeEPS {  arg epsFileName;

				// defaults = Praat defaults
			epsFileName = epsFileName ? "/tmp/praatTmp.eps" ;
			epsFileName = if ( epsFileName[0]==$/, {epsFileName},
									{ String.scDir++"/"++epsFileName}) ;

			script = script+format("Write to EPS file... %\n", epsFileName)

			^nil ;
	}



	draw { arg timeRange, verticalRange, garnish, drawingMethod ;

			timeRange = timeRange ? [0,0] ;
			verticalRange = verticalRange ? [0,0] ;
			garnish = garnish ? "yes" ;
			drawingMethod = drawingMethod ? "curve" ;
			script = script+format("select Sound % \n", sndFileName.basename.splitext[0]) ;
			script = script +format("Draw... % % % % % %\n", timeRange[0], timeRange[1],
						verticalRange[0], verticalRange[1], garnish, drawingMethod) ;


	}


	toSpectrum { arg fast ;
		fast = fast ? "yes" ;
		script = script+format("select Sound % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("To Spectrum... %\n", fast) ;
	}



	drawSpectrum { arg log, frequencyRange, minimumPower, maximumPower, garnish ;
		var tag ;
		log = log ? false ;
		frequencyRange = if (log == true, {[10.0, 10000.0]}, {[0.0, 0.0]}) ;
		tag = if (log == true, {" (log freq)"}, {""}) ;
		minimumPower = minimumPower ? 0 ;
		maximumPower = maximumPower ? 0 ;
		garnish = garnish ? "yes" ;
		script = script+format("select Spectrum % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("Draw%... % % % % %\n", tag,
					frequencyRange[0],  frequencyRange[1],
					minimumPower, maximumPower, garnish) ;
	}



	writeSpectrum { arg  long, fileName ;
		var tag ;
		long = long ? false ;
		tag = if(long == false, {" short"}, {""}) ;
		fileName = fileName ? (String.scDir++"/sounds/praatTmp.spectrum") ;
		fileName = if ( fileName[0]==$/, {fileName},
									{ String.scDir++"/sounds/"++fileName}) ;
		script = script+format("select Spectrum % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("Write to% text file... %\n", tag, fileName) ;

	}


	/////
	toMelFilter { arg analysisWindowDuration, timeStep,
			positionOfFirst, distanceBetween, maximumFrequency ;
		analysisWindowDuration = analysisWindowDuration ? 0.015 ;
		timeStep = timeStep ? 0.005 ;
		positionOfFirst = positionOfFirst ? 100.0 ;
		distanceBetween = distanceBetween ? 100.0 ;
		maximumFrequency = maximumFrequency ? 0.0 ;
		script = script+format("select Sound % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("To MelFilter... % % % % %\n",
			analysisWindowDuration, timeStep,
			positionOfFirst, distanceBetween, maximumFrequency ) ;
	}



	paintMelFilter { arg type, timeRange, frequencyRange, amplitudeRange ;
		/*
			type: image, contours, cells, surface
		*/
		type = type ? "cells" ;
		timeRange = timeRange ? [0.0, 0.0] ;
		frequencyRange = frequencyRange ? [0.0, 0.0] ;
		amplitudeRange = amplitudeRange ? [0.0, 0.0] ;
		script = script+format("select MelFilter % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("Paint %... % % % % % %\n", type,
					timeRange[0], timeRange[1],
					frequencyRange[0],  frequencyRange[1],
					amplitudeRange[0],  amplitudeRange[1]) ;
	}


	drawMelFilter { arg type, timeRange, frequencyRange, amplitudeRange ;
		/*
			type: "filters", "contours", "one contour"
		*/
		type = type ? "contours" ;
		timeRange = timeRange ? [0.0, 0.0] ;
		frequencyRange = frequencyRange ? [0.0, 0.0] ;
		amplitudeRange = amplitudeRange ? [0.0, 0.0] ;
		script = script+format("select MelFilter % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("Draw %... % % % % % %\n", type,
					timeRange[0], timeRange[1],
					frequencyRange[0],  frequencyRange[1],
					amplitudeRange[0],  amplitudeRange[1]) ;
	}


	drawMelFilterSpectrum { arg time, frequencyRange, amplitudeRange ;

		time = time ? 0.1 ;
		frequencyRange = frequencyRange ? [0.0, 0.0] ;
		amplitudeRange = amplitudeRange ? [0.0, 0.0] ;
		script = script+format("select MelFilter % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("Draw spectrum (slice)... % % % % % %\n",
					time, frequencyRange[0],  frequencyRange[1],
					amplitudeRange[0],  amplitudeRange[1]) ;
	}






	writeMelFilter { arg  long, fileName ;
		var tag ;
		long = long ? false ;
		tag = if(long == false, {" short"}, {""}) ;
		fileName = fileName ? (String.scDir++"/sounds/praatTmp.melFilter") ;
		fileName = if ( fileName[0]==$/, {fileName},
									{ String.scDir++"/sounds/"++fileName}) ;
		script = script+format("select MelFilter % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("Write to% text file... %\n", tag, fileName) ;

	}
	///

	toFormant { arg timeStep, maxFormantNumber, maxFormantFreq, windowLength, preEmphasis ;

		timeStep = timeStep ? 0.0 ;
		maxFormantNumber = maxFormantNumber ? 5 ;
		maxFormantFreq = maxFormantFreq ? 5500 ; //adult female
		windowLength = windowLength ? 0.025 ;
		preEmphasis = preEmphasis ? 50 ;
		script = script+format("select Sound % \n", sndFileName.basename.splitext[0]) ;
		script = script +format("To Formant (burg)... % % % % %\n",
					timeStep, maxFormantNumber, maxFormantFreq, windowLength, preEmphasis) ;

	}


	drawFormantSpeckle { arg timeRange, maxFrequency, dynamicRange, garnish ;

		timeRange = timeRange ? [0.0, 0.0] ;
		maxFrequency = maxFrequency ? 5500 ;
		dynamicRange = dynamicRange ? 30 ;
		garnish = garnish ? "yes" ;
		script = script+format("select Formant % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("Speckle... % % % % % %\n",
					timeRange[0], timeRange[1], maxFrequency,
					dynamicRange,  garnish) ;
	}


	drawFormantTracks { arg timeRange, maxFrequency, garnish ;

		timeRange = timeRange ? [0.0, 0.0] ;
		maxFrequency = maxFrequency ? 5500 ;
		garnish = garnish ? "yes" ;
		script = script+format("select Formant % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("Draw tracks... % % % % %\n",
					timeRange[0], timeRange[1], maxFrequency, garnish) ;
	}

	writeFormant { arg long, fileName ;
		var tag ;
		long = long ? false ;
		tag = if(long == false, {" short"}, {""}) ;
		fileName = fileName ? (String.scDir++"/sounds/praatTmp.formant") ;
		fileName = if ( fileName[0]==$/, {fileName},
									{ String.scDir++"/sounds/"++fileName}) ;
		script = script+format("select Formant % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("Write to% text file... %\n", tag, fileName) ;

	}





	toIntensity {	arg minimumPitch, timeStep, subtractMean ;

		minimumPitch = minimumPitch ? 100 ;
		timeStep = timeStep ? 0 ;
		subtractMean = subtractMean ? "yes" ;
		script = script+format("select Sound % \n", sndFileName.basename.splitext[0]) ;
		script = script +format("To Intensity... % % % \n",
					minimumPitch, timeStep, subtractMean) ;

	}



	drawIntensity { arg timeRange, minimum, maximum, garnish ;

		timeRange = timeRange ? [0,0] ;
		minimum = minimum ? 0.0 ;
		maximum = maximum ? 0.0 ;
		garnish = garnish ? "yes" ;
		script = script+format("select Intensity % \n", sndFileName.basename.splitext[0]) ;
		script = script +format("Draw... % % % % % \n", timeRange[0], timeRange[1],
					minimum, maximum, garnish) ;


	}


	writeIntensity { arg long, fileName ;
		var tag ;
		long = long ? false ;
		tag = if(long == false, {" short"}, {""}) ;
		fileName = fileName ? (String.scDir++"/sounds/praatTmp.formant") ;
		fileName = if ( fileName[0]==$/, {fileName},
									{ String.scDir++"/sounds/"++fileName}) ;
		script = script+format("select Intensity % \n", sndFileName.basename.splitext[0]) ;
		script = script + format("Write to% text file... %\n", tag, fileName) ;

	}



	toPitch { arg timeStep, pitchFloor, pitchCeiling ;

		timeStep = timeStep ? 0 ;
		pitchFloor = pitchFloor ? 75.0 ;
		pitchCeiling = pitchCeiling ? 500.0 ;
		script = script+format("select Sound % \n", sndFileName.basename.splitext[0]) ;
		script = script +format("To Pitch... % % % \n",
				timeStep, pitchFloor, pitchCeiling) ;
		// doesn't avoid strange jumps
		// script = script + "Kill octave jumps\n"

	}


	drawPitch { arg log, timeRange, frequencyRange, garnish ;

		var tag ;
		log = log ? false ;
		frequencyRange = if (log == true, {[20, 500.0]}, {[0.0, 500.0]}) ;
		tag = if (log == true, {" logarithmic"}, {""}) ;
		timeRange = timeRange ? [0,0] ;
		garnish = garnish ? "yes" ;
		script = script+format("select Pitch % \n", sndFileName.basename.splitext[0]) ;
		script = script +format("Draw%... % % % % % \n",
			 tag, timeRange[0], timeRange[1], frequencyRange[0], frequencyRange[1], garnish) ;
	}



// this one is special because usual export file format is too complex
// Thus we export a 1D matrix (i.e. a time-series)
	writePitch { arg long, fileName ;
		var tag ;
		long = long ? false ;
		tag = if(long == false, {" short"}, {""}) ;
		fileName = fileName ? (String.scDir++"/sounds/praatTmp.pitchContour") ;
		fileName = if ( fileName[0]==$/, {fileName},
									{ String.scDir++"/sounds/"++fileName}) ;
		script = script+format("select Pitch % \n", sndFileName.basename.splitext[0]) ;
		script = 	script + "To Matrix\n" ;
		script = script + format("Write to% text file... %\n", tag, fileName) ;
	}

// utilities

	open { arg epsFileName, openApp ;
		openApp = openApp ? "/Applications/Preview.app/Contents/MacOS/Preview" ;
		epsFileName = epsFileName ? (String.scDir++"/sounds/praatTmp.eps") ;
		epsFileName = if ( epsFileName[0]==$/, {epsFileName},
								{ String.scDir++"/"++epsFileName}) ;			unixCmd(openApp+epsFileName) ;

	}


	selectViewport { arg fromX, toX, fromY, toY ;
			fromX = fromX ? 0 ;
			toX = toX ? 6 ;
			fromY = fromY ? 0 ;
			toY = toY ? 4 ;
			script = script +format("Select outer viewport... % % % %\n", fromX, toX, fromY, toY)

	}



	lineWidth { arg width ;
		width = width ? 1.0 ;
		script = script +format("Line width... %\n", width) ;
		}

	line { arg style;
		style = style ? "Plain" ; // --> "Dashed", "Dotted"
		script = script +format("% line\n", style) ;
	}

	color { arg color ;
		/*
		Black, White, Red, Green, Blue, Yellow,
		Cyan, Magenta, Maroon, Lime, Navy, Teal,
		Purple, Olive, Silver, Grei
		*/
		color = color ? "Black" ;
		script = script + (color++"\n");
	}

	markEvery { arg where, units, distance,
				writeNumbers, drawTicks, drawDottedLines ;

			where = where ? "left" ;
			units = units ? 1.0 ;
			distance = distance ? 0.1 ;
			writeNumbers = writeNumbers ? "yes" ;
			drawTicks = drawTicks ? "yes" ;
			drawDottedLines = drawDottedLines ? "yes" ;
			script = script + format("Marks % every... % % % % %\n",
						where, units, distance,
						writeNumbers, drawTicks, drawDottedLines
						) ;
	}

	oneMark { arg position, writeNumber, drawTick, drawDottedLine,
				drawText ;

			position = position ? "left" ;
			writeNumber = writeNumber ? "yes" ;
			drawTick = drawTick ? "yes" ;
			drawDottedLine = drawDottedLine ? "yes" ;
			drawText = drawText ? "";
			script = script + format("One mark %... % % % % %\n",
						position,
						writeNumber, drawTick, drawDottedLine,
						drawText ) ;
	}



// Still can't understand
/*
	toPdf {	arg epsFileName, a ;
			var p, l ;
			epsFileName = epsFileName ? String.scDir++"/sounds/praatTmp.eps" ;
			epsFileName = if ( epsFileName[0]==$/, {epsFileName},
									{ String.scDir++"/"++epsFileName}) ;

			/*
			p = Pipe.new("epstopdf" + epsFileName, "r") ;
			l = p.getLine ;
			while({l.notNil}, {l.postln; l = p.getLine; }) ;
			p.close ;
			"suca".postln ;
			*/
			epsFileName.postln ;
			a = unixCmd("epstopdf"+epsFileName) ;
			// a.postln;

	}


	open { arg pdfFileName, openApp ;
			openApp = openApp ? "/Applications/Preview.app/Contents/MacOS/Preview" ;
			pdfFileName = pdfFileName ? String.scDir++"/sounds/praatTmp.pdf" ;
			pdfFileName = if ( pdfFileName[0]==$/, {pdfFileName},
									{ String.scDir++"/"++pdfFileName}) ;			unixCmd(openApp+pdfFileName) ;
	}

*/



/*

Convenience methods

*/

	createSpectrogram { arg epsFileName, windowLength, maxFreq, timeStep, freqStep, windowShape,
						timeRange, freqRange, maxDB, autoscaling, dynamicRange,
						preEmphasis, dynamicCompression, garnish,
						fromX, toX, fromY, toY,
						open = true ;

				this.toSpectrogram(windowLength, maxFreq, timeStep, freqStep, windowShape) ;
				this.selectViewport(fromX, toX, fromY, toY) ;
				this.paintSpectrogram(timeRange, freqRange, maxDB, autoscaling, dynamicRange,
						preEmphasis, dynamicCompression, garnish) ;
				this.writeEPS ;
				this.send ;
				//this.toPdf ;
				this.deleteTmp ; // we have to wait Praat using the files
				// if (open == true, {this.open(epsFileName)}); // Too early

		}



	exportSpectrogram {  arg windowLength, maxFreq, timeStep, freqStep, windowShape,
						long, fileName ;
				this.toSpectrogram(windowLength, maxFreq, timeStep, freqStep, windowShape) ;
				this.writeSpectrogram(long, fileName) ;
				this.writeScript ;
				this.send ;
	}




	exportSpectrum {   arg fast, long, fileName ;
			this.toMelFilter(fast) ;
			this.writeSpectrum(long, fileName) ;
			this.writeScript ;
			this.send ;
	}


	exportMelFilter {   arg analysisWindowDuration, timeStep,
			positionOfFirst, distanceBetween, maximumFrequency,
					long, fileName ;
			this.toMelFilter(analysisWindowDuration, timeStep,
					positionOfFirst, distanceBetween, maximumFrequency) ;
			this.writeMelFilter(long, fileName) ;
			this.writeScript ;
			this.send ;
	}

	exportFormant { arg timeStep, maxFormantNumber, maxFormantFreq, windowLength, preEmphasis,
				long, fileName ;
			this.toFormant(timeStep, maxFormantNumber, maxFormantFreq,
					windowLength, preEmphasis) ;
			this.writeFormant(long, fileName) ;
			this.writeScript ;
			this.send;
	}


	exportPitch { arg timeStep, pitchFloor, pitchCeiling,
				long, fileName, tag ;
		this.toPitch(timeStep, pitchFloor, pitchCeiling) ;
		this.writePitch(long, fileName) ;
		this.writeScript ;
		this.send;
	}


	exportIntensity { arg minimumPitch, timeStep, subtractMean,				long, fileName, tag ;
		this.toIntensity (minimumPitch, timeStep, subtractMean) ;
		this.writeIntensity(long, fileName) ;
		this.writeScript ;
		this.send;
	}




// BUG: writeEPS: we need to specify  the epsFileName, otherwise is praatTmp

	createFormant { arg timeStep, maxFormantNumber, maxFormantFreq, windowLength, preEmphasis,
				timeRange, maxFrequency, dynamicRange, garnish,
				long, fileName ;
		this.toFormant(timeStep, maxFormantNumber, maxFormantFreq,
					windowLength, preEmphasis) ;
		this.drawFormantSpeckle(timeRange, maxFrequency, dynamicRange, garnish) ;
		this.writeEPS ;
		this.send ;
	}


	createPlot { arg epsFileName,  timeRange, verticalRange, garnish, drawingMethod,
						fromX, toX, fromY, toY ;

		this.selectViewport(fromX, toX, fromY, toY) ;
		this.draw(timeRange, verticalRange, garnish, drawingMethod) ;
		this.writeEPS ;
		this.send ;
		this.deleteTmp ; // we have to wait Praat using the files
		}


	createIntensity { arg epsFileName,  mimimumPitch, timeStep, subtractMean,
			timeRange, minimum, maximum, garnish,
						fromX, toX, fromY, toY ;

				this.toIntensity(mimimumPitch, timeStep, subtractMean) ;
				this.selectViewport(fromX, toX, fromY, toY) ;
				this.drawIntensity(timeRange, minimum, maximum, garnish) ;
				this.writeEPS ;
				this.send ;
				this.deleteTmp ; // we have to wait Praat using the files
		}


	createSpectrum { arg fast, log, frequencyRange, minimumPower, maximumPower, garnish ;
			this.toSpectrum(fast) ;
			this.drawSpectrum(log, frequencyRange, minimumPower, maximumPower, garnish) ;
			this.writeEPS ;
			this.send ;
			this.deleteTmp ; // we have to wait Praat using the files

	}

	createMelFilter { arg analysisWindowDuration, timeStep,
				positionOfFirst, distanceBetween, maximumFrequency,
				type, timeRange, frequencyRange, amplitudeRange ;
			this.toMelFilter(analysisWindowDuration, timeStep,
				positionOfFirst, distanceBetween, maximumFrequency) ;
			this.paintMelFilter(type, timeRange, frequencyRange, amplitudeRange) ;
			this.writeEPS ;
			this.send ;
	}


	createPitch { arg epsFileName, timeStep, pitchFloor, pitchCeiling,
						log, timeRange, frequencyRange, garnish,
						fromX, toX, fromY, toY ;

				this.toPitch(timeStep, pitchFloor, pitchCeiling) ;
				this.selectViewport(fromX, toX, fromY, toY) ;
				this.drawPitch(log, timeRange, frequencyRange, garnish) ;
				this.writeEPS(epsFileName) ;
				this.send ;
				this.deleteTmp ; // we have to wait Praat using the files
		}


	createAll { // quick&dirty with defaults
		this.draw ;
		this.toSpectrogram ;
		this.selectViewport(6, 12, 0, 4) ;
		this.paintSpectrogram ;
		this.toIntensity ;
		this.selectViewport(0, 6, 4, 8) ;
		this.drawIntensity ;
		this.toPitch ;
		this.selectViewport(6, 12, 4, 8) ;
		this.drawPitch(log:true);
		this.selectViewport(0, 12, 0, 8) ;
		this.writeEPS ;
		this.send ;


	}



	quickView { // quick&dirty with defaults
		this.draw ;
		this.toSpectrogram ;
		this.selectViewport(0, 6, 4, 8) ;
		this.paintSpectrogram ;
		this.selectViewport(0, 6, 0, 8) ;
		this.writeEPS ;
		this.send ;


	}


/*

View and conversion: EXPERIMENTAL

*/

	importSpectrogram { arg spectrogramFileName ;
		var header = [], line, arr ;
		spectrogramFileName = spectrogramFileName ? (String.scDir++"/sounds/praatTmp.spectrogram") ;
		spectrogramFileName = if ( spectrogramFileName[0]==$/, { spectrogramFileName },
									{ String.scDir++"/"++spectrogramFileName }) ;

		spectrogram = File.new(spectrogramFileName, "r") ;		12.do({arg i; header = header.add(spectrogram.getLine); }) ; // eliminate header info
		header.postln ;
		line = spectrogram.getLine.asFloat ;
		arr = [line] ;
		while({line.notNil}, {arr = arr.add(line.asFloat); line = spectrogram.getLine; }) ;
		spectrogram.close ;
	// now spectrogram becomes the right data structure
		spectrogram = arr.clump(header[5].asInteger).flop ;

		}


// the same...

	importMelFilter { arg melFilterFileName ;
		var header = [], line, arr ;
		melFilterFileName = melFilterFileName ? (String.scDir++"/sounds/praatTmp.melFilter") ;
		melFilterFileName = if ( melFilterFileName[0]==$/, { melFilterFileName },
									{ String.scDir++"/"++melFilterFileName }) ;

		melFilter = File.new(melFilterFileName, "r") ;			13.do({arg i; header = header.add(melFilter.getLine); }) ; // eliminate header info
		header.postln ;
		line = melFilter.getLine.asFloat ;
		arr = [line] ;
		while({line.notNil}, {arr = arr.add(line.asFloat); line = melFilter.getLine; }) ;
		melFilter.close ;
	// now melFilter becomes the right data structure
		melFilter = arr.clump(header[5].asInteger).flop ;

		}

	importFormant { arg formantFileName ;
		var header = [], line, arr = [] ;
		var intensity, nF, couples, i ;
		var frameFreq = [], frameBw = [] ;
		formantFileName = formantFileName ? (String.scDir++"/sounds/praatTmp.formant") ;
		formantFileName = if ( formantFileName[0]==$/, { formantFileName },
									{ String.scDir++"/"++formantFileName }) ;

		formant = File.new(formantFileName, "r") ;			9.do({arg i; header = header.add(formant.getLine); }) ; // eliminate header info
		header.postln ;
		# xmin, xmax, nx, dx, x1, maxnFormants = header[3..].asFloat ;
		line = formant.getLine.asFloat ;
		//arr = [line] ;
		while({line.notNil}, {arr = arr.add(line.asFloat); line = formant.getLine; }) ;
		arr.postln;
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
		}



/*
		script = 	script + "
myPitch = selected(\"Pitch\")
time_step = Get time step
start = Get time from frame number... 1
nFrames = Get number of frames

Create simple Matrix... time 1 nFrames start+(x-1)*time_step
myTime = selected(\"Matrix\")
select myPitch
To Matrix
plus myTime
Merge (append rows)
Transpose\n" ;

*/


	importPitch { arg pitchContourFileName ;
		var header = [], line, arr = [] ;
		pitchContourFileName = pitchContourFileName ?
							(String.scDir++"/sounds/praatTmp.pitchContour") ;
		pitchContourFileName = if ( pitchContourFileName[0]==$/, { pitchContourFileName },
									{ String.scDir++"/"++pitchContourFileName }) ;

		pitchContour = File.new(pitchContourFileName, "r") ;			14.do({arg i; header = header.add(pitchContour.getLine); }) ; // eliminate header info
		header.postln ;
		line = pitchContour.getLine.asFloat ;
		while({line.notNil}, {	arr = arr.add(line.asFloat) ;
							line = pitchContour.getLine ;
							}) ;
		arr.postln;
		pitchContour.close ;
			// now pitchContour becomes the right data structure
		pitchContour = arr ;
		}

	importIntensity { arg intensityFileName ;
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
		intensity = arr ;
		}



// importing Pitch contour is complicated because you have all candidates and other stuff
// better import the interpolated contour accessible via pitch listing
/*
// it works, but you must export manually

	importPitchContour { arg pitchListFileName ;
		var line, pitchList ;
		pitchListFileName = pitchListFileName ? String.scDir++"/sounds/praatTmp.pitchContour" ;
		pitchListFileName = if ( pitchListFileName[0]==$/, { pitchListFileName },
									{ String.scDir++"/"++pitchListFileName }) ;

		pitchList = File.new(pitchListFileName, "r") ;			line = pitchList.getLine ; // header
		//arr = [line] ;
		while({line.notNil}, {	pitchContour = pitchContour.add(
									line.split($ ).reverse[0].asFloat
										) ;
							line = pitchList.getLine; }) ;
		// Note that "--undefined--".asFloat returns 0
		pitchContour.postln;
		pitchList.close ;
	}

*/

	jSpectrogram { arg stepX, stepY ;
		//var mn = t.flat.sort.reverse[0].ampdb, mx = t.flat.sort.reverse[0].ampdb, amp = (mn-mx).abs;
		var index, window ;
		stepX = stepX ? 3 ;
		stepY = stepY ? stepX ;
		// not that fast ...
		window = JSCWindow( "Spectrogram",
			Rect(100,100, spectrogram.size*stepX, spectrogram[0].size*stepY), resizable: false
					);
		window.view.background = Color.white;
		window.drawHook = {
			spectrogram.do({ arg y, n ;
				y.reverse.do({ arg item, m ;
				//var itm = (item.ampdb+mn)/amp ;
				//JPen.color = Color.new(1.0-itm, 1.0-itm, 1.0-itm);
				JPen.color = Color.new(1.0-item, 1.0-item, 1.0-item);
				JPen.fillRect( Rect( n*stepX, m*stepY, stepX, stepY ));
					});
				});
			};
			window.front;
		}




	jMelFilter { arg stepX, stepY, minAmp, maxAmp, amp ;
		var index, window ;
		//var newArr = melFilter/melFilter.flat.sort.reverse[0] ;
		var newArr ;
		stepX = stepX ? 3 ;
		stepY = stepY ? stepX ;
		minAmp = minAmp ? 0.0 ;
		maxAmp = maxAmp ? 1.0 ;
		amp = amp ? 0.0 ;
		newArr = melFilter.flat.normalize.clip(minAmp, maxAmp)
				.clump(melFilter[0].size) + amp ;
		window = JSCWindow( "MelFilter",
			Rect(100,100, melFilter.size*stepX, melFilter[0].size*stepY), resizable: false
					);
		window.view.background = Color.white;
		window.drawHook = {
			newArr.do({ arg y, n ; // using normalized
				y.reverse.do({ arg item, m ;
				//var itm = if( item > threshold, {item + amp }, {0.0}) ; // "limiter"
				// var itm = item - threshold ;
				//JPen.color = Color.new(1.0-itm, 1.0-itm, 1.0-itm);
				JPen.color = Color.new(1.0-item, 1.0-item, 1.0-item);
				JPen.fillRect( Rect( n*stepX, m*stepY, stepX, stepY ));
					});
				});
			};
			window.front;
		}



	jFormant { arg stepX, stepY, formantList ;
		var index, window ;
		var maxFreq = freqList.flat.sort.reverse[0];
		var newArr = freqList/maxFreq*100; // (0-100)
		var chosen = [] ;
		stepX = stepX ? 3 ;
		stepY = stepY ? stepX ;
		formantList = formantList ? nil ;
		window = JSCWindow( "Formant",
			Rect(100,100, freqList.size*stepX, stepY*100), resizable: false
					);
		window.view.background = Color.white;
		if (formantList == nil, {
		window.drawHook = {
			newArr.do({ arg y, n ; // using normalized
				y.do({ arg item, m ;
					JPen.color = Color.new(0.0, 0.0, 0.0);
					JPen.fillRect( Rect( n*stepX, stepY*(100-item), stepX, stepY ));
					});
				});
			};
		},
		{
		window.drawHook = {
			newArr.do({ arg y, n ; // using normalized
				chosen = [] ;
				formantList.do({ arg f ; if (y[f] != nil, {chosen = chosen.add(y[f])})});
				chosen.postln;
				chosen.do({ arg item, m ;
					JPen.color = Color.new(0.0, 0.0, 0.0);
					JPen.fillRect( Rect( n*stepX, stepY*(100-item), stepX, stepY ));
				});
			})

		};
		});
		window.front;
		}

	jPitchContour { arg stepX, stepY, maxHz ;
		var index, window ;
		var newArr ;
		var chosen = [] ;
		stepX = stepX ? 3 ;
		stepY = stepY ? stepX ;
		maxHz = maxHz ? 2000 ;
		newArr = pitchContour*100/maxHz; // (0-100)
		window = JSCWindow( format("Pitch (0-% Hz)", maxHz),
			Rect(100,100, pitchContour.size*stepX, stepY*100), resizable: false
					);
		window.view.background = Color.white;
		window.drawHook = {
			newArr.do({ arg y, n ; // using normalized
					JPen.color = Color.new(0.0, 0.0, 0.0);
					JPen.fillRect( Rect( n*stepX, stepY*(100-y), stepX, stepY ));
					});
			};
		window.front;
		}


}

