package com.haurentziu.starchart;

import java.util.ArrayList;

/**
 * 
 * @author haurentziu
 *
 */

public class Constellation {

	private ArrayList<Star> lineStart;
	private ArrayList<Star> lineEnd;
	
	Constellation(ArrayList<Star> lineStart, ArrayList<Star> lineEnd){
		this.lineEnd = lineEnd;
		this.lineStart = lineStart;
	}

	public ArrayList<Star> getStartStars(){
		return lineStart;
	}

	public ArrayList<Star> getEndStars(){
		return lineEnd;
	}


	
}
