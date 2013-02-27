
+ Array {

// returns a couple of arrays
// [a, b, b, c] --> [[a, b, c], [1, 2, 1]]

	itemsAndOccurrences {
		var items = [this[0]] , occurrences = [] ;
		var ind = 1, old ;  
		old = this[0] ;
		this[1..].do({ arg item, index ;
			if (item == old, { ind = ind+1 }, { 
						items = items.add(item) ;
						occurrences = occurrences.add(ind) ;
						old = item ; ind = 1 ;
						}) ;				
		}) ;
		occurrences = occurrences.add(ind) ; 
		^[items, occurrences]
	}


// downsample an array of size = n to an array of size = m < n
// using linear interpolation 
	downsample { arg newSize ;
		var newArr = [] ;
		var step = this.size/newSize ;
		var x, y, x0, y0, x1, y1, alpha ;
		newSize.do({ arg index ;
			 x = step * index ;
			 x0 = x.asInteger ;
			 x1 = x0+1 ;
			 y0 = this[x0] ;
			 y1 = this[x1] ;
			 y = case
			 	{ (y0 == -inf).and(y1 == -inf) } { -inf }
			 	{ (y0 == -inf).and(y1 != -inf)} { y1 }
			 	{ (y0 != -inf).and(y1 == -inf)} { y0 }
			 	{ (y0 != -inf).and(y1 != -inf)} 
			 		{  
						alpha = (x - x0)/(x1 - x0) ;
				 		y = y0 + (alpha*(y1-y0)) ;
				 	} ;
			newArr = newArr.add(y) ;
		}) ;
		^newArr
	}


	lineSegment { arg range = 0.25;
		var newArr = [this[0]] ;
		var refCoeff, coeff ;
		var refSample, sample ; 
		refSample = this[0] ;
		refCoeff = this[1] - this[0] ;
		(this.size-2).do({ arg index ;
			coeff = this[index+2] - this[index+1] ;
			sample = this[index+1];			
			if ( (coeff >= (refCoeff-range)).and(coeff <= (refCoeff+range)), 
					{ newArr = newArr.add(refSample) } ,
					{ refCoeff = coeff ; 
						refSample = sample ; 
						newArr = newArr.add(refSample) }
						) ;			
			}) ;
		
		^newArr.add(this.reverse[0])
	
	}




	asAgArray {
	}
	
	simpleMovingAverage { arg neighbor = 1 ;
		var indices = Array.series(this.size-neighbor, neighbor) ;
		var sma = this[..(neighbor-1)] ; // first untouched (no memory)
		sma.postln ;
		indices.do({ arg index ;
				sma = sma.add(
					this[index-neighbor..index].sum/(neighbor+1)
					) ;
		}) ;
		sma.size.postln ;
		^sma
	}
	

// if silence	is -inf this result with 
// in adding neighbor rests at the head of eac note group
	simpleMovingAverage2 { arg neighbor = 1 ;
		var newArr = [] ;
		var indices = Array.series(this.size-neighbor, neighbor) ;
		var sma = this[..(neighbor-1)] ; // first untouched (no memory)
		indices.do({ arg index ;
				sma = sma.add(
					this[index-neighbor..index].sum/(neighbor+1)
					) ;
		}) ;
		sma.do({ arg item, index ;
			if ( (item == -inf).and(this[index] != -inf ), 
				{ newArr = newArr.add(this[index]) }, 
				{ newArr = newArr.add(item) } ;
				);
		}) ;
		^newArr
	}


	deleteOscillations { arg threshold = 0.5 ;
		var arr = [this[0]], index = 0 ;
		var first, second, third ;
		while ( { index <= (this.size-2) }, 
			{	first = this[index] ;
				second = this[index+1] ;
				third = this[index+2] ; 	
				# arr, index = this.deleteOscillationsOn3( first, second, third,
						 arr, index, threshold ) ;
			} );
		^arr
	}

// private of deleteOscillations
	deleteOscillationsOn3 { arg first, second, third, arr, index, threshold ;
		if ( ((first - second).abs <= threshold).and(first == third), 
			{ arr = arr.addAll([first, first]) ; index = index + 2 },
			{ arr = arr.addAll([second]) ; index = index + 1 }
		) ;
		^[arr, index]
	}
	


	octaveToRange { arg minVal, maxVal ;
		// it works assuming at least min-max = -12 (octave);
		var newArr = [] ;
		this.do({ arg item ;
			if ( item != -inf, { 
				while ({ item >= maxVal }, { item  = item - 12 }) ;
				while ({ item <= minVal }, { item  =  item + 12}) ;
				} );
			newArr = newArr.add(item)
				
		}) ;
		^newArr 
	}


