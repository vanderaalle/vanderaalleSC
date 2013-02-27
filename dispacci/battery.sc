// for the Dispacci project, Milano 01/12/12

// the Battery is a container for Burst instances and processes the schedules them out
// each burst a separate out --> a sound source
Battery {
	
	var <>inBus, <>howMany, <>offset, <>busArr, <>burstArr ;
	// interface
	var <>fireTasks, <>processTasks ;
	var <>fireTimes, <>processTimes, <>freqs, <>tails, <>iters ;
		
	*new { arg inBus, howMany = 6, offset = 2 ; 
		^super.new.initBattery(inBus, howMany, offset) 	
	}
	
	initBattery { arg aInBus, anHowMany, anOffset ;
		{
		howMany = anHowMany ;
		inBus = aInBus ;
		offset = anOffset ;
		Server.local.sync ;
		burstArr = Array.fill(howMany, {|i| Burst(inBus, i+offset)}) ; 
		Server.local.sync ;
		// init interface
		fireTimes = Array.fill(howMany, {1}) ;
		processTimes = Array.fill(howMany, {1}) ;
		freqs = Array.fill(howMany, {20}) ;
		tails = Array.fill(howMany, {7}) ;
		iters = Array.fill(howMany, {1}) ;
		// filled with tasks, but you can control each one
		fireTasks = Array.fill(howMany, {|i|
			Task{ inf.do{ burstArr[i].play ; fireTimes[i].wait } }
			}) ;
		// a parallel process
		processTasks = Array.fill(howMany, {|i|
			Task{
				inf.do{
					burstArr[i].process(processTimes[i], freqs[i], tails[i], iters[i]) ;
					(processTimes[i]+0.5).wait ; // time foor processing is crucial
					}	
				}
			})
		}.fork
	}
	
	// fireTask interface
	fire { arg index ; fireTasks[index].play }
	pauseFiring { arg index ; fireTasks[index].pause }
	fireAll { fireTasks.do{|ts| ts.play } }
	pauseAllFiring { fireTasks.do{|ts| ts.pause } }
	

	// this is the second interface 
	process { arg index ; processTasks[index].play }
	pauseProcess { arg index ; processTasks[index].pause }
	processAll { processTasks.do{|ts| ts.play} }
	pauseAllProcesses { processTasks.do{|ts| ts.pause} }

	pauseAll { this.pauseAllFiring ; this.pauseAllProcesses }

	// mono testing and back 
	mix { burstArr.do{|i| i.outBus = 0; i.vol = 1/burstArr.size }}
	restoreFromMix {burstArr.do{|i, j| i.outBus = j+offset; i.vol = 1 }}
}

// 

/*
Server.local.reboot;


// here we read from file
~playBuf = Buffer.read(Server.local, "/Sonata1GMinorAdagio.aiff").normalize ; 
// we write on a bus

~in = Bus.audio(Server.local, 1) ;

~player = {arg out, buf; Out.ar(out, PlayBuf.ar(1, ~playBuf, loop:1))}.play(Server.local, args:[\out, ~in, \buf, ~playBuf]) ;




c = Battery(~in, 6) ; // create the Batterys
c.fireAll ; // play them all

c.processAll ;

c.pause(1)


c.fireTimes = Array.fill(6, {rrand(0.1,0.5)}) ;
c.processTimes = Array.fill(6, {rrand(0.1,0.9)}) ;
c.freqs = Array.fill(6, {rrand(20, 100)}) ;
c.tails = Array.fill(6, {rrand(2, 20)}) ;
c.iters = Array.fill(6, {rrand(1, 6)}) ;


c.mix
c.restoreFromMix


*/