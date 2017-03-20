// HarmoEvent encapsulate analysis of bin differences
// and automated splitting and export of file segments

HarmoEvent {

	var <>amp ;

	// start with an array of amps
	*newFrom { arg amp ;
		^super.new.initHarmoSpectrum(amp)
	}

	// you just pass an amp array from SonaGraph
	initHarmoSpectrum { arg anAmp ;
		amp = anAmp ;
	}

	// calculates the difference in dB between
	// a bin and the previous one
	// returns an amp-like struct with only diffs
	differentiate {
		^amp[1..].collect{|slice,i|
			slice.collect{|fr,j|
				fr-amp[i][j]
			}
		}
	}

	// average each bin, as an average diff
	// returns an amp.size single value array
	averageBin {
		^this.differentiate.collect{|bin| bin.sum/88}
	}

	// retrieve difference > thresh between bins
	// returns an array of [diff, index]
	// to keep track of time points
	getDiffPoints {|thresh = 5|
		var diffP = [] ;
		this.averageBin.abs.do{|i,j|
			if(i>thresh){diffP = diffP.add([i, j])}
		} ;
		^diffP
	}

	// this works on attacks, i.e. discards dB
	filterNear {|att, binDiff = 3|
		var filtered = [] ;
		var seq = Pseq(att).asStream ;
		var which = seq.next ;
		var next = seq.next;
		while {next.notNil}{
			if(next-which <= binDiff){
				next = seq.next
			}{
				filtered = filtered.add(which) ;
				which = next ;
				next = seq.next
			}
		} ;
		^filtered
	}

	// general wrapper
	getEvents {|thresh = 4, binDiff = 3|
		// we collect only attacks
		var att = this.getDiffPoints(thresh).collect{|i| i[1]} ;
		^this.filterNear(att, binDiff)
	}

	// PRIVATE: apply a curve env (start/end) to a floatArr
	// representing a signal, used to split nicely
	// used by splitIntoFiles
	applyEnv {|array, startFrame, stopFrame, attD = 0.005, relD = 0.03|
		var sus ;
		attD = (attD*Server.local.sampleRate).trunc.asInteger ;
		relD = (relD*Server.local.sampleRate).trunc.asInteger ;
		sus = stopFrame-startFrame-attD-relD ;
		// a la Reaper
		^Env.new([0,1,1,0], [attD, sus, relD], [-3,\linear,3])
		.asSignal(stopFrame-startFrame).collect{|i| i}
		*
		array[startFrame..stopFrame]
	}

	// cascade calls to get filtered events, and file export
	splitIntoFiles {| thresh = 4, binDiffSec = 0.15,
		srcPath, splitDir, anRate,
		attD = 0.005, relD = 0.03|
		var snd = SoundFile.openRead(srcPath) ;
		var arr = FloatArray.newClear(snd.numFrames) ;
		var period = (anRate.reciprocal * Server.local.sampleRate).asInteger ;
		var att = this.getEvents(thresh, binDiff:binDiffSec/anRate.reciprocal) ;
		snd.readData(arr) ; // read into arr from file
		att[..att.size-2].do{|which,i|
			var f = SoundFile.new.headerFormat_("AIFF")
			.sampleFormat_("float").numChannels_(1) ;
			var data = this.applyEnv(arr,
				which*period,
				att[i+1]*period,
				attD, relD).as(FloatArray) ;
			f.openWrite(splitDir++i++".aiff") ;
			f.writeData(data);
			f.close ;
		}
	}

}

/*

SonaGraph.prepare ;

~path ="/Users/andrea/musica/regna/fossilia/compMine/erelerichnia/fragm/octandreExc2.aif"; ~sample = Buffer.read(s, ~path).normalize ;

// an istance
a = SonaGraph.new;
// now analyzing in real-time
a.analyze(~sample,20) ; // high rate!

a.att = HarmoEvent.newFrom(a.amp).getEvents(4, binDiff:0.15/a.anRate.reciprocal) ;
a.gui(~sample, 5)

// one shot to get files
HarmoEvent.newFrom(a.amp).splitIntoFiles(4, 0.15, ~path, "/Users/andrea/musica/regna/fossilia/compMine/erelerichnia/testSplit/", a.anRate)

*/