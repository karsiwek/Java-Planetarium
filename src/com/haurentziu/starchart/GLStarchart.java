package com.haurentziu.starchart;

import java.awt.Font;

import java.awt.geom.Point2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.haurentziu.coordinates.EquatorialCoordinates;
import com.haurentziu.coordinates.HorizontalCoordinates;
import com.haurentziu.coordinates.SphericalCoordinates;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;


/**
 * 
 * @author haurentziu
 *
 */

public class GLStarchart implements GLEventListener{

	final Star stars[];
	private final Constellation constellations[];
	

	double localSideralTime = 12; //Local Sideral Time
	float latitude = (float) Math.toRadians(51 + 28.0/60.0);
	float longitude = 0;
	float altitudeAngle = (float) Math.toRadians(-150);
	float azimuthAngle = (float) Math.toRadians(180);

	boolean showGround = true;
	boolean showGrid = false;
	boolean showConstellationLines = true;
	boolean showCardinalPoints = true;
	boolean showStarNames = false;
	
	byte projection = SphericalCoordinates.STEREOGRAPHIC_PROJECTION;
	int timeWarp = 1;

	int height, width;
	float ortoHeight, ortoWidth;

	private Timer t = new Timer();
	private long unixTime;

	private boolean isSunlight;


	private GLUT glut = new GLUT();
	float zoom = 2;
	
	Star selectedStar = new Star(0, 0, 0, 0, 0);
	boolean isSelected = false;
	
	GLStarchart(){
		double julianDate = System.currentTimeMillis()/86400000.0 + 2440587.5;
		double T = (julianDate - 2451545.0)/36525.0;
		double LST0 = 280.46061837 + 360.98564736629 * (julianDate - 2451545.0) + 0.000387933*T*T - T*T*T/38710000.0;
		unixTime = System.currentTimeMillis();
		while(LST0 > 360)
			LST0 -= 360;
		
		localSideralTime = (float) Math.toRadians(LST0);
		DataLoader loader = new DataLoader();
		stars = loader.loadStars();
		constellations = loader.loadConstellations();
	}
	
	
	@Override
	public void display(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();


		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
			
		float fps = drawable.getAnimator().getLastFPS();
		System.out.println(fps);
		gl.glClearColor(0f, 0.075f, 0.125f, 1f);

		if(showGrid)
			drawGrid(gl);

		if(showConstellationLines)
			drawConstellations(gl);

		drawStars(gl);

		renderBodies(gl);

		if(showGround)
			drawGround(gl);

		if(showCardinalPoints)
			drawCardinalPoints(gl);

		if(isSelected)
			renderStarText(gl);

		renderObserverInfo(gl);

		updateTime();
		
	}


	@Override
	public void dispose(GLAutoDrawable arg0) {

	}

