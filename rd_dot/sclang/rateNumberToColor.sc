// rateNumberToColor.sc - (c) rohan drape, 2004-2007

+ SimpleNumber {
	rateNumberToColor {
		^(0:\darkgoldenrod1, 1:\dodgerblue3, 2:\gray41, 3:\firebrick, 4:\darkgreen).at(this).asString;
	}
}
