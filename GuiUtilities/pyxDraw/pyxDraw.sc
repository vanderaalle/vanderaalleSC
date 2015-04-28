/*
 Extension to ArrayedCollection and other (see SignalPlusGUI)
  which allows to generate vectorial pdf graphics.
  Useful for signal-like stuff. Modelled on plot/jplot.
 Writes an ASCII file using the Python language and PyX package.
 For rendering the .py file see:
 - http://www.python.org/
 - http://www.pyx.sourceforge.net/

 -> andrea valle, 11/01/07
 http://www.semiotiche.it/andrea/
*/

/*
+ ArrayedCollection {

	pyxDraw { arg fileName, yRange, xRange,
				curve, point, bar, barLine,
				// curveCol, barCol, barLineCol, pointCol,
				width, height ;

			var curveCol, barCol, barLineCol, pointLineCol, pointCol ;
			var code, range, xMin, xMax ;
			var pyxFile = File(fileName, "w") ;
			var name = fileName.split($.)[0]++".pdf" ;
			var pdfFile = if ( fileName[0]==$/, {name}, { String.scDir++"/"++name}) ;

			range = this.copyRange(0, this.size).sort ;
			xMin = range[0] ;
			xMax = range[this.size-1] ;

			// some defaults
			xRange = xRange ? [ 0, this.size ] ;
			yRange = yRange ? [ xMin, xMax ] ;
			curve = curve ? true ;
			point = point ? false ;
			bar = bar ? false ;
			barLine = barLine ? true ;
			width = width ? 8 ;
			height = height ? 5 ;


			// CIRMA colors: can be made args
			curveCol 		= "color.rgb(0.0, 0.2, 0.6)" ;// "color.gray.black" ;
			barCol  		= "color.rgb(0.7, 0.7, 0.7)" ;
			barLineCol 	= if (barLine == true,	{ "color.gray.white"  },
											{ barCol }) ;
			pointLineCol 	=  curveCol ;
			pointCol		= "color.rgb(1.0, 0.6, 0.0)" ;


			curve = if ( curve == true, {format( "graph.style.line([%]),", curveCol)},
									 	{""}) ;
			point = if ( point == true,	{format( "
		graph.style.symbol(graph.style.symbol.circle, size=0.05,
		symbolattrs=[%, deco.filled([%])])", pointLineCol, pointCol)},
									 	{""}) ;
			bar = if ( bar == true, 		{format( "graph.style.histogram(
			[%,deco.filled([%])], fillable=1),", barLineCol, barCol)}, 										{""}) ;

			pyxFile.write("from pyx import *\n") ;
			// sometimes is needed, sometimes not...Depends on laTeX et al.
			//pyxFile.write("text.set(fontmaps=\"psfonts.cmz\")\n") ;
			pyxFile.write("arr = [\n") ;
			this.do({ arg item, index ;
				pyxFile.write("["+index.asString+", "+item.asString+"],\n")
				}) ;
			pyxFile.write("\n]\n") ;

			code = format ( "
g = graph.graphxy(width=%, height=%, x=graph.axis.linear(min=%, max=%),
                  y=graph.axis.linear(min=%, max=%))
g.plot(graph.data.list(arr, x=1, y=2),
		[% % %])
g.writePDFfile(\"%\") ",
		width, height, xRange[0], xRange[1], yRange[0], yRange[1],  bar, curve, point, pdfFile
					) ;

		pyxFile.write(code) ;
		pyxFile.close ;

		^this ;
		}



	}




+ Wavetable {
	pyxDraw { arg fileName, yRange, xRange,  curve, point, bar, barLine, width, height ;
		^this.asSignal.pyDraw(fileName, yRange, xRange,  curve, point, bar, barLine, width, height);
	}
}

+ Buffer {
	pyxDraw { arg fileName, yRange, xRange,  curve, point, bar, barLine, width, height ;
		// works with mono
		this.loadToFloatArray(action: { |array, buf|
		{array.pyxDraw(fileName, yRange, xRange,  curve, point, bar, barLine, width, height) }.defer;});
	}
}



+ Function {

	pyxDraw { arg fileName, yRange, xRange,  curve, point, bar, barLine, width, height, duration  = 0.01, server ;
		this.loadToFloatArray(duration, server, { |array, buf|
			var numChan;
			// works with mono
			// numChan = buf.numChannels;
			{
				array.pyxDraw(fileName, yRange, xRange,  curve, point, bar, barLine, width, height)
			}.defer;
		})
	}


}



+Env {

	pyxDraw { arg fileName, yRange, xRange,  curve, point, bar, barLine, width, height, size = 400; 			this.asSignal(size).pyxDraw(fileName, yRange, xRange,  curve, point, bar, barLine, width, height);
	}

}



+ SoundFile{

// It is supposed you have already called openRead(path)

	pyxDraw { arg fileName, yRange, xRange,  curve, point, bar, barLine, width, height, size = 400;
	 	var rawArray = Signal.newClear(this.numFrames) ;
	 	this.readData(rawArray) ;
	 	rawArray.pyxDraw(fileName, yRange, xRange,  curve, point, bar, barLine, width, height);
	}
}
*/