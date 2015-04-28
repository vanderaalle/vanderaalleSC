// caution: doesn't work with spaces in path
SampleListGenerator {

		*fromPath { arg samplesPath ;
			var p, l, labelList = [], fileName ;
			samplesPath.postln ;
			p = Pipe.new("ls" + samplesPath, "r") ;					l = p.getLine ;
			while({l.notNil}, {

					if ( (l.contains("/").not).and(l.contains(".")),
					{ 	fileName = (l.split($.)[0].asSymbol) ;
						labelList = labelList.add(fileName) ;
					 }) ;
				l = p.getLine;
				}) ;
			p.close ;
			^labelList
	}
}