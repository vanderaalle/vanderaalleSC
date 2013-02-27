// Andrea Valle, started on Nov 2009

// conversion in order to avoid cluttering other classes
// should be in general, but put it there
// so vep classes are all in the same place

+ SimpleNumber {


	degreerad {
		^this*pi/180 ;
	}
	
	raddegree {
		^this*180/pi ;
	}
			
}


+ Symbol {

	degreerad { ^this } 
	raddegree { ^this } 


}


+ AbstractFunction {

	degreerad { ^this.composeUnaryOp('degreerad') } 
	raddegree { ^this.composeUnaryOp('raddegree') } 


}


+ SequenceableCollection {
	
	degreerad { ^this.performUnaryOp('degreerad') }
	raddegree { ^this.performUnaryOp('raddegree') }	
}