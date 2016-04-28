package com.haurentziu.starchart;

import com.haurentziu.coordinates.EquatorialCoordinates;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * 
 * @author haurentziu
 *
 */


class DataLoader {
	
	DataLoader(){
		
	}
	
	ArrayList<Star> loadStars(){
		ArrayList stars = new ArrayList<>();
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader("./res/hip_main.csv"));
			String line = reader.readLine();

			while(line != null){
				String[] s = line.split("\\|");
				String[] raStrings = s[3].split(" ");
				String[] decStrings = s[4].split(" ");
				float mag  = Float.parseFloat(s[5]);
				int hip = Integer.parseInt(s[1].trim());
				double rightAscension = 15 * (Integer.parseInt(raStrings[0]) + Integer.parseInt(raStrings[1])/60f + Float.parseFloat(raStrings[2])/3600f);
				int decDegrees = Integer.parseInt(decStrings[0]);
				double declination;
				if(decDegrees > 0)
					declination = Integer.parseInt(decStrings[0]) + Integer.parseInt(decStrings[1])/60.0 + Float.parseFloat(decStrings[2])/3600.0;
				else{
					declination = Integer.parseInt(decStrings[0]) - Integer.parseInt(decStrings[1])/60.0 - Float.parseFloat(decStrings[2])/3600.0;
				}

				float bv;
				if(s[37].trim().length() == 0){
					bv = 0;
				}
				else {
					bv = Float.parseFloat(s[37]);
				}
				Star star = new Star(Math.toRadians(rightAscension), Math.toRadians(declination), mag, bv, hip);
				stars.add(star);
				line = reader.readLine();
			}
			reader.close();
			System.out.println("Loaded Stars");
		}
		catch(Exception ex){
			System.out.println("Could not load stars!");
			System.exit(0);
		}
		
		return stars;
	}
	
	ArrayList<Constellation> loadConstellations(ArrayList<Star> stars){
		try{
			BufferedReader reader = new BufferedReader(new FileReader("./res/constellations.csv"));
			String line = reader.readLine();

			ArrayList<Constellation> constellations = new ArrayList<>();

			while(line != null){
				String s[] = line.split(" ");
				ArrayList<Star> startLines, endLines;

				startLines = new ArrayList<>();
				endLines = new ArrayList<>();

				for(int k = 2; k < s.length; k+=2){
					int start = Integer.parseInt(s[k]);
					int end = Integer.parseInt(s[k+1]);
					Star startStar = binarySearch(stars, start);
					Star endStar = binarySearch(stars, end);
					startLines.add(startStar);
					endLines.add(endStar);
				}

				constellations.add(new Constellation(startLines, endLines));
				line = reader.readLine();
			}
			System.out.println("Loaded Constellation Lines");

			return constellations;
		}
		catch(Exception ex){
			System.out.println("Could not load constellation lines");
			System.exit(0);
			return null;
		}
	}

	private Star binarySearch(ArrayList<Star> stars, int hip){
		int high = stars.size() - 1;
		int low = 0;
		while(high >= low){
			int middle = (low + high)/2;
			Star middleStar = stars.get(middle);

			if(middleStar.getHipparcos() == hip){
				return stars.get(middle);
			}
			if(middleStar.getHipparcos() < hip)
				low = middle + 1;

			if(middleStar.getHipparcos() > hip)
				high = middle - 1;
		}
		return null;
	}

	ArrayList<MilkyWayVertex> loadMilkyWay() {
		try{
			ArrayList<MilkyWayVertex> vertices = new ArrayList<>();

			BufferedReader reader = new BufferedReader(new FileReader("./res/milkyway.csv"));
			String line = reader.readLine();
			while(line != null){
				String[] data = line.split(",");
				boolean move = data[0].equals("MOVE");
				double ra = Math.toRadians(Double.parseDouble(data[1])*15);
				double dec = Math.toRadians(Double.parseDouble(data[2]));
				vertices.add(new MilkyWayVertex(new EquatorialCoordinates(ra, dec), move));
				line = reader.readLine();
			}
			return vertices;

		}

		catch (Exception ex){
			ex.printStackTrace();
			System.out.println("Could not load Milky Way vertices");
			System.exit(0);
			return null;
		}

	}

}
