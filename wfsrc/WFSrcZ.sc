WFSrcZ {
	var <>id ;
	var synth, <>bus, vol ;
	var xyz ; // an array, not normalized

	*new { |id, x = 0, y = 0, z = 0|
		^super.new.initWFSrc(id, x, y, z)
	}

	initWFSrc { |anId,anX, anY, aZ|
		id = anId;
		this.xyz_(anX,anY,aZ);
		Server.local.waitForBoot{
			SynthDef(\wfsrc, {|in, out, amp = 1|
				Out.ar(out, In.ar(in)*amp)
			}).add ;
			bus = Bus.audio(Server.local, 1) ;
			Server.local.sync ;
			synth = Synth(\wfsrc, [\in, bus, \out, id])
		}
	}

	xyz_ {|newX,newY,newZ| xyz = [newX, newY,newZ];
		this.changed(\xyz, [id, xyz])
	}
	xyz {^xyz}

	// synth interface
	synth {^synth}
	synth_{|aSynth| synth = aSynth;
		//we get vol to store it
		synth.get(\amp, {|v| vol = v.ampdb})
	}

	route {|out| synth.set(\out, out) }

	vol_ {|aVol| vol = aVol ; // in dB
		synth.set(\amp, vol.dbamp)
	}
	vol {^vol}

	pause {synth.run(false)}
	play {synth.run}

	mute {
		synth.get(\amp, {|v|
			vol = v.ampdb;
			synth.set(\amp, 0) }) ;
	}
	unmute { synth.set(\amp, vol.dbamp) }
}

WFSrcDispatcherZ {
	// this:
	// 1. map to virtual dimension
	// 2. converts to radians
	// 3. dispatches to IP

	var <>wFSrcs ;
	var <>address ;
	var <>meterFactor ;

	*new { |aWFSrcArr, ip = "192.168.1.1", port = 4243, meterFactor = 1|
		^super.new.initWFSrcDispatcher(aWFSrcArr, ip, port, meterFactor)
	}

	initWFSrcDispatcher { |wfsrcArr, ip, port,
		aMeterFactor|
		wFSrcs = wfsrcArr ;
		wFSrcs.do{|i| i.addDependant(this)} ;
		address = NetAddr(ip, port) ;
		meterFactor = aMeterFactor ;
		NetAddr.broadcastFlag = true ;
		address = NetAddr(ip, port) ;
	}

	update { arg theChanged, theChanger, more;
		//var pointXY, pointYZ ;
		var x, y, z ;
		var az ;
		[theChanged, theChanger, more].postln;
		x = more[1][1][0]*meterFactor;
		y = more[1][1][1]*meterFactor;
		z = more[1][1][2]*meterFactor;
		az =  if (x < 0){ atan(y/x) + pi }
		{ atan(y/x) } ;
		//we have to select the src
		// we have to convert in polar
		address.sendMsg("/iosono/renderer/version1/src",
			more[0],// src
			0,  // type
			az, // azimuth: 1.57
			atan(z/x), // elevation: 0.0
			sqrt(x.squared + y.squared + z.squared), // radius: 5
			1.0, // vol
			0.0, // LFE
			0.0, // delay
			0, //scaling
			0, // screen
			0.0, // spread
			0 //srcTrait
		)
	}

}