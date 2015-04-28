FormantCharter {    
   
   var 	<>formantDict, <>directory, <>fileName, <>formantChartFile, <>step,
   		<>formantDataFile ;
   
   
   *new { arg formantDict, name ;
		^super.new.initF(formantDict, name) ;
	 }
   
   
	initF { arg aFormantDict, name, aDirectory = "/musica/antigone/formantChart/", suffix ="mp",                     aFileName = format("formantChart_%.%", name, suffix), aStep = 0.006 ;
        formantDict = aFormantDict ;                     directory = aDirectory ;        fileName = aFileName ;        formantChartFile = File.new(directory++fileName, "w") ;        step = aStep ;
        formantDataFile = File.new(directory++fileName.split($.)[0]++"Data.txt", "w") ;	}            	createHeader {		var header = "\\setuppapersize[A4][A4]         \\setupcolors[state=start]\\starttext \\startuseMPgraphic\{formantChart\}         " ;		formantChartFile.write(header)        }
        

    
	createHeaderMP {
	// for MP
		var header = "
input mp-tool ;
input mp-spec ;

beginfig(1) ;  " ;
		formantChartFile.write(header)
        }

        	createClosure {        var tail = " \\stopuseMPgraphic  \\useMPgraphic{formantChart}  \\stoptext " ;         formantChartFile.write(tail)	}


	createClosureMP {
        var tail = "
endfig ;
end.
" ; 
        formantChartFile.write(tail)
	}

	drawFormantSpace { arg x = 1500, y = 3500 ;        var fsX = x*step ;        var fsY = (y-500)*step ;        var spaceString =  "   drawoptions(withpen pencircle scaled .25pt withcolor .5white) ;   draw hlingrid(0, 10, 1, 21cm, 9cm) ; draw vlingrid(0, 10, 1, 9cm, 21cm) ; " ;        formantChartFile.write(spaceString)    }
    
    
    	drawFormantSpace2 { arg x = 1500, y = 3500 ;
        var fsX = x*step ;
        var fsY = (y-500)*step ;

        var spaceString =  format( "   
path s ;
s := fullsquare xscaled %cm yscaled %cm shifted (%cm,%cm) ;
draw s withpen pencircle scaled 0.25pt withcolor .5white ;
", fsX, fsY, fsX*0.5, fsY*0.5 );
        formantChartFile.write(spaceString)
    }
    
        plotFormant { arg vocal, vocalRange, i ;                var color = "(0.2, 0.25, 0.6)";
                //was: format("(%, %, %)", i*0.05, 0.25, 0.6 ) ;                var oX = vocalRange[0]*step ;                var oY = (vocalRange[2]-500)*step ;                var w  = (vocalRange[1] - vocalRange[0]) * step ;                var h  = (vocalRange[3] - vocalRange[2]) * step ;                var formantString = format ("  path s ;s := fullsquare xscaled %cm yscaled %cm shifted (%cm,%cm) smoothed .25cm ;draw s withpen pencircle scaled 0.5mm withcolor %;externalfigure \"ipa/%.pdf\"      xyscaled 10        shifted (%cm, %cm) ;", w, h, oX+(w*0.5), oY+(h*0.5), color,       vocal,  oX+(w*0.5)-0.255, oY+(h*0.5)-0.25) ;                ^formantString	}


// doesn't work
    plotFormantLOG { arg vocal, vocalRange, i ;
                var color = format("(%, %, %)", i*0.05, 0.25, 0.6 ) ;
                var oX = vocalRange[0].log ;//-5.25*7 ;
                var oY = vocalRange[2].log ;//-6.65*6;
                var w  = vocalRange[1].log ;//-5.25*7 ;
                var h  = vocalRange[3].log ;//-6.65*6 ;
                var formantString = format ("  
path s ;
s := (%,%)--(%,%)--(%,%)--(%,%)--cycle ;
draw s withpen pencircle scaled 0.5mm withcolor %;
externalfigure \"ipa/%.pdf\"
      xyscaled (0.35cm, 0.35cm)  
      shifted (%cm, %cm) ;
", oX, oY, w, oY, w, h, oX, h, color,
       vocal,  (oX+w)*0.5, (oY+h)*0.5).replace("-inf", "0") ;
                ^formantString
	}


    plotFormantChart {
            formantDict.do({ arg vocal, i ;
            		var vocalRange = formantDict.findKeyForValue(vocal) ;
            		var formantString = this.plotFormant(vocal, vocalRange, i) ;
				formantChartFile.write(formantString) ;
            }) ;
            	               	}

	
	plotVocoid { arg f1, f2 ;
	// formant1 and formant2 must NOT be clustered in items and occurrences
		var vocoidString ;	
		formantDict.do({ arg vocal ;
			var vocalRange = formantDict.findKeyForValue(vocal) ;
			var xMin, xMax, yMin, yMax ;
			# xMin, xMax, yMin, yMax = vocalRange ;
			if ( f1.inclusivelyBetween(xMin, xMax).and(f2.inclusivelyBetween( yMin, yMax)), 
					{ vocoidString = format("drawdot (%cm, %cm)withpen pencircle scaled .1cm withcolor 0.8red ;\n", f1*step, (f2-500)*step) }) ;  
				
		}) ;
		formantChartFile.write(vocoidString) ;
		formantDataFile.write(format("% %\n", f1, f2))
	} 


	closeFormantChartFile {
		this.createClosure ;	
		formantChartFile.close ;
		formantDataFile.close ;
	}
	

	closeFormantChartFileMP {
		this.createClosureMP ;	
		formantChartFile.close ;
		formantDataFile.close ;
	}

}