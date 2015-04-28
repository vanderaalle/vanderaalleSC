/*

PsPlotter generate a postscript/pdf file with a plot of the passed data
data can be any ArrayedCollection, Buffer, Envelope, audio Function,
Signal, Wavetable

Andrea Valle scripsit 19/02/2013
*/

PsPlotter {

	var <>str ; // the string
	var <>path ; // where to write it
	var <>min, <>max ; // data range to be plot
	var <>width, <>height ; // fig dimensions in points
	var <>xEvery, <>yEvery, <>frame ; // x and y grid, frame in pt
	var <>yGridOn, <>xGridOn ; // plot grid?
	var <>gridWidth, <>frameWidth, <>curveWidth ; // stroke widths
	var <>gridCol, <>frameCol, <>curveCol ; // stroke widths
	var <>speckleWidth, <>speckleCol ;
	var <>barWidth, <>barCol ;
	var <>data, <>min, <>max ;
	var <>continuousOn, <>discreteOn, <>barOn ;
	var <>fontName, <>fontSize, <>fontCol ;
	var <>yLabelOn, <>xLabelOn ;
	var <>xLabMultiplier, <>yLabMultiplier ;
	var <>xLabRound, <>yLabRound ;
	var <>xLabStart, <>yLabStart ;
	var <>xLabEvery, <>yLabEvery ;
	var <>samplePoints ; // number of point for resampling
	var <>duration ; // duration to be calculated for functions
	var <>zero, <>zeroWidth ;
	var <>name, <>ext ;

	// first we assume data is an array
	*new { arg
		data, path, min, max, samplePoints = 400, duration = 0.01,
		width = 400, height = 200,
		xEvery = 25, yEvery = 0.4, frame = 30,
		yGridOn = true, xGridOn = true,
		gridWidth = 0.2, frameWidth = 1,
		curveWidth = 0.5, speckleWidth = 1, barWidth = 1,
		gridCol = [0.5, 0.5, 0.5],
		frameCol = [0,0,0], curveCol = [0,0,0],
		speckleCol = [0,0,0],
		barCol = [0,0,0],
		continuousOn = true, discreteOn = true, barOn = true,
		fontName = "Courier", fontSize = 8, fontCol = [0,0,0],
		yLabelOn = true, xLabelOn = true,
		xLabMultiplier = 1, yLabMultiplier = 1,
		xLabRound = 0.01, yLabRound = 0.01,
		xLabStart = -20, yLabStart = -25,
		xLabEvery = 2, yLabEvery = 2,
		zeroWidth = 0.75 ;
		// this is ugly
		^super.new.initPsPlotter(
			data, path, min, max, samplePoints, duration,
			width, height, xEvery, yEvery, frame, yGridOn, xGridOn,
			gridWidth, frameWidth, curveWidth, speckleWidth, barWidth,
			gridCol, frameCol, curveCol, speckleCol, barCol,
			continuousOn, discreteOn, barOn,fontName, fontSize, fontCol,
			yLabelOn, xLabelOn,	xLabMultiplier, yLabMultiplier,
			xLabRound , yLabRound, xLabStart, yLabStart, xLabEvery, yLabEvery,
			zeroWidth)
	}

	initPsPlotter { arg
		// this can't be looked at
			adata, apath, amin, amax, asamplePoints, aduration,
			awidth, aheight, axEvery, ayEvery, aframe, ayGridOn, axGridOn,
			agridWidth, aframeWidth, acurveWidth, aspeckleWidth, abarWidth,
			agridCol, aframeCol, acurveCol, aspeckleCol, abarCol,
			acontinuousOn, adiscreteOn, abarOn, afontName, afontSize, afontCol,
			ayLabelOn, axLabelOn,	axLabMultiplier, ayLabMultiplier,
			axLabRound, ayLabRound, axLabStart, ayLabStart,
		axLabEvery, ayLabEvery, azeroWidth;
		var arr ;
		# data, path, min, max, samplePoints, duration,
			width, height, xEvery, yEvery, frame, yGridOn, xGridOn,
			gridWidth, frameWidth, curveWidth, speckleWidth, barWidth,
			gridCol, frameCol, curveCol, speckleCol, barCol,
			continuousOn, discreteOn, barOn,fontName, fontSize, fontCol,
			yLabelOn, xLabelOn,	xLabMultiplier, yLabMultiplier,
			xLabRound , yLabRound, xLabStart, yLabStart, xLabEvery, yLabEvery,
			zeroWidth =
[adata, apath, amin, amax, asamplePoints, aduration,
			awidth, aheight, axEvery, ayEvery, aframe, ayGridOn, axGridOn,
			agridWidth, aframeWidth, acurveWidth, aspeckleWidth, abarWidth,
			agridCol, aframeCol, acurveCol, aspeckleCol, abarCol,
			acontinuousOn, adiscreteOn, abarOn, afontName, afontSize, afontCol,
			ayLabelOn, axLabelOn,	axLabMultiplier, ayLabMultiplier,
			axLabRound, ayLabRound, axLabStart, ayLabStart,
		axLabEvery, ayLabEvery, azeroWidth];
		#name, ext = path.split($.) ;
		//data = this.checkData(aData);
		{
		data = case
		{ data.class.superclasses.includes(ArrayedCollection)}
		{ data }
		{ data.class == Env }
		{ data.asSignal(samplePoints)}
		{ data.class == Wavetable }
		{ data.asSignal(samplePoints)}
		{ data.class == SoundFile }
		{ data.openRead(data.path);
				arr = Signal.newClear(data.numFrames) ;
				data.readData(arr);
				xLabMultiplier = arr.size/data.sampleRate/samplePoints ;
				arr = arr.resamp1(samplePoints);
				arr
		}
		{ data.class == Buffer }
		{ data.loadToFloatArray(action: { |array, buf|
				xLabMultiplier = data.numFrames/data.sampleRate/samplePoints ;
				arr = array.resamp1(samplePoints);
				});
				arr
		}
		{ data.class == Function }
		{ data.loadToFloatArray(duration, Server.local, action: { |array|
				xLabMultiplier = array.size/Server.local.sampleRate/samplePoints ;
				data = array.resamp1(samplePoints);
				});
				data
		} ;
			//Server.local.sync ; // why not?
			1.wait ; // pretty empirical
		min = if (min.isNil) {data.minItem}{min} ;
		max = if (max.isNil) {data.maxItem}{max} ;
		data = data.linlin(min, max, 0, height) ; // scaled arr
		zero = 0.linlin(min, max, 0, height) ; // needed for bar and line
		this.plot ;
		this.write
					}.fork ;

	}

// can't sync it, alas
/*
	checkData { arg data ;
		case
		{ data.class.superclasses.includes(ArrayedCollection)}
		{ ^data}
		{ data.class == Env }
		{ ^data.asSignal(samplePoints)}
		{ data.class == Wavetable }
		{ ^data.asSignal(samplePoints)}
		{ data.class == Buffer }
		{ // works with mono
			data.loadToFloatArray(action: { |array, buf|
			data = array}) ;
			^data
		}
	}
*/

	setWidth {
		str = str++"% document size\n";
		str = str++"<< /PageSize [X Y] >> setpagedevice\n"
			.replace("X", width+(frame*2))
			.replace("Y", height+(frame*2))
	}

	setFont {
		str = str++"% font setting grid\n";
		str = str++"/"++fontName ++" findfont "++ fontSize.asString++" scalefont setfont\n" ;
	}

	drawFrame {
		str = str++"% frame\n";
		str = str++frame.asString++" "++frame.asString++" translate\n" ;
		str = str++frameWidth.asString++" setlinewidth\n" ;
		frameCol.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		str = str++"newpath\n0 0 moveto\n";
		str = str++width.asString++" "+ 0.asString++" lineto\n";
		str = str++width.asString++" "+ height.asString++" lineto\n";
		str = str++0.asString++" "+ height.asString++" lineto\n";
		str = str++0.asString++" "+ 0.asString++" lineto\n";
		str = str++"closepath stroke\n" ;
		if ((min < 0) && (max > 0)) {this.drawZeroLine}
	}

	drawZeroLine {
		str = str++"% zero line\n";
		str = str++zeroWidth.asString++" setlinewidth\n" ;
		str = str++"newpath\n";
		str = str++3.neg.asString++" "++zero.asString++" moveto\n" ;
		str = str++width.asString++" "++zero.asString++" lineto\n" ;
		str = str++"closepath stroke\n" ;
	}

	drawYGrid {
		var num, y ;
		str = str++"% horizontal grid\n";
		str = str++gridWidth.asString++" setlinewidth\n" ;
		gridCol.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		str = str++"newpath\n";
		num = ((max-min)/yEvery).trunc ;
		num.do{|i|
			y = i*(height/num);
			str = str++"-3 "++ y.asString ++" moveto\n" ;
			str = str++width.asString++" "++y.asString++" lineto\n"
		} ;
	str = str++"closepath stroke\n" ;
	}

	drawYLabels {
		var num, y, val ;
		str = str++"% horizontal labels\n";
		fontCol.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		num = ((max-min)/yEvery).trunc ;
		(num+1).do{|i|
			if (i%yLabEvery == 0) {
				y = i*(height/num);
				val = (((max-min)/num*i+min)*yLabMultiplier).round(yLabRound) ;
				str = str++yLabStart.asString++" "++ (y-(fontSize*0.25)).asString ++" moveto\n" ;
				str = str++"("++val.asString++") show\n"
				}
		} ;
	}


	drawXGrid {
		var num, x ;
		str = str++"% vertical grid\n";
		str = str++gridWidth.asString++" setlinewidth\n" ;
		gridCol.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		str = str++"newpath\n";
		num = (data.size/xEvery).trunc ;
		num.do{|i|
			x = i*(width/num);
			str = str++ x.asString ++" -3 moveto\n" ;
			str = str++x.asString++" "++height.asString++" lineto\n"
		} ;
		str = str++"closepath stroke\n" ;
	}


	drawXLabels {
		var num, x, val ;
		str = str++"% vertical labels\n";
		fontCol.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		num = (data.size/xEvery).trunc ;
		(num+1).do{|i|
			if (i%xLabEvery == 0) {
				x = i*(width/num);
				val = ((i*xEvery)*xLabMultiplier).round(xLabRound) ;
				str = str++ (x-(val.asString.size*fontSize*0.25)).asString ++" "++xLabStart.asString++" moveto\n" ;
				str = str++"("++val.asString++") show\n"
			}
		} ;
	}

	drawContinuous {
		// curve
		var xStep = width/data.size ;
		str = str++"% curve\n" ;
		str = str++curveWidth.asString++" setlinewidth\n" ;
		curveCol.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		str = str++"newpath\n" ;
		str = str++0.asString++" "++(data[0])++" moveto\n" ;
		data[1..].do{|i, j|
			str = str++((j+1)*xStep).asString++" "++i.asString++" lineto\n"
		} ;
		str = str++"stroke\n" ;
	}

	drawDiscrete {
		// discrete
		var xStep = width/data.size ;
		str = str++"% discrete\n" ;
		str = str++speckleWidth.asString++" setlinewidth\n" ;
		speckleCol.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		data.do{|i, j|
			str = str++((j)*xStep).asString++" "++i.asString++" "++speckleWidth.asString++" 0 360 arc fill\n"
		} ;
	}


	drawBar {
		// curve
		var xStep = width/data.size ;
		str = str++"% bar\n" ;
		str = str++barWidth.asString++" setlinewidth\n" ;
		barCol.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		str = str++"newpath\n" ;
		data.do{|i, j|
			str = str++((j)*xStep).asString++" "++zero.asString++" moveto\n";
			str = str++((j)*xStep).asString++" "++i.asString++" lineto stroke\n"
		} ;

	}

	// total
	plot {
		this.setWidth ;
		this.setFont ;
		this.drawFrame ;
		if (xGridOn) { this.drawXGrid } ;
		if (xLabelOn) { this.drawXLabels };
		if (yGridOn) { this.drawYGrid } ;
		if (yLabelOn) { this.drawYLabels };
		if (continuousOn){ this.drawContinuous } ;
		if (barOn){ this.drawBar } ;
		if (discreteOn){ this.drawDiscrete } ;
	}

	write {
		var file = File(name++".ps", "w") ;
		file.write(str++"showpage\n") ;
		file.close ;
		if (ext.asSymbol == \pdf) {
			("pstopdf"+name++".ps").unixCmd{
				("rm"+name++".ps").unixCmd
			}
		}
	}

}


/*


(
// alternatives:
a = Array.fill(500, {|i| rrand(-1.0, 1.0)}) ;
a = Env.perc ;
// we boot the server
s.boot ;

a = Buffer.read(s,"/musica/audioRumentario/bells/chappa1.wav") ;
a = {SinOsc.ar} ;
a = SoundFile("/musica/audioRumentario/bells/chappa1.wav") ;

// plotting with some options
p = PsPlotter(a, "/Users/andrea/Desktop/untitled.pdf", min:-1, max:1, yEvery: 0.1, xEvery: 10, xLabEvery:10, barOn:false, speckleCol:[1,0,0], curveCol:[0,0.5, 0]) ;

)

*/
