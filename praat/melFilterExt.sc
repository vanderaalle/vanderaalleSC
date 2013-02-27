+ ArrayedCollection {

// methods to work on filters	
	
	binarize { arg threshold = 0.5 ;
		var nf = [] ;
		this.flat.postln.do({ arg item ;  
					if (item >= threshold, {nf = nf.add(1)}, {nf = nf.add(0)}) ;
			}) ;
		^nf.clump(this[0].size) ;
	}


	normalize2D {
		^this.flat.normalize.clump(this[0].size) ;
	} 
	
	
	clip2D { arg lo, hi ;
		^this.flat.clip(lo, hi).clump(this[0].size) ;
	}
	
	
	// clean but useless, thresh works the same
	thresh2d { arg aNumber ;
		^this.flat.thresh(aNumber).clump(this[0].size) ;
	}
	
	

// posting and plotting

	postSlice { arg sliceIndex = 0, pitchList ;
		var slice = this[sliceIndex] ;
		slice.do({ arg item, index ;
			var pitch = pitchList[index] ;
			["pitch", pitch, "relevance", item].postln ;
		}) ;
		
	}
		
	postSliceT { arg sliceIndex = 0, threshold = 0, pitchList ;
		var n = 0, slice = this[sliceIndex] ;
		slice.do({ arg item, index ;
			var pitch = pitchList[index] ;
			if (item > threshold) {["pitch", pitch, "relevance", item].postln; n = n+1 ; };
		}) ;
		["total", n].postln ;
	}
	
	
	
	jSlice { arg sliceIndex, stepX, stepY ;
		var slice, window ;
		stepX = stepX ? 130 ;
		stepY = stepY ? 5 ;
		slice = this[sliceIndex] ;
		window = JSCWindow( "Slice", 
			Rect(100,100, stepX, this[0].size*stepY), resizable: false
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

	jFilter { arg stepX, stepY ;
		var index, window ;
		var newArr ;
		stepX = stepX ? 3 ;
		stepY = stepY ? stepX ;
		newArr = this.flat.normalize
				.clump(this[0].size) ;
		window = JSCWindow( "MelFilter", 
			Rect(100,100, this.size*stepX, this[0].size*stepY), resizable: false
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



}