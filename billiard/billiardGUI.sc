BilliardGUI {

	var <>billiardGenerator, <>step ;
	var <>w, <>ballArray, <>radius, <>radiusGui ;
	
	*new {Ê arg billiardGenerator, step = 400, radius = 0.95 ;
		^super.new.initBG(billiardGenerator, step, radius)Ê
	}

	initBG { arg aBilliardGenerator, aStep, aRadius ;
		billiardGenerator = aBilliardGenerator ;
		billiardGenerator.addDependant(this) ;
		step = aStep ;
		radius = aRadius ;
		this.createGUI ;
	}


	createGUI {
		w = GUI.window.new("Tabula ex-cambio: a Stock Exchange Billiard", Rect.new(0, 100, 2*step, step)) ;
		w.view.background_(Color(0.9, 0.9, 0.9)) ;
		w.onClose_({ billiardGenerator.removeDependant(this) }) ;
		radiusGui = 	GUI.staticText.new
			(w, 
			Rect(billiardGenerator.state[0][0]*step-(radius*step*0.5), 
			billiardGenerator.state[0][1]*step-(radius*step*0.5), radius*step, radius*step)
			)
			.background_(Color.new255(230, 230, 230)) ;
		billiardGenerator.state.do({ arg item, index ;
			ballArray = ballArray.add(
				GUI.staticText.new(w, Rect(item[0]*step-10, item[1]*step-10, 20, 20))
					.string_((index+1).asString)
					.align_(\center)
					.stringColor_(Color(1, 1, 1))
					.background_(Color.hsv(index/billiardGenerator.state.size, 1, 1))
			)	
		}) ;
		w.front	
	}

	paint {
		billiardGenerator.state.do({ arg item, index ;
			ballArray[index].bounds_(Rect(item[0]*step, item[1]*step, 20, 20))
		}) ;
		if ( billiardGenerator.listener != 0 )
				{ radiusGui.background_(Color.new255(100, 100, 100)) ;
					radiusGui.bounds_(
				Rect(billiardGenerator.state[billiardGenerator.listener-1][0]*step-(radius*step*0.5), 
			billiardGenerator.state[billiardGenerator.listener-1][1]*step-(radius*step*0.5), radius*step, radius*step)
				)
				}
				{ radiusGui.background_(Color.new255(230, 230, 230)) } ;
	}

	update { arg theChanged, theChanger, more ;
		case { more[0] == \state }
				{ this.paint }
	}

}