

// ranges
// cetL: 27-38
// cetH: 39-50
// ser:  75-86

// piano: 21-108
// cello: 36 - 93?
// adler: sol 79 on fret
// harmonics: 80- re 74 fourth -> 98
// A-string, open: 57 (CGDA = 36 43 50 57)



SonaGraph.prepare ;
~seq = SD.readArchive("/home/andrea/musica/scores/musicaPetrosa/rimuNabo/rimuNabo.spec") ;
l = SD.eventsFromVoices(~seq)

o = SDEvGui(l).makeGui(1200, hStep:4).rate_(6.2)

6.2*60/4
z = o.newSelection ;

o.addToSelection(z, 2, 40)
o.addSelection(z) ;
o.selections[1][19]
o.u.refresh
o.checkSelections(19, 0)

o.selections = []; o.u.refresh ;

o.actualSelection = o.newSelection ; o.u.refresh ;
o.addToSelection(z, 0, 40)
o.addSelection(z) ;
o.selections[1][19]
o.u.refresh

z = o.newSelectionInRange(0, 43, 31, 64) ;
o.addSelection(z) ;
z = o.newSelectionInRange(43, 66, 31, 64) ;
o.addSelection(z) ;
z = o.newSelectionInRange(67, 91, 31, 64) ;
o.addSelection(z) ;
z = o.newSelectionInRange(225, 250, 31, 81) ;
o.addSelection(z) ;
z = o.newSelectionInRange(166, 214, 54, 81) ;
o.addSelection(z) ;


o.removeSelection(o.selections[0]) ;
o.removeSelections(o.selections[..2]) ;

m = o.mergeSelections([o.selections[0], o.selections[1], o.selections[2] ]) ;
m = o.mergeSelections([o.selections[0], o.selections[1], o.selections[2], o.selections[3], o.selections[4]  ]) ;

// merge all selections, need a copy
m = o.mergeSelections(o.selections.copy) ;
o.selections = [] ; o.u.refresh ;
o.addSelection(m) ;


// merge actual to a selection
o.addActualToSelections ;
m = o.mergeSelections([o.selections[0], o.selections.last]) ;
o.actualSelection = o.newSelection ; o.u.refresh ;

m = o.mergeSelections(o.selections[1..2]) ;
o.cleanSelections ;

o.addActualToSelection(o.selections[0])
o.removeActualFromSelection(o.selections[0])
o.removeActualFromSelection(o.selections[1])

o.actualSelection = o.newSelection ; o.u.refresh ;

o.addActualToSelections ;


o.maxExt

19.do{|i|
	z = o.newSelectionInRange(i*50, i*50+40, 31, 81) ;
o.addSelection(z) ;
}

o.selections.size
o.playSelection(o.selections[3], 225)

o.actualSelection = o.newSelection ; o.u.refresh ;

o.playSelection(o.actualSelection)

o.cursor

o.addActualToSelection(o.selections[0])

o.removeFromSelection(~piano, 227, 40)
o.removeFromSelection(~cello, 457, 57)
o.removeFromSelection(~cetH, 216, 46)

~piano = o.selections[0]
o.addActualToSelection(~piano)
~cello = o.selections[3]

o.addActualToSelection(~piano)
o.addActualToSelection(~cello)
o.addActualToSelection(~cetH)
o.addActualToSelection(~cetL)
o.addActualToSelection(~serr)
o.addActualToSelections ;

o.removeActualFromSelection(~cetL)

~serr = o.selections[4]
~cetL = o.selections[1]
~cetH = o.selections[2]

// write
(
o.writeSelection(~piano, "/home/andrea/musica/scores/selvaPetrosa/rimuNabo/piano.sel") ;
o.writeSelection(~cello, "/home/andrea/musica/scores/selvaPetrosa/rimuNabo/cello.sel") ;
o.writeSelection(~cetL, "/home/andrea/musica/scores/selvaPetrosa/rimuNabo/cetL.sel") ;
o.writeSelection(~cetH, "/home/andrea/musica/scores/selvaPetrosa/rimuNabo/cetH.sel") ;
o.writeSelection(~serr, "/home/andrea/musica/scores/selvaPetrosa/rimuNabo/serr.sel") ;
)
// clean up
// o.selections = [] ; o.u.refresh ;

// removing a collection, e.g. id 2
o.selections.removeAt(2) ; o.u.refresh ;

// read back
~piano = o.readSelection("/home/andrea/musica/scores/selvaPetrosa/rimuNabo/piano.sel")
~cello = o.readSelection("/home/andrea/musica/scores/selvaPetrosa/rimuNabo/cello.sel")
~cetL = o.readSelection("/home/andrea/musica/scores/selvaPetrosa/rimuNabo/cetL.sel")
~cetH = o.readSelection("/home/andrea/musica/scores/selvaPetrosa/rimuNabo/cetH.sel")
o.addSelections([~piano, ~cello, ~cetL, ~cetH])

o.selections.removeAt(3) ; o.u.refresh ;


f = File("/home/andrea/Documents/exp.txt", "w") ;
f.write("l = "++l.asCompileString) ; f.close
