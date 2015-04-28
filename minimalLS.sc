// MLSys : minimal L-System
// Built after wonderful LSys by Batuhan Bozkurt
// no branching, no context, no params, just what I need for strings
// absence of branching allows to use [] as array standard notation 

/*
// USAGE
// deterministic. Adding some spice to the sentence
a = MLSys("sempre caro mi fu quest'ermo colle", [" "->"! "])
a.applyRules; a.currentAxiom
// again
a.applyRules; a.currentAxiom

// stochastic, with uniform distro. No more so sure
a = MLSys("sempre caro mi fu quest'ermo colle", [" "->["! ","? "]])
a.applyRules; a.currentAxiom
// again
a.applyRules; a.currentAxiom

// vowel scrambling. Note the ultracompact notation for keys
a = MLSys("sempre caro mi fu quest'ermo colle", [["a", "e", "i", "o", "u"]->["a", "e", "i", "o", "u"]])
a.applyRules; a.currentAxiom
// again, going toward a unique vowel
a.applyRules; a.currentAxiom

// Note that [] means 2 different things on rule sides:
// 	- on key is: for each key
//	- on val: select randomly one val 

*/

MLSys {
	
	var <axiom, <currentAxiom, <rules ;
	var <axioms ; // used to store all rewriting process
		
	*new
	{|argAxiom, argRules|
		^super.new.init(argAxiom, argRules);
	}
	
	init
	{|argAxiom, argRules|
		axiom = argAxiom;
		currentAxiom = argAxiom;
		rules = argRules ;
		axioms = [] ;
	}

	applyRule { |rule|	
		var val ;
		var keyArr = if (rule.key.isString) // so that we can always iterate on arr
			{[rule.key]} {rule.key} ;
		// for each key in the arr
		keyArr.do{|key|
			val = if (rule.value.isString.not)
				{ rule.value.choose } //if an arr, we choose with uniform distro
				{ rule.value } ; // else we get the value directly
			currentAxiom = currentAxiom.replace(key, val) ;
			// store in the axioms arr
			// here we would collect each rule
			//axioms = axioms.add(currentAxiom)
		}
	}
	
	applyRules {|level = 1|
		level.do{
			rules.do{|rule| this.applyRule(rule) } ;
			// store in the axioms arr
			// here we collect levels
			axioms = axioms.add(currentAxiom)
		}
	}
	
}
