+ SimpleNumber {


	melcps {
		var freq ;
		freq = 700*(exp(1)**(this/1127.01048)-1) ;
		^freq ;
	}
	
	cpsmel {
		var mel ;
		mel = 1127.01048*log(1+(this/700)) ;
		^mel ;
	}
			
}


+ Symbol {

	cpsmel { ^this } 
	melcps { ^this } 


}


+ AbstractFunction {

	cpsmel { ^this.composeUnaryOp('cpsmel') } 
	melcps { ^this.composeUnaryOp('melcps') } 


}


+ SequenceableCollection {
	
	cpsmel { ^this.performUnaryOp('cpsmel') }
	melcps { ^this.performUnaryOp('melcps') }	
}