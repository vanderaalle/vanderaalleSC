(
// test with piano staff
var size = 1000 ;
var a3 = [size*2.sqrt, size] ; 
var corner = [50, 175] ; 
var separator = 210 ;
var width = size*2.sqrt*0.9 ;
var partition = 10 ;
var lineNumber = 11 ;
var organization ;
var step = 10 ;
var staves = 4 ;

n = NB.new("testN", a3[0], a3[1], true, "pdf");


// random filling
// organization = Array.fill(lineNumber-1, {[1,2].choose}) ;
organization = Array.fill(4, 1).addAll([1.5,1.5]).addAll(Array.fill(4, 1)) ;


p = Page.new(n, staves, corner, separator,
	lineNumber, organization, partition, width, step)
		.createStaves ;


k = NoteWriter.new(page: p) ;


// lineNumber is the pitch def
// we have to calculate before lineNumber
i = 173 ;
Array.series(i, 36, 0.25 ).do({|it, ind|
	k.placeNote(it, ind, i) ;	}) ;


//k = NoteWriter.newFromLog("/log.arc", p) ;
//k.placeAll ;

k.createClefs ;
k.addTempo(i) ;
n.displayCode

)
