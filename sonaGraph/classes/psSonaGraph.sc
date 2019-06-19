/*

PsSonaGraph generates a postscript/pdf file with a plot
from amp data structure of SonaGraph

Andrea Valle scripsit 15/11/2016
*/

PsSonaGraph {

	var <>str ; // the string
	var <>path ; // where to write it
	var <>width, <>height ; // fig dimensions in points
	var <>xEvery,  <>frame ; // x and y grid, frame in pt
	var <>yGridOn, <>xGridOn ; // plot grid?
	var <>gridWidth, <>frameWidth, <>curveWidth ; // stroke widths
	var <>gridCol, <>frameCol, <>curveCol ; // stroke widths
	var <>speckleWidth, <>speckleCol ;
	var <>barWidth, <>barCol ;
	var <>data, <>thresh, <>rate;
	var <>fontName, <>fontSize, <>fontCol ;
	var <>yLabelOn, <>xLabelOn ;
	var <>xLabRound, <>yLabRound ;
	var <>xLabMul;
	var <>xLabStart, <>yLabStart ;
	var <>xLabEvery, <>yLabEvery ;
	var <>samplePoints ; // number of point for resampling
	var <>duration ; // duration to be calculated for functions
	var <>zero, <>zeroWidth ;
	var <>name, <>ext ;
	var <>cellType ;

	// first we assume data is an array
	*new { arg
		data, path, thresh= -60, rate = 10,
		width = 400, height = 200,
		xEvery = 25, frame = 30,
		yGridOn = true, xGridOn = true,
		gridWidth = 0.2, frameWidth = 1,
		gridCol = [0.5, 0.5, 0.5],
		frameCol = [0,0,0], curveCol = [0,0,0],
		fontName = "Courier", fontSize = 6, fontCol = [0,0,0],
		yLabelOn = true, xLabelOn = true,
		xLabRound = 0.01, yLabRound = 0.01,
		xLabMul = 1,
		xLabStart = -10, yLabStart = -25,
		xLabEvery = 2, yLabEvery = 2, cellType = \oval ;
		// this is ugly
		^super.new.initPsPlotter(
			data, path, thresh,rate,
			width, height, xEvery, frame, yGridOn, xGridOn,
			gridWidth, frameWidth,
			gridCol, frameCol,
			fontName, fontSize, fontCol,
			yLabelOn, xLabelOn,
			xLabRound , yLabRound,
			xLabMul,
			xLabStart, yLabStart, xLabEvery, yLabEvery,
			cellType
		)
	}

	initPsPlotter { arg
		// this can't be looked at
		adata, apath, athresh, arate,
		awidth, aheight, axEvery, aframe, ayGridOn, axGridOn,
		agridWidth, aframeWidth,
		agridCol, aframeCol,
		afontName, afontSize, afontCol,
		ayLabelOn, axLabelOn,
		axLabRound, ayLabRound,
		axLabMul,
		axLabStart, ayLabStart,
		axLabEvery, ayLabEvery,
		aCellType;
		var arr ;
		# data, path, thresh, rate,
		width, height, xEvery, frame, yGridOn, xGridOn,
		gridWidth, frameWidth,
		gridCol, frameCol,
		fontName, fontSize, fontCol,
		yLabelOn, xLabelOn,
		xLabRound , yLabRound,
		xLabMul,
		xLabStart, yLabStart, xLabEvery, yLabEvery,
		cellType
		=
		[adata, apath, athresh,arate,
			awidth, aheight, axEvery, aframe, ayGridOn, axGridOn,
			agridWidth, aframeWidth,
			agridCol, aframeCol,
			afontName, afontSize, afontCol,
			ayLabelOn, axLabelOn,
			axLabRound, ayLabRound,
			axLabMul,
			axLabStart, ayLabStart,
			axLabEvery, ayLabEvery,
			aCellType
		];
		#name, ext = path.split($.) ;
		str = "" ;
		this.plot ;
		this.write
	}

	cell {|x,y,width,height, gray|
		if (cellType == \oval){
			^this.circleCell(x, y, width, height, gray)}
		{ ^this.squareCell(x, y, width, height, gray)}
	}

	squareCell {|x, y, width, height, gray|
		var cell = "newpath
X Y moveto
0 height rlineto
width 0 rlineto
0 -height rlineto
closepath
GR setgray
fill
"
		.replace("X", x).replace("Y", y)
		.replace("height", height).replace("width", width)
		.replace("GR", gray) ;
		^cell
	}

	circleCell {|x,y,width, height, gray|
		var ratio = height/width ;
		var cell = "gsave
ratio 1 scale
X Y width 0 360 arc
GR setgray
fill
grestore
"
		.replace("X", x*ratio.reciprocal).replace("Y", y)
		.replace("width", width*0.5).replace("ratio", ratio)
		.replace("GR", gray) ;
		^cell
	}

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
	}


	drawYGrid {
		var step = height/88 ;
		str = str++"% horizontal grid\n";
		str = str++gridWidth.asString++" setlinewidth\n" ;
		gridCol.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		str = str++"newpath\n";
		88.do{|i|
			if (i%12 == 0){
				str = str++"-3 "++ (height-(step*(i+1))).asString ++" moveto\n" ;
				str = str++(width+2).asString++" "++(height-(step*(i+1))).asString++" lineto\n" ;
			}
		} ;
		str = str++"closepath stroke\n" ;
	}

	drawYLabels {
		var step = height/88 ;
		var j = 0 ;
		var frq = Array.series(8, 21+88-1, -12).midicps.round(0.01) ;
		str = str++"% horizontal labels\n";
		fontCol.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		str = str++(-14).asString++" "++(height+fontSize).asString++" moveto\n" ;
		str = str ++"(Oct) show\n" ;
		str = str++(width+2).asString++" "++(height+fontSize).asString++" moveto\n" ;
		str = str ++"(Hz) show\n" ;
		str = str++"newpath\n";
		88.do{|i|
			if (i%12 == 0){
				str = str++"-8 "++ (height-(step*(i+1))).asString ++" moveto\n" ;
				str = str++"(@) show\n".replace("@", (9-j).asString) ;
				str = str++(width+2).asString++" "++(height-(step*(i+1))).asString ++" moveto\n" ;
				str = str++"(@) show\n".replace("@", (frq[j]).asString) ;

				j = j+1
			}
		} ;
		str = str++"closepath stroke\n" ;
	}

	// maybe some glitch in time calculation
	drawXGrid {
		var num, x ;
		str = str++"% vertical grid\n";
		str = str++gridWidth.asString++" setlinewidth\n" ;
		gridCol.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		str = str++"newpath\n";
		num = (data.size/xEvery).trunc ;
		(num+1).do{|i|
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
		str = str++(width*2/5).asString++" "++(xLabStart*2).asString++" moveto\n" ;
		str = str ++"(time (seconds)) show\n" ;
		num = (data.size/xEvery).trunc ;
		(num+1).do{|i|
			x = i*(width/num);
			val = i*xEvery ;
			//.asTimeString;
			str = str++ (x-(val.asString.size*0.5*fontSize*0.25)).asString ++" "++xLabStart.asString++" moveto\n" ;
			str = str++"("++(i*xLabMul)++") show\n"

		} ;
	}

	drawSonagraph {
		// curve
		var xStep = width/data.size ;
		var yStep = height/data[0].size ;
		var gray ;
		data.do{|ampArr,i|
			ampArr.do{|p, j|
				if (p > thresh){
					gray = p.linlin(thresh, -10, 0.9, 0) ;
					str = str++
					this.cell(i*xStep, j*yStep, xStep, yStep, gray)
					//this.circleCell(i*xStep, j*yStep, xStep, yStep, gray)
				}
			}
		} ;
	}

	// total
	plot {
		this.setWidth ;
		this.setFont ;
		this.drawFrame ;
		this.drawSonagraph ;
		if (xGridOn) { this.drawXGrid } ;
		if (xLabelOn) { this.drawXLabels };
		if (yGridOn) { this.drawYGrid } ;
		if (yLabelOn) { this.drawYLabels };
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
a = SonaGraph.new;
a.readArchive("/Users/andrea/Desktop/sonaChal.log")
~data = a.amp ;

p = PsSonaGraph(~data, "/Users/andrea/Desktop/untitled.pdf", width:600, xEvery: 30, xLabEvery:2, xGridOn:true, yGridOn:true, xLabelOn:true, gridCol: [1, 0, 0], frameCol:[0.2,0.5,0.7]) ;


)
*/