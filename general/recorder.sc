/*

A minimal bus recorder, mono

// a bus
~recBus = Bus.audio(Server.local) ;
r = Recorder(~recBus, "/recTest2.aiff")

// start something to record
x = {Out.ar(~recBus, SinOsc.ar(800))}.play

r.rec

r.stop

x.free

*/
/*
Recorder {

	var <>recBus, <>path, <>headerFormat, <>sampleFormat, <>recBuffer;
	var <>recNode ;
	var <>window ;

	*new { arg recBus, path, headerFormat = "aiff", sampleFormat = "float" ;
		^super.new.initRecorder(recBus, path, headerFormat, sampleFormat )
	}

	initRecorder { arg recBus_, path_, headerFormat_, sampleFormat_ ;
		recBus = recBus_ ;
		path = path_ ;
		headerFormat = headerFormat_ ;
		sampleFormat = sampleFormat_ ;

		Server.local.waitForBoot{
			{
				SynthDef(\recorder, {arg buffer, bus;
� � � � 			DiskOut.ar(buffer, In.ar(bus, 1));
			}).add;
			Server.local.sync ;
			"Ready to record".postln ;
			}.fork ;
		}

	}


	rec {
		{
			// allocate a disk i/o buffer
			recBuffer = Buffer.alloc(Server.local, 2**16, 1);
			Server.local.sync ;
			// create an output file for this buffer, leave it open
			recBuffer.write(path, headerFormat, sampleFormat, 0, 0, true);
			"Now Recording".postln ;
			// create the diskout node; making sure it comes after the source
			recNode = Synth.tail(nil,\recorder, [\buffer, recBuffer, \bus, recBus]);
		}.fork(AppClock)
		}

	stop {
		// stop recording
		recNode.free;
		// close the buffer and the soundfile, and then free
		recBuffer.close{recBuffer.free; "Recording stopped".postln};
	}

	gui {


	}


}

*/