// andrea valle, hopefully for a percussion piece

Page { 

	// 
	var <>nb ; // nb is a NB object
	var <>number ; // staves per system
	var <>separator ; // distance between subsequent Staffs
	var <>corner  ; // [x,y] of where to start drawing Staffs
	
	
/*
	_______________________
	|
	|	
	|	corner[x,y] ->
	|	|----------------
	|	V		ö
	|    -------- |------		
	|			| separator: from top corner (IMPORTANT)
	|			v
	|	 -----------------
	|	


	here you don't control staves, just place them 	
*/

	var <>width ; // Staff width
	var <>organization ; // Staff organization array
	// so you can have:
/*
_________________________________
_________________________________


_________________________________
_________________________________
_________________________________

*/	
	// its size indicates the number of lines in the staff
	var <>partition ; // number of measure on each staff
	var <>step ;  // a multiplier for organization (scales)
	var <>lineNumber ; // known from organization
	var <>code ; // to collect NB code
	var <>mapFunc ; // a mapping function, just for testing the mechanism
	classvar <pointcm = 28.3465 ; 

	
	
	*new { arg nb, number, corner, separator,
		lineNumber, organization, partition, width, step ; // interface for Staff 
		^super.new.initPage(nb, number, corner, separator,
			lineNumber, organization, partition, width, step
		) 
	}

	initPage { arg aNb, aNumber, aCorner, aSeparator,
			aLineNumber, anOrganization, aPartition, aWidth, aStep ;
		nb = aNb ;	
		number = aNumber ;
		organization = anOrganization ;
		separator = aSeparator ;
		corner = aCorner ;
		lineNumber = aLineNumber ; 
		organization = anOrganization ;
		partition = aPartition ;
		width = aWidth ; 
		step = aStep ; 
		// just for test
		mapFunc = { arg item, x, staffIndex ;
			var line = item.asInteger ;
			var rest = if (item.asInteger != item) {0.5} {0} ;
			var y = staffIndex*separator + (organization[..(line-1)].sum+rest*step) ;
			var dim = 6 ;
			nb.push ;
			nb.rotate(30) ;
			nb.oval(x-(dim*0.5), y-(dim*0.5), dim*1.3, dim) ;
			nb.pop ;	
		}
	}


	createStaves {
		var factor, increase = 0, cd = [] ;
		nb.translate(corner[0], corner[1]) ;
		nb.push ;
		number.do({ arg i ;
			Staff.new(nb, lineNumber, organization, partition, width, step )
				.createLeftDelimiter 
				.createRightDelimiter 
				.createInnerDelimiters
				.createSubdivisions
				.createLines ;
			nb.translate(0, separator) 
		}) ;
		nb.pop ;
	}

// just for test
	placeNote { arg item, time, totalTime ; 
	// line: line index of the staff ; time: the time ; totalTime: duration of a page 
		var totalWidth = width * number ;
		var place = time*totalWidth/totalTime ; // conversion in space
		var x = place % width ;	 // wrap up
		var staffIndex = place.div(width) ;
		mapFunc.value(item, x, staffIndex) ;
	}



}


// substantially private
Staff {

	var <>nb ; // nb is a NB object
	var <>width ; // Staff width
	var <>organization ; // an array defining a set of distancers
	// so you can have:
/*
_________________________________
_________________________________


_________________________________
_________________________________
_________________________________

*/	
	// its size indicates the number of lines in the staff
	var <>step ;  // a multiplier for organization (scaler)
	var <>lineNumber ; // known from organization
	var partition ; // number of subdivision
	
	classvar <pointcm = 28.3465 ; 
	
	*new { arg nb, lineNumber, organization, partition, width, step ; 
		^super.new.initStaff(nb, lineNumber, organization, partition, width, step) 
	}


	initStaff { arg aNb, aLineNumber, anOrganization, aPartition, aWidth, aStep ;
		nb = aNb ;	
		width = aWidth ;//*pointcm ;
		organization = anOrganization ;
		step = aStep ;//*pointcm ;
		lineNumber = aLineNumber ;
		partition = aPartition ;
		if ( organization.size+1 != lineNumber, 
			{ "WARNING!: inconsistency. Organization defaulted to [1,1..]".postln ;
				organization = Array.fill(lineNumber-1, {1}).postln 
			});
		nb.stroke(0, 0, 0) ;
	}


	createLines {
		var factor, increase = 0 ;
		// the first
		nb.line(0, 0, width, 0) ;
		organization.do({ arg i, ind ;
			factor = i*step ;
			nb.line(0, factor+increase, width, factor+increase ) ;
			increase = increase  +factor ;
	}) ;
	}

	createLeftDelimiter { arg serif = 5 ;
		nb.strokewidth(2) ;
		nb.line(0, 0-(step*0.5), 0, (organization.sum+1) * step-(step*0.5)) ; 
		nb.line(0,  0-(step*0.5), serif, 0-(step*0.5)) ;
		nb.line(0,  (organization.sum+1) * step-(step*0.5),serif, (organization.sum+1) * step-(step*0.5)) ;
		nb.strokewidth(1) ;
	} 
		
	createRightDelimiter { arg serif = 5 ;
		nb.strokewidth(2) ;
		nb.line(width, 0-(step*0.5), width, (organization.sum+1) * step-(step*0.5)) ;
		nb.line(width-serif,  0-(step*0.5),width, 0-(step*0.5)) ;
		nb.line(width-serif,  (organization.sum+1) * step-(step*0.5),width, (organization.sum+1) * step-(step*0.5)) ;
		nb.strokewidth(1) ;
	} 

	createInnerDelimiters { 
		var wStep = width/partition ;
		(partition-1).do({ arg i ;
			//nb.line(i+1*wStep, 0-(step*0.5), i+1*wStep, (organization.sum+1) * step-(step*0.5))
			nb.dottedLine(i+1*wStep, 0-(step*0.5), i+1*wStep, (organization.sum+1) * step-(step*0.5))
		}) ;
	} 
	
	createSubdivisions { arg number = 4, len, transl ;
		var wStep = width/partition ;
		len  	? len = (organization.sum+1) * (1/15) * step ; 
		transl 	? transl = (organization.sum+1) * (12.8/30 ) * step ; // 4.1 empyrical
		nb.push ;
		nb.translate(0, transl) ;
		(partition-1).do({ arg i ;
			(number-1).do({ arg k ;
			k = (k+1)*0.25 ;
			nb.line(
				i+k*wStep, 
				0 ,
				i+k*wStep, 
				len
				) ;
			}) ;
		}) ;
		nb.pop ;
	} 


}


