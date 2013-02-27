/*
GUI.cocoa

~tmpnm = 400 ;
// 400*0.1*6/60 = 4 min


r = RuScore("/musica/rumentarium/scores/Image2.png")

r.clean(138, timePointNum: ~tmpnm)

r.plot(r.source)

r.plot(r.cleanSource)

r.plot(r.sampledSource)
r.plot(r.target)

r.scoreArray.postln


(
o = ArduinoSMS("/dev/tty.usbserial-A9007LwD", 115200); // blue
p = ArduinoSMS("/dev/tty.usbserial-A1001N7X", 115200); // green
q = ArduinoSMS("/dev/tty.usbserial-A9007LnA", 115200);  // white

~instrList = [
	\tintinnabolum1,
	\tintinnabolum2,
	\patella1,
	\patella2,
	\campana1,
	\campana2,
	
	\sistrum1,
	\sistrum2,
	\sphera,
	\tympanum,
	\campanarium,
	\globus,
	
	\crepitacolum1,
	\crepitacolum1,
	\tubus1,
	\tubus2,
	\discus1,
	\discus2
] ;

k = RuMaster([o,p,q], ~instrList) ;
)

r.addDependant(k) ;

r.play(~tmpnm, 18, 0.1) ; // Chronometer.new(500, 500, "Vanitas", 0.1)
r.stop
*/

// crop the source to effective vertical area. Do not rely on offsets (top/bottom)
// horizontal area depends on you, you can clean it as preferred

// can probably be generalized by parametrization
RuScore {
	
	// on init:
	var <>path ; // the path of the file
	var <>source ; // the input SCImage source
	var <>sampledSource ;
	var <>cleanSource ; // 
	var <>target ; // the output SCImage
	var <>score, <>task, <>scanInt ;
	var <>scoreArray ; // the notation array 
	
	*new { arg path ;
		^super.new.initRuScore(path) 
	}

	initRuScore { arg aPath ;
		path = aPath ;
		source = SCImage.new(path);
	}
	
	plot { arg image ;
		^image.plot(showInfo:false) 
	}
	
	// 6*18+6*5 = 138
	clean { arg lines = 138, topOffset = 0, bottomOffset = 0, timePointNum = 1000 ;  
	//topOffset = 4, bottomOffset = 12, timePointNum = 1000 ;  // parametrized for the case
		sampledSource = SCImage.fromImage(source) ;
		this.scale(timePointNum, topOffset, lines, bottomOffset) ;
		this.removeOffsets(timePointNum, topOffset, lines, bottomOffset) ;
		this.slice(timePointNum) ;
	}
	
// private
	scale { arg timePointNum, topOffset, lines, bottomOffset ;
		sampledSource.scalesWhenResized_(true);
		sampledSource.setSize(timePointNum, topOffset + lines + bottomOffset); 
	}
	
	removeOffsets {  arg timePointNum, topOffset, lines, bottomOffset ;
		// deleting whites on top and bottom
		var pixls = sampledSource.pixels[timePointNum*topOffset..timePointNum*(topOffset+lines)] ;
		// a new image with the remaining array
		target = SCImage.new(timePointNum, lines) ;
		target.setPixels(pixls) ;
	}
	
	
	slice { arg timePointNum, staff = 18, systems = 6, sep = 6 ;
		// strictly hard coded
		// deletes blanks
		var newArr = [] ;
		var pxls = Int32Array.new ;
		var i = 0 ;
		systems.do({ 
			pxls = pxls++target.pixels[i*timePointNum..(i+staff)*timePointNum-1] ;
			i = i+staff+sep
		}) ;
		// a new image with the remaining array
		// original without blanks
		cleanSource = SCImage.new(timePointNum, staff*systems) ;
		cleanSource.setPixels(pxls) ;
		pxls = pxls.clump(timePointNum) ;
		staff.do{ |ind|  
				systems.do{ |ind2|
				newArr = newArr++pxls[ind+(staff*ind2)] 
			}
		} ;		
		target = SCImage.new(timePointNum*systems, staff) ;
		scoreArray = newArr.flat.as(Int32Array) ; //accessible from external
		target.setPixels(scoreArray) ;
		// one component is enough, in [0,1]
		score = scoreArray.as(Array).collect({|e| (255-e.red)/255}); 
		score = score.clump(timePointNum*systems).flop ;
	}
// End of Private


	play { arg timePointNum, staff, scanInterval ;
		var window, old = score[0] ;
		var x, y, offset ;
		if (task.isNil) {
				window = cleanSource.plot(showInfo:false) ;
				scanInt = scanInterval ;
				task = Task({
					score.do({ arg item,index ;
						this.changed(this, [item]) ;
						{
						x = index % timePointNum;
						offset = (index/timePointNum).asInteger*staff ;						staff.do{ arg i ;
							y = offset + i  ;
							cleanSource.setColor(Color(1-old[i], 1-old[i], 0.25), 
									(x-1).max(0), y) ;
							cleanSource.setColor(Color.red, x, y) ; 
							old = item ;
							} ;
						window.refresh ;
						}.defer ;
						scanInterval.wait ;
						}) ;
						this.changed(this, [Array.fill(staff, 0)]) ;
					"finished (arduino still open)".postln ;
			}) ;
		task.play(SystemClock) 
		}
		{ scanInt = scanInterval ; task.play(SystemClock) };
	}


	start {
		task.start(SystemClock) ;
	}

	pause {
		task.pause ;
	}

// the same
	stop {
		task.stop ;
	}

	reset {
		task.reset ;
	}


/*

	sliceSeparated { arg timePointNum ;
		// strictly hard coded
		var pxls = [
			target.pixels[0..timePointNum*6-1], 		
			target.pixels[timePointNum*12..timePointNum*18-1],
			target.pixels[timePointNum*24..timePointNum*30-1],
			target.pixels[timePointNum*36..timePointNum*42-1],
			target.pixels[timePointNum*48..timePointNum*54-1], 
			target.pixels[timePointNum*60..timePointNum*66-1] 
			];
					// a new image with the remaining array
		pxls.do{|i| i[i.size-1].postln} ;			
		target = Array.fill(pxls.size, { |p| SCImage.new(timePointNum, 6).setPixels(pxls[p])}) ;
		target.do{|t| t.plot }
	}
*/

}