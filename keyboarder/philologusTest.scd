(
a = Philologus.new("Lo sciacallo sciancato sotto un antico scialle

color sciacallo allunga il muso nell'aiola

strappandola coi denti mi procura una viola

poi col suo passo scialbo mi accompagna al cancello

rosso di rose e roso di ruggine e d'amor.
"
) ;

a.maxRank ;
a.items ;
a.size ;
a.numItems ;
a.makeTable ;
a.makeScore ;

)



(
// the waste land
a = Philologus.new(File("/wasteLand.txt", "r")) ;
a.makeTable(range:false) ;
a.makeScore(time: false) ;
)