// This is specific for ag arrays
// should move to AGArray def

	stretchDurations { arg factor = 1 ;
		var pitches = this.clump(2).flop[0] ;
		var durations = this.clump(2).flop[1] ; 
		durations = durations * factor ;
		^[pitches, durations].flop.flat
	}

	transposePitches { arg delta = 0 ;
		var pitches = this.clump(2).flop[0] ;
		var durations = this.clump(2).flop[1] ; 
		pitches = pitches + delta ; 
		^[pitches, durations].flop.flat
	}

	changePitchRange { arg factor = 1 ;
		var pitches = this.clump(2).flop[0] ;
		var durations = this.clump(2).flop[1] ; 
		pitches = (pitches - pitches.minItem)*factor + pitches.minItem ; 
		^[pitches, durations].flop.flat
	}


// the same as average

	smoothPitches {  arg neighbor = 1 ;
		var pitches = this.clump(2).flop[0] ;
		var durations = this.clump(2).flop[1] ;
		pitches = pitches.simpleMovingAverage(neighbor) ; 
		^[pitches, durations].flop.flat

	}



// drawing pitch contours

	jPitchContour { arg stepX, stepY, maxHz ;
		var index, window ;
		var newArr ;
		var chosen = [] ;
		stepX = stepX ? 3 ;
		stepY = stepY ? stepX ;
		maxHz = maxHz ? 2000 ;
		newArr = if (this[0].isArray.not, {[this]}, {this}) ;
		newArr = this*100/maxHz; // (0-100)
		window = JSCWindow( format("Pitch (0-% Hz)", maxHz), 
			Rect(100,100, this[0].size*stepX, stepY*100), resizable: false
					);
		window.view.background = Color.white;
		window.drawHook = {
			this.do({	 arg pitchContour ;
					newArr = pitchContour*100/maxHz; // (0-100)
					JPen.color = Color.new(1.0.rand, 1.0.rand, 1.0.rand) ;
					newArr.do({ arg y, n ; // using normalized
						JPen.fillRect( Rect( n*stepX, stepY*(100-y), stepX, stepY ));
						});
					}) ;
				} ;
		window.front;
		}
		

// SPECIALS for NimChimpsky: 
	
	
	// this considers a terminal t/f(+*) as a flag
	itemsAndOccurrencesWithFlag {
		var items = [this[0]] , occurrences = [] ;
		var ind = 1, old = this[0] ;
		var continuationFlag = old.asString.split($*)[2] ;
		this[1..].do({ arg item, index ;
			if ( item == -inf, { 
				if ( (item == old), 
						{ind = ind+1}, 
						{ 
						items = items.add(item) ;
						occurrences = occurrences.add(ind) ;
						old = item ; 
						continuationFlag = old.asString.split($*)[2] ;
						ind = 1 ;
						}) ;
						
					},{ 	
					if ( (item == old).and(continuationFlag == "t"), 
						{ind = ind+1}, 
						{ 
						items = items.add(item) ;
						occurrences = occurrences.add(ind) ;
						old = item ; 
						continuationFlag = old.asString.split($*)[2] ;
						ind = 1 ;
						}) ;
						
					}) ;
									
		}) ;
		occurrences = occurrences.add(ind) ; 
		^[items, occurrences]
	}



	// supposes a flopFlattened array
	/*
e.g.
[ -inf, 17, 60, 13, -inf, 6, 60, 14, -inf, 25, 60, 14, -inf, 8, 60, 52, -inf, 20, 60, 13, -inf, 62, 60, 27, -inf, 1, 60, 17, -inf, 70, 60, 21, -inf, 16, 60, 13, -inf, 26 ]
	*/
	// generate a sequence to be flopFlattened 
	// referenceArr is the aray containing the starting phones
	
	replaceWithUtterances { arg startArr, nimGraph ;
		var newArr = [] ;
		var itemArr, indexArr ;
		var nim = NimChimpsky.new(nimGraph) ;
		var ind = 0 ;
		# itemArr, indexArr = this.clump(2).flop ;
		itemArr.do({ arg item, index ;
			var length = indexArr[index] ; 
			var infArr ;
			var startingPhone, utterance ;
			if ( item == -inf,
						{ 
						infArr = Array.fill(length, -inf) ;
						newArr = newArr.addAll(infArr) ;
						ind = ind + indexArr[index] ;
						//totalLength = totalLength + length ; 
						} 
				) ;
			if ( item == 60,  
						{ 
						startingPhone = startArr[ind] ;
						utterance = nim.generateUtterance(startingPhone, length) ;
						newArr = newArr.addAll(utterance) ;
						ind = ind + indexArr[index] ;
						//totalLength = totalLength + length ;
						}	
				)
		})
		^newArr 
	}
 /*
	jPitchContourEach { arg pitchContour, stepX, stepY, maxHz, window ;
		var index ;
		var newArr ;
		var chosen = [] ;
		newArr = pitchContour*100/maxHz; // (0-100)
		window.drawHook = {
			newArr.do({ arg y, n ; // using normalized
					JPen.color = Color.new(0.0, 0.0, 0.0);
					JPen.fillRect( Rect( n*stepX, stepY*(100-y), stepX, stepY ));
					});
			};
		}
*/

}