	@Override
	public void init(GLAutoDrawable drawable) {
		drawable.getAnimator().setUpdateFPSFrames(20, null);
		GL2 gl = drawable.getGL().getGL2();
	//	gl.glClearDepth(1.0);                     // Enables Clearing Of The Depth Buffer
	//	gl.glEnable(GL2.GL_DEPTH_TEST);            // Enables Depth Testing
	//	gl.glDepthFunc(GL2.GL_LEQUAL);
	}
	

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL2 gl = drawable.getGL().getGL2();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		
		double aspectRatio = (double)width/height;
		gl.glOrtho(-2*aspectRatio, 2*aspectRatio, -2, 2, -1, 1);
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		ortoWidth = (float)aspectRatio*2f;
		ortoHeight = 2;
		this.width = width;
		this.height = height;
	}


	private void renderBodies(GL2 gl){
		double julianDate = unixTime / 86400000.0  + 2440587.5;
		SolarSystem system = new SolarSystem();
		EquatorialCoordinates sunEquatorial = system.computeSunEquatorial(julianDate);
		HorizontalCoordinates sunHorizontal = sunEquatorial.toHorizontal(longitude, latitude, localSideralTime);
		if(sunHorizontal.getAltitude() > 0) {
			Point2D p = sunHorizontal.toProjection(azimuthAngle, altitudeAngle, projection);
			if(isInBounds(p)) {
				gl.glColor3f(1f, 0.749f, 0f);
				drawCircle((float) (zoom * p.getX()), (float) (zoom * p.getY()), 0.075f, gl);
			}
			isSunlight = true;
		}
		else isSunlight = false;

	}


	private void renderStarText(GL2 gl){

		TextRenderer titleRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 26));
		titleRenderer.setColor(0.051f, 0.596f, 0.729f, 1f);
		titleRenderer.beginRendering(width, height);
		titleRenderer.draw("HIP " + selectedStar.getHipparcos(), 0, height - 30);
		titleRenderer.endRendering();
		
		TextRenderer infoRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 15));
		infoRenderer.setColor(0.141f, 0.784f, 0.941f, 1f);
		infoRenderer.beginRendering(width, height);
		infoRenderer.draw("Magnitude: " + selectedStar.getMagnitude(), 0, height - 55);

		String bvString = String.format("B-V Color Index %.2f", selectedStar.getBVMagnitude());
		infoRenderer.draw(bvString, 0, height - 75);

		String raString = rad2String(selectedStar.getRightAscension(), false, true);
		String decString = rad2String(selectedStar.getDeclination(), false, false);
		infoRenderer.draw("RA/Dec(J2000): "  + raString + "/" + decString, 0, height - 95);

		String azString = rad2String(selectedStar.getHorizontalCoordinates().getAzimuth() - Math.PI, true, false);
		String altString = rad2String(selectedStar.getHorizontalCoordinates().getAltitude(), false, false);
		infoRenderer.draw("Az/Alt: " + azString + " / " + altString, 0, height - 115);
		infoRenderer.endRendering();
	}

	private void renderObserverInfo(GL2 gl){
		GLUT glut = new GLUT();
		Date date = new Date(unixTime);
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		sdf.setTimeZone(TimeZone.getDefault());
		String formatedDate = sdf.format(date);

		gl.glColor3f(1, 1, 1);
		gl.glRasterPos2f(-ortoWidth, -ortoHeight + 0.32f);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Time Warp: " + timeWarp + "x");
		gl.glRasterPos2f(-ortoWidth, -ortoHeight + 0.22f);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Time: " + formatedDate);
		gl.glRasterPos2f(-ortoWidth, -ortoHeight + 0.12f);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Observer's Altitude: " + rad2String(latitude, false, false));
		gl.glRasterPos2f(-ortoWidth, -ortoHeight + 0.02f);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Observer's Longitude: " + rad2String(longitude, false, false));
	}
	
	private String rad2String(double d, boolean normalise, boolean inHours){
		double deg = Math.toDegrees(d);
		if(normalise){
			while(deg > 360)
				deg -= 360;
			while(deg < 0)
				deg +=360;
		}
		if(inHours)
			deg /= 15.0;
		int degrees = (int)deg;
		int minutes = (int)((deg - (int)deg)*60);
		float seconds = (float)(deg - degrees - minutes/60.0)*3600f;
		
		String s;
		if(inHours)
			s = String.format("%dh %02dm %.2fs", degrees, minutes, seconds);
		else
			s = String.format("%d\u00b0 %02d\u2032 %.2f\u2033", degrees, minutes, seconds);

		return s;
	}
	
	private void drawCardinalPoints(GL2 gl){
		GLUT glu = new GLUT();
		gl.glColor3f(0.694f, 0f, 0.345f);
		String[] cardinalPoints = {"S", "W", "N", "E"};

		for(int i = 0; i < 4; i++){
			HorizontalCoordinates hc = new HorizontalCoordinates(i*Math.PI/2, 0);
			Point2D p = hc.toProjection(azimuthAngle, altitudeAngle, projection);

			gl.glRasterPos2f((float)(zoom*p.getX()), (float)(zoom*p.getY()));
			glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, cardinalPoints[i]);

		}
	}
	
		
	private void drawGrid(GL2 gl){
		gl.glColor3f(0.404f, 0.302f, 0f);
		float start;
		if(showGround) {
			start = 0;
		}
		else {
			start = - (float)Math.PI / 2f + 0.2f;
		}

		//altitude lines
		for(float i = 0.1f; i < 2*Math.PI; i+= Math.PI/18f){
			gl.glBegin(GL2.GL_LINE_STRIP);
			for(float j = start; j < Math.PI/2; j+= 0.1){
				HorizontalCoordinates h = new HorizontalCoordinates(i, j);
				Point2D p = h.toProjection(azimuthAngle, altitudeAngle, projection);
				gl.glVertex2f((float)(zoom*p.getX()), (float)(zoom*p.getY()));
			}
			gl.glEnd();
		}

		//azimuth lines
		for(float i = start; i < Math.PI/2; i += 0.15){
			gl.glBegin(GL2.GL_LINE_STRIP);
			for(float j = 0; j < 2*Math.PI + 0.2; j += 0.1){
				HorizontalCoordinates h = new HorizontalCoordinates(j, i);
				Point2D p = h.toProjection(azimuthAngle, altitudeAngle, projection);
				gl.glVertex2f((float)(zoom*p.getX()), (float)(zoom*p.getY()));
			}
			gl.glEnd();
		}
		
	}

	private void drawGround(GL2 gl){
		if(isSunlight) {
			gl.glColor3f(0.25f, 0.38f, 0.17f);
		}
		else{
			gl.glColor3f(0.15f, 0.28f, 0.07f);
		}
		for(double i = 0; i <= 2*Math.PI; i += Math.PI/17){
			drawPieceofGround(gl, i, i + Math.PI/16.8, Math.PI/30.0);

		}
	}

	private boolean isInBounds(Point2D p){
		boolean inBounds = p.getX() <= ortoWidth && p.getX() >= -ortoWidth && p.getY() <= ortoHeight && p.getY() >= -ortoHeight;
		return inBounds;
	}


	private void drawPieceofGround(GL2 gl, double azStart, double azEnd, double step){
		gl.glBegin(GL2.GL_TRIANGLE_FAN);

		for(double i = azStart; i <= azEnd; i += step){
			HorizontalCoordinates c = new HorizontalCoordinates(i, 0);
			Point2D p = c.toProjection(azimuthAngle, altitudeAngle, projection);
			if(p.distance(0, 0) < 2) {
				gl.glVertex2f((float)(zoom * p.getX()), (float)(zoom * p.getY()));
			}
		}

		for(double i = 0; i > -Math.PI/2; i -= step){
			HorizontalCoordinates c = new HorizontalCoordinates(azEnd, i);
			Point2D p = c.toProjection(azimuthAngle, altitudeAngle, projection);
			if(p.distance(0, 0) < 2) {
				gl.glVertex2f((float)(zoom * p.getX()), (float)(zoom * p.getY()));
			}
		}


		for(double i = -Math.PI/2; i <= 0; i += step){
			HorizontalCoordinates c = new HorizontalCoordinates(azStart, i);
			Point2D p = c.toProjection(azimuthAngle, altitudeAngle, projection);
			if(p.distance(0, 0) < 2) {
				gl.glVertex2f((float)(zoom * p.getX()), (float)(zoom * p.getY()));
			}
		}

		gl.glEnd();
	}


	private void drawConstellations(GL2 gl){
		gl.glColor3f(0.4f, 0.4f, 0.4f);
		for(int i = 0; i < constellations.length; i++){
			ConstellationLine[] lines = constellations[i].getLines();
			for(int j = 0; j < lines.length; j++){
				
				EquatorialCoordinates equatorial[] = lines[j].getPositions(stars);
				HorizontalCoordinates start = equatorial[0].toHorizontal(longitude, latitude, localSideralTime);
				HorizontalCoordinates end = equatorial[1].toHorizontal(longitude, latitude, localSideralTime);
				
				if(start.getAltitude() > 0 || end.getAltitude() > 0 || !showGround){
					Point2D p1, p2;
					
					p1 = start.toProjection(azimuthAngle, altitudeAngle, projection);
					p2 = end.toProjection(azimuthAngle, altitudeAngle, projection);
					if(p1.distance(0, 0) < 2|| p2.distance(0, 0) < 2) {
						gl.glBegin(GL2.GL_LINES);
						gl.glVertex2f((float) (zoom * p1.getX()), (float) (zoom * p1.getY()));
						gl.glVertex2f((float) (zoom * p2.getX()), (float) (zoom * p2.getY()));
						gl.glEnd();
					}
				}
			}
		}
	}
	
	private void drawStars(GL2 gl){
		gl.glColor3f(1f, 1f, 1f);
		GLUT glut = new GLUT();
		for(int i = 0; i < stars.length; i++){
			if(stars[i].getMagnitude() < 5.5){
				HorizontalCoordinates c = stars[i].toHorizontal(longitude, latitude, localSideralTime);
				stars[i].setHorizontalCoordinates(c);
				if(c.getAltitude() > 0 || !showGround){
					Point2D p;
					p = c.toProjection(azimuthAngle, altitudeAngle, projection);
					if(p.distance(0, 0) < 2){
						double[] color = stars[i].getStarRGB();
						gl.glColor3f((float)color[0], (float)color[1], (float)color[2]);
						stars[i].setProjection(p);
						float radius = stars[i].getRadius();
						drawCircle((float)(zoom * p.getX()), (float)(zoom * p.getY()), radius, gl);
						if(stars[i].getMagnitude() < 1 && showStarNames){
							gl.glRasterPos2f((float)(zoom * p.getX() + radius), (float)(zoom * p.getY()+ radius));
							glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, "HIP " + stars[i].getHipparcos());
						}

					}

				}
			}
		}
	}



	private void drawCircle(float centerX, float centerY , float radius, GL2 gl){
		gl.glBegin(GL2.GL_POLYGON);
		for(float angle = 0; angle < 2*Math.PI + 0.5f; angle += 0.5){
			float x = (float)(centerX + radius * Math.cos(angle));
			float y = (float)(centerY + radius * Math.sin(angle));
			gl.glVertex2f(x, y);
		}

		gl.glEnd();
	}
	
	private void updateTime(){
		int deltaT = t.getDeltaTime();
		unixTime += timeWarp*deltaT;
		localSideralTime += 4.84813681e-9*timeWarp*deltaT; //dt*pi/(180*3600000) ms(time) -> rad
	}

}
