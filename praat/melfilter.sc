MelFilter {


	var <>melFilter ;
	var <>xmin, <>xmax, <>nx, <>dx, <>x1, <>ymin, <>ymax, <>ny, <>dy, <>y1 ;
	var <>pitchList, <>pitchDict, <>pitchFilter, <>pitchKeys ;



	importMelFilter { arg melFilterFileName ;
		var header = [], line, arr ;
		melFilterFileName = melFilterFileName ? String.scDir++"/sounds/praatTmp.melFilter" ;
		melFilterFileName = if ( melFilterFileName[0]==$/, { melFilterFileName }, 
									{ String.scDir++"/"++melFilterFileName }) ;

		melFilter = File.new(melFilterFileName, "r") ;			13.do({arg i; header = header.add(melFilter.getLine); }) ; // eliminate header info
		header.postln ;
		# xmin, xmax, nx, dx, x1, ymin, ymax, ny, dy, y1 = header[3..].asFloat ;
		line = melFilter.getLine.asFloat ;
		arr = [line] ;
		while({line.notNil}, {arr = arr.add(line.asFloat); line = melFilter.getLine; }) ;
		melFilter.close ;
	// now melFilter becomes the right data structure
		melFilter = arr.normalize.clump(header[5].asInteger).flop ;
	// with flop melMilter is a series of time slices representing freqs in time
		} 



// working on pitch

	createPitchList { arg rounding = 1.0; // fraction  of semitone
		pitchList = Array.new ;
		melFilter[0].size.do({ arg item, index ;
			pitchList = pitchList.add(((index+1) * y1 + dy).melcps.cpsmidi.round(rounding)) ;
		})
	}
	

	createPitchDict {
		pitchDict = Dictionary.new ;
		pitchList.asSet.do({ arg item ;
			pitchDict[item] = [] ;
			}) ;
		pitchList.do({ arg item, index ;
			pitchDict[item] = pitchDict[item].add(melFilter.flop[index]) ;
			}) ;
		pitchDict.keys.do({ arg item ;
			pitchDict[item] = pitchDict[item].sum/pitchDict.size ; 
		}) ;
	}

	
	createPitchFilter { arg minPitch, maxPitch ;
		pitchFilter = Array.new ;
		minPitch = minPitch ? pitchDict.keys.asArray.sort[0] ;
		maxPitch = maxPitch ? pitchDict.keys.asArray.sort.reverse[0] ;
		pitchDict.keys.asArray.sort.do ({ arg item, index ;
			if ((item >= minPitch) && (item <= maxPitch),
				{ pitchFilter = pitchFilter.add(pitchDict[item])}
				) ;
		}) ;
		pitchFilter = pitchFilter.flop ; 
		pitchKeys = pitchDict.keys.asArray.sort[minPitch..maxPitch] ;
	}
	
		
	
	
/*	
// methods to work on filters	
	
	binarize { arg threshold = 0.5, filter ;
		var nf = [] ;
		filter = filter ? this.pitchFilter ; 
		filter.flat.postln.do({ arg item ;  
					if (item >= threshold, {nf = nf.add(1)}, {nf = nf.add(0)}) ;
			}) ;
		filter = nf.clump(filter[0].size) ;
		^filter	
	}


// posting and plotting

	postSlice { arg sliceIndex = 0 ;
		var slice = melFilter[sliceIndex] ;
		slice.do({ arg item, index ;
			var pitch = pitchList[index] ;
			["pitch", pitch, "relevance", item].postln ;
		}) ;
		
	}
		
	postSliceT { arg sliceIndex = 0, threshold = 0 ;
		var n = 0, slice = melFilter[sliceIndex] ;
		slice.do({ arg item, index ;
			var pitch = pitchList[index] ;
			if (item > threshold) {["pitch", pitch, "relevance", item].postln; n = n+1 ; };
		}) ;
		["total", n].postln ;
	}
	
	
	
	jSlice { arg sliceIndex, stepX, stepY, filter ;
		var slice, window ;
		stepX = stepX ? 130 ;
		stepY = stepY ? 5 ;
		filter = filter ? this.pitchFilter ; 
		slice = filter[sliceIndex] ;
		window = JSCWindow( "Slice", 
			Rect(100,100, stepX, filter[0].size*stepY), resizable: false
					);
		window.view.background = Color.white;
		window.drawHook = {
			slice.do({ arg item, index ;
				JPen.color = Color.new(1.0-item, 1.0-item, 1.0-item);
				JPen.fillRect( Rect( 0, index*stepY, stepX, stepY ));
				JPen.color = Color.new(0.5, 0.5, 0.5);
				//JPen.width = 0.5;
				JPen.line(0 @ index*stepY, stepX @ index*stepY) ;
				// JPen.font = JFont( "Helvetica-Bold", 12 );
				// JPen.fillColor = Color.red;
				// JPen.stringAtPoint(item,((0 @ index*stepY)+12)) ;
				}) ;
				JPen.stroke ;
			} ;
		window.front;

	
	}

	jFilter { arg stepX, stepY, filter ;
		var index, window ;
		var newArr ;
		stepX = stepX ? 3 ;
		stepY = stepY ? stepX ;
		filter = filter ? this.pitchFilter ; 
		newArr = filter.flat.normalize
				.clump(filter[0].size) ;
		window = JSCWindow( "MelFilter", 
			Rect(100,100, filter.size*stepX, filter[0].size*stepY), resizable: false
					);
		window.view.background = Color.white;
		window.drawHook = {
			newArr.do({ arg y, n ; // using normalized
				y.reverse.do({ arg item, m ;
				JPen.color = Color.new(1.0-item, 1.0-item, 1.0-item);
				JPen.fillRect( Rect( n*stepX, m*stepY, stepX, stepY ));
					});			
				});
			};
		window.front;
		}
*/			


}



Player88 {
		
		
	var <>pitchFilter, <>pitchKeys ;
			
	*new { arg pitchFilter, pitchKeys ;
			^super.new.init(pitchFilter, pitchKeys) ;
	}	

	init { arg pitchFilter, pitchKeys ;
			var levels ;
			this.pitchFilter = pitchFilter ;
			this.pitchKeys = pitchKeys ; 
			levels = ("#"+pitchFilter.flop[0].asString).interpret ;
			levels.postln ;
					// single filter
			SynthDef("player88", { arg out = 0, freq = 100, dur = 1.0, levels = #[0.1, 0.5, 0.1] ;
				var env, sinus ;
				env = Env.new(levels, Array.fill(levels.size-1, { dur })) ;
				env.postln;
				sinus = SinOsc.ar(freq) 
					* EnvGen.kr(env);//, levelScale: 1/env.size) ;
				Out.ar(out, sinus ) ;
			}).send(Server.local) ;

	}


	play {
		var pitchFilter2 = pitchFilter.flop ;
		pitchFilter2.do({ arg item, index ;
			Synth.tail(nil, "player",  [\freq, item.midicps, \levels, pitchFilter2[index], \dur, 0.01 ]) ;
		}) ;	
	}

}
