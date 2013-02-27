PraatFormant {

	var <>formant ;
	var <>xmin, <>xmax, <>nx, <>dx, <>x1, maxnFormants ;
	var <>intensityList, <>freqList, <>bwList ; // 3 correlated lists for storing formant data
	var <>pitchList, <>pitchDict, <>pitchFilter, <>pitchKeys ;


	importFormant { arg formantFileName ;
		var header = [], line, arr = [] ;
		var intensity, nF, couples, i ;
		var frameFreq = [], frameBw = [] ;
		formantFileName = formantFileName ? String.scDir++"/sounds/praatTmp.formant" ;
		formantFileName = if ( formantFileName[0]==$/, { formantFileName }, 
									{ String.scDir++"/"++formantFileName }) ;
		
		formant = File.new(formantFileName, "r") ;			9.do({arg i; header = header.add(formant.getLine); }) ; // eliminate header info
		header.postln ;
		# xmin, xmax, nx, dx, x1, maxnFormants = header[3..].asFloat ;
		line = formant.getLine.asFloat ;
		//arr = [line] ;
		while({line.notNil}, {arr = arr.add(line.asFloat); line = formant.getLine; }) ;
		arr.postln;
		i = 0 ;
		while {i != arr.size} { 
							intensityList = intensityList.add(arr[i]) ; 
							nF = arr[i+1].asInt; 
							couples = arr[i+2..i+2+(nF*2)-1] ;
							forBy(0, couples.size-1, 2, 
								{ arg i; 
								frameFreq = frameFreq.add(couples[i]) ;
								frameBw = frameBw.add(couples[i+1]) ;
								 }
								) ;
							freqList = freqList.add(frameFreq) ;
							bwList = bwList.add(frameBw) ;
							i = (i+2+(nF*2)); 
							} ;
					
		formant.close ;
		} 




		
	jPaint { arg stepX, stepY, formantList ;
		var index, window ;
		var maxFreq = freqList.flat.sort.reverse[0];
		var newArr = freqList/maxFreq*100; // (0-100)
		var chosen = [] ;
		stepX = stepX ? 3 ;
		stepY = stepY ? stepX ;
		formantList = formantList ? nil ;
		window = JSCWindow( "Formant", 
			Rect(100,100, freqList.size*stepX, stepY*100), resizable: false
					);
		window.view.background = Color.white;
		if (formantList == nil, {
		window.drawHook = {
			newArr.do({ arg y, n ; // using normalized
				y.do({ arg item, m ;
					JPen.color = Color.new(0.0, 0.0, 0.0);
					JPen.fillRect( Rect( n*stepX, stepY*(100-item), stepX, stepY ));
					});			
				});
			};
		}, 
		{	
		window.drawHook = {
			newArr.do({ arg y, n ; // using normalized
				chosen = [] ;
				formantList.do({ arg f ; if (y[f] != nil, {chosen = chosen.add(y[f])})});
				chosen.postln;
				chosen.do({ arg item, m ;
					JPen.color = Color.new(0.0, 0.0, 0.0);
					JPen.fillRect( Rect( n*stepX, stepY*(100-item), stepX, stepY ));
				});
			})
			
		};
		});	
		window.front;
		}
				
}



