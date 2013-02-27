+ String {
	// make stochastic
	// a workaround to make strings readable in stochastic 
	// LSys
	
	makeStochastic {
		var str = "" ;
		this.do{|i| str = str++i++"()"} ;
		^str
		}
		
	ms{ ^this.makeStochastic }
}