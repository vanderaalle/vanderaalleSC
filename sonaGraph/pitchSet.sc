// by Newton Armstrong
// https://www.listarc.bham.ac.uk/lists/sc-users/msg54932.html
// a set of methods that implement post-tonal theory operations

+ SequenceableCollection {
	// PITCH CLASS METHODS
	asPCSet {
		^this.mod(12).removeDuplicates.sort;
	}
	asPitchSet {
		^PitchSet(this);
	}
	// algorithm adapted from Morris: Class Notes for Atonal Theory p.40
	normalForm {
		var pcSet, riSet, rotations, spans, span, indices, normal, i=0;
		var firstPCs, index;
		pcSet = this.asPCSet;
		if (pcSet.size == 1) { ^[0] };
		riSet = pcSet.inversion.reverse;
		rotations = [];
		pcSet.do { |each, i| rotations = rotations.add(pcSet.rotate(i.neg)) };
		riSet.do { |each, i| rotations = rotations.add(riSet.rotate(i.neg)) };
		rotations = rotations.removeDuplicates;
		while ({ (rotations.size > 1) && (span != 0) }, {
			i = i + 1;
			spans = rotations.collect { |item| (item[pcSet.size - i] - item[0]) % 12 };
			span = spans.minItem;
			indices = spans.indicesOfEqual(span);
			rotations = rotations[indices];
			if (span == 0) {
				firstPCs = rotations.collect { |item| item[0] };
				index = firstPCs.indexOf(firstPCs.minItem);
				rotations = rotations[index];
			};
		});
		normal = rotations.flat;
		normal = (normal - normal[0]) % 12;
		^normal;
	}
	// inversion in pitch-class space
	inversion {
		^(12 - this.asPCSet % 12);
	}
	intervalVector {
		var list, intervals = [], vector;
		list = this.normalForm;
		list.do { |item, i|
			(list.size - (i + 1)).do { |el, j|
				intervals = intervals.add((item - list[j + i + 1]).abs.fold2(6));
			}
		};
		intervals = intervals.sort;
		vector = (1 .. 6).collect { |item, i| intervals.occurrencesOf(item) };
		^vector;
	}
	// interval vector similarity between two PCSets
	// after Rogers: A Geometric Approach to PCset Similarity (PNM 37 (1999))
	/*
	similarity { |aPCSet|
		var a, b, counts, similarity;
		a = this.intervalVector;
		b = aPCSet.intervalVector;
		counts = Array.newClear(a.size);
		a.size.do { |i|
			counts[i] = ((a[i] / a.sum) - (b[i] / b.sum)).abs;
		};
		similarity = (2 - counts.sum) * 0.5; // normalize to 0..1
		^similarity;
	}
	*/
	// ranks all PCSets by degree of similarity to receiver
	// 1 = maximally similar, 0 = maximally dissimilar
	/*
	similarityRank {
		var pairs, sim;
		pairs = [];
		SetClass.lib.keysValuesDo { |key, val|
			if (key != '1-1') {
				sim = this.similarity(val);
				pairs = pairs.add([sim.round(0.0001), val]);
			};
		};
		^pairs.sort { |a, b| a[0] > b[0] };
	}
	*/
	indexVector {
		var pcSet, pairs, vector, n;
		pcSet = this.asPCSet;
		pairs = [];
		vector = 0 ! 12;
		pcSet.do { |elem, j|
			pcSet[j .. (pcSet.size - 1)].do { |elem, i|
				pairs = pairs.add([pcSet[j], elem]);
			}
		};
		pairs.do { |item|
			n = item.sum % 12;
			if (item[0] == item[1]) {
				vector[n] = vector[n] + 1;
			} {
				vector[n] = vector[n] + 2;
			}
		};
		^vector;
	}
	// rename: invariantTranspositions? or tIntersection
	commonToneTranspositions { |func|
		var pcSet, intervalVector, out;
		pcSet = this.asPCSet;
		intervalVector = this.intervalVector;
		out = [];
		// tritone is self-symmetrical
		if (intervalVector[5] > 0) { intervalVector[5] = intervalVector[5] * 2 };
		intervalVector.mirror.do { |item, i|
			if (func.isNil) {
				if (item > 0) { out = out.add(pcSet + i + 1 % 12) };
			} {
				if (func.(item)) { out = out.add(pcSet + i + 1 % 12) };
			}
		};
		^out;
	}
	// rename: invariantInversions? or iIntersection
	commonToneInversions { |func|
		var pcSet, indexVector, out;
		pcSet = this.asPCSet;
		indexVector = this.indexVector;
		out = [];
		indexVector.do { |item, i|
			if (func.isNil) {
				if (item > 0) { out = out.add(12 - pcSet + i % 12) };
			} {
				if (func.(item)) { out = out.add(12 - pcSet + i % 12) };
			}
		};
		^out;
	}
	allTranspositions {
		var pcSet = this.asPCSet;
		^(0..11).collect { |i| pcSet + i % 12 };
	}
	allInversions {
		^this.allTranspositions.collect { |i| 12 - i % 12 };
	}
	uniquePCs {
		^(this % 12).removeDuplicates;
	}
	uniquePCSets {
		var list, out, bool;
		list = this.allTranspositions ++ this.allInversions;
		out = [this.normalForm];
		list.do { |item|
			bool = [];
			out.do { |each | if (item.includesAll(each)) { bool = bool.add(true) } };
			if (bool.isEmpty) { out = out.add(item) };
		};
		^out;
	}
	// deprecate!!
	setClassName { this.setClass }
	setClass {
		var normalForm = this.normalForm;
		SetClass.lib.keysValuesDo { |key, val| if (val == normalForm) { ^key } };
	}
	complementarySetClass {
		^(0..11).symmetricDifference(this.normalForm).normalForm;
	}
	zRelation {
		^SetClass(SetClass.zRelations[this.setClassName]);
	}
	// normal form (set class) subsets
	subsets {
		var normalForm, allSetClasses, out;
		normalForm = this.normalForm;
		allSetClasses = SetClass.setClasses;
		out = [];
		allSetClasses.do { |setClass|
			if (normalForm.includesAll(setClass)) { out = out.add(setClass) };
		};
		^out;
	}
	// normal form (set class) supersets
	supersets {
		var normalForm, allSetClasses, out;
		normalForm = this.normalForm;
		allSetClasses = SetClass.setClasses;
		out = [];
		allSetClasses.do { |setClass|
			if (setClass.includesAll(normalForm)) { out = out.add(setClass) };
		};
		^out;
	}

	// symmetry/equivalence
	degreeOfSymmetry {
		^[this.transpositionalSymmetry, this.inversionalSymmetry];
	}
	transpositionalSymmetry {
		^this.symmetricalTranspositions.size;
	}
	inversionalSymmetry {
		^this.symmetricalInversions.size;
	}
	symmetricalTranspositions {
		var pcSet = this.asPCSet;
		^([pcSet] ++ this.commonToneTranspositions({ |n| n == pcSet.size }));
	}
	symmetricalInversions {
		var pcSet = this.asPCSet;
		^this.commonToneInversions({ |n| n == pcSet.size });
	}
}
