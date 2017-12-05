DbSynth {
	classvar <>bufDict ;
	classvar <>normalize ;

	*initClass {
		bufDict = () ;
		normalize = true ;
	}

	//*dir { arg path; root = path }

	*new { arg key, defName, args, target, addAction =\addToHead ;
		^super.new.initDbSynth(key, defName, args, target, addAction)
	}

	*clean { bufDict = () }
	*freeAll { Buffer.freeAll }
	*reset { bufDict = (); Buffer.freeAll }

	// so you have an idea about what is in use
	*names {
		^bufDict.keys.collect{|i| i.asString.split($_)[0]}.asArray
	}

	initDbSynth { arg key, defName, args, target, addAction;
		var buf ;
		if (
			// it has a key with the name, thus a buffer
			bufDict.keys.includes(key.fileName.asSymbol)){
			// SynthDef must have buf arg
			^Synth(defName, args++[\buf, bufDict[key.fileName.asSymbol]], target, addAction)
		}{
			// load buffer and associate it to name as a key
			"Loading NEW BUFFER: ".post; key.path.postln ;
			// should be done once
			buf = if (normalize){
				Buffer.read(Server.local,
				key.path.dirname++"/"++key.name++"/"++key.path.basename).normalize

			}{
				Buffer.read(Server.local,
				key.path.dirname++"/"++key.name++"/"++key.path.basename)
			} ;

			bufDict[key.fileName.asSymbol] = buf ;
		}
	}



}

/*

SynthDef(\pl, {|buf, transp = 0, mul = 1, pan = -1, out = 0|
	Out.ar(out, Pan2.ar(PlayBuf.ar(1, buf,rate:transp.midiratio, doneAction:2), pan)
	*mul)
}).add ;

~root = "/Users/andrea/musica/recordings/philarmonia/" ;
~tuba = Object.readArchive(~root++"tuba"++".db") ;
~which = ~tuba.select{|e| e[\midi] == 34 }[0]; // ok
DbSynth(~which, \pl) ;

~which = ~tuba.select{|e| e[\midi] == 35 }[0]; // ok
DbSynth(~which, \pl) ;


~which = ~tuba.select{|e| e[\midi] == 38 }[0]; // ok
DbSynth(~which, \pl) ;

DbSynth.reset

DbSynth.bufDict[~which.fileName.asSymbol]
*/
