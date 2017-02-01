// a class to encapsulate Wavefield source
// a source is just a proxy to define position
// conversion to/from polar/cartesian
// if it has a synth associated, it can scale vol
// it notifies, e.g. to an OSC module
// and to a plotter module
/*
// my computer
// studio
192.168.0.56
255.255.255.0

// konzerthaus
192.168.1.1
255.255.255.0


//sending to
192.168.1.1, 4243

*/


WFSrc {
	var <>id ;
	var synth, <>bus, vol ;
	var xy ; // an array, both normalized -1, 1

	*new { |id, x = 0, y = 0|
		^super.new.initWFSrc(id, x, y)
	}

	initWFSrc { |anId,anX, anY|
		id = anId;
		this.xy_(anX,anY);
		Server.local.waitForBoot{
			SynthDef(\wfsrc, {|in, out, amp = 1|
				Out.ar(out, In.ar(in)*amp)
			}).add ;
			bus = Bus.audio(Server.local, 1) ;
			Server.local.sync ;
			synth = Synth(\wfsrc, [\in, bus, \out, id])
		}
	}

	xy_ {|newX,newY| xy = [newX, newY];
		this.changed(\xy, [id, xy])
	}
	xy {^xy}

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

/*
SynthDef(\sine, {|out, amp = 1, freq = 440|
	Out.ar(out,
		SinOsc.ar(freq, mul: amp))
}).add ;

~arr = Array.fill(10, {|i|
WFSrc(i)}).collect{|i|i.xy_(rrand(-1.0,1), rrand(-1.0,1))}

~synths = Array.fill(10, {|i|Synth(\sine, [\out, ~arr[i].bus])})
s.scope

~arr.do{|i| i.mute}
~arr.do{|i| i.unmute}
~arr.do{|i| i.vol_(-6)}
~arr.do{|i| i.free}

*/

WFSrcPlotter {
	var <>wFSrcs ;
	var <>or, <>dim, <>ptD ;
	var <>window ;

	*new { |aWFSrcArr|
		^super.new.initWFSrcPlotter(aWFSrcArr)
	}

	initWFSrcPlotter { |wfsrcArr|
		dim = 300 ;
		ptD = 10 ;
		or = Point(dim, dim) ;
		wFSrcs = wfsrcArr ;
		wFSrcs.do{|i| i.addDependant(this)} ;
		this.createWindow ;
	}

	createWindow {
		var col = 1.0/wFSrcs.size ;
		window = Window("WFSrc Plotter", Rect(100, 100, dim*2, dim*2)).background_(Color.grey(0.2))
		.drawFunc_{
			wFSrcs.do{|i, id|
				Pen.fillColor_(Color.hsv(col*id, 0.8, 1)) ;
				Pen.fillOval(
					Rect(
						i.xy[0]*dim-(ptD*0.5)+or.x,
						i.xy[1].neg*dim-(ptD*0.5)+or.y, ptD, ptD))}
		}
		.front ;
	}

	update { arg theChanged, theChanger, more;
		//[theChanged, theChanger, more].postln ;
			window.refresh
	}
}

/*
a = WFSrc(0) ; b = WFSrc(1) ;
a.xy_(0.10, 0.20)
b.xy_(0.50,0.10)
c = WFSrcPlotter([a,b])
c.wFSrcs
a.xy_(0,-1)

r = {inf.do{|i|
a.xy_(a.xy[0].postln+(i*0.01)%1, a.xy[1].postln+(i*0.01)%1) ;
0.5.wait
}
}.fork(AppClock)
r.stop

~arr = Array.fill(50, {|i| WFSrc(i)}).collect{|i|i.xy_(rrand(-1.0,1), rrand(-1.0,1))} ;
c = WFSrcPlotter(~arr) ;

r = {inf.do{
	var x, y;
	~arr.do{|i|
		x = (i.xy[0]+rrand(-0.01, 0.01)) ;
		y = (i.xy[1]+rrand(-0.01, 0.01)) ;
		case { x > 1 }{
			x = 1-x.frac;
		}
		{ x < -1}{
			x = x.frac.neg;
		} ;
		case { y > 1 }{
			y = 1-y.frac;
		}
		{ y < -1}{
			y = y.frac.neg;
		} ;
		i.xy_(x, y);
};
		0.05.wait
}
}.fork(AppClock)
r.stop

*/

// does not need update from external sources
// dirty approach
WFSrcLifePlotter {
	var <>or, <>dim, <>ptD ;
	var <>window ;
	var <>state ;
	var <>grid ;

	*new { |grid, state|
		^super.new.initWFSrcPlotter(grid, state)
	}

	initWFSrcPlotter { |aGrid, aState|
		dim = 300 ;
		ptD = 10 ;
		or = Point(dim, dim) ;
		state = aState.flat ; // life matrix
		grid = aGrid ;
		this.createWindow ;
	}

	createWindow {
		var col = 1.0/state.size ;
		window = Window("Life Plotter", Rect(100, 100, dim*2, dim*2)).background_(Color.grey(0.2))
		.drawFunc_{
			grid.do{|i, id|
				// BUGGY
				if(state[id] == 1) {
				Pen.fillColor_(Color.hsv(col*id, 0.8, 1)) ;
				Pen.fillOval(
					Rect(
						i[0]*dim-(ptD*0.5)+or.x,
						i[1].neg*dim-(ptD*0.5)+or.y, ptD, ptD))}
			}
		}
		.front ;
	}

	refresh { window.refresh }

}



WFSrcDispatcher {
	// this:
	// 1. map to virtual dimension
	// 2. converts to radians
	// 3. dispatches to IP

	var <>wFSrcs ;
	var <>address ;
	var <>meterFactor, <>ratioXY ;

	*new { |aWFSrcArr, ip = "192.168.1.1", port = 4243, meterFactor = 1, ratioXY = 1|
		^super.new.initWFSrcDispatcher(aWFSrcArr, ip, port, meterFactor, ratioXY)
	}

	initWFSrcDispatcher { |wfsrcArr, ip, port,
		aMeterFactor, aRatioXY|
		wFSrcs = wfsrcArr ;
		wFSrcs.do{|i| i.addDependant(this)} ;
		address = NetAddr(ip, port) ;
		meterFactor = aMeterFactor ;
		ratioXY = aRatioXY.reciprocal ;
		NetAddr.broadcastFlag = true ;
		address = NetAddr(ip, port) ;
	}

	update { arg theChanged, theChanger, more;
		var point ;
		//we have to select the src
		// we have to convert in polar
		//[theChanged, theChanger, more].postln ;
		point = Point(more[1][0],
			more[1][1]*ratioXY).asPolar ;
		address.sendMsg("/iosono/renderer/version1/src",
			more[0],// src
			0,  // type
			point.theta, // azimuth: 1.57
			0.0, // elevation
			point.rho*meterFactor, // radius: 5
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

/*
a = WFSrc(0) ; b = WFSrc(1) ;
a.xy_(0.10, 0.20)
b.xy_(0.50,0.10)
c = WFSrcPlotter([a,b])
c.wFSrcs
d = WFSrcDispatcher([a,b], "192.168.0.255", 4243)


a.xy_(0,-1)
b.xy_(1,-1)


b.xy
*/


WFSrcLogger {
	// Following the model of the previous
	// dependant classes creates a log ASCII
	// file that can be easily parsed
	// (Nodebox, SC etc)
	// data represents for each WFSrc
	// its time-stamped positions

	var <>wFSrcs ;
	var <>path, <>dict, <>file ;
	var <>time ;

	*new { |aWFSrcArr, path|
		^super.new.initWFSrcLogger(aWFSrcArr, path)
	}

	initWFSrcLogger { |wfsrcArr, aPath|
		wFSrcs = wfsrcArr ;
		wFSrcs.do{|i| i.addDependant(this)} ;
		path = aPath ;
		file = File(path, "w") ;
		time = thisThread.seconds ;
		dict = () ;
		wFSrcs.do{|i,j|
			dict[j] = [] ;
		};
	}

	update { arg theChanged, theChanger, more;
		var point ;
		dict[more[0]] = dict[more[0]]
		.add([thisThread.seconds-time, more[1]]) ;
	}

	close {
		// write in an easy parsifiable ASCII format
		file.write("[") ;
		dict.do{|i|
			file.write(i.asCompileString);
			file.write(",") ;
	};
	file.write("]") ;
	file.close ;
	}

}

