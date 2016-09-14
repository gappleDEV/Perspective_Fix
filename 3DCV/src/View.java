import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class View extends JFrame implements MouseListener
{
	
	double[] x = {-1,-1,-1,-1}; //original point x coordinate
	double[] y = {-1,-1,-1,-1}; //original point y coordinate
	
	double[] xp = {-1,-1,-1,-1}; //x prime; new point x coordinate
	double[] yp = {-1,-1,-1,-1}; //y prime; new point y coordinate
	
	double[][] A = new double[3][3];
	double[][] Ai = new double[3][3]; //inverse of A
	
	double[][] B = new double[3][3];
	
	boolean wantDots = true;
	
	Matrix H; //homography matrix
	
	int count = -1;
	String imgName;
	Shape circle = new Ellipse2D.Float(10,10,10,10);
	BufferedImage myimg;
	
	public View(String imgName)
	{
		this.imgName = imgName;
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		if(wantDots)
		{
			for(int i = 0; count > -1 && i <= count; i++)
			{
				g.setColor(Color.GREEN);
				g.fillOval((int)x[i] - 10, (int)y[i] - 10, 20, 20);
			}
		}
	}
	
	public void start()
	{	
		try {
			myimg = ImageIO.read(new File(imgName));
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		
		ImageIcon icon = new ImageIcon(myimg);
	
		this.setLayout(new FlowLayout());
		this.setSize(myimg.getWidth(), myimg.getHeight());
		JLabel lbl = new JLabel();
		lbl.setIcon(icon);
		this.add(lbl);
		this.addMouseListener(this);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JOptionPane.showMessageDialog(null, "Use the following order:\n1) bottom left corner -> 2) bottom right corner -> 3) top right corner -> 4) top left corner");

	}
	
	//Calculates the corresponding points
	public void calculateNewPoints()
	{
		//Find the middle between the points that are meant to have the same x coordinates
		xp[0] = (x[0] + x[3]) / 2;
		xp[3] = x[0];
		xp[1] = (x[1] + x[2]) / 2;
		xp[2] = xp[1];
		
		//Find the middle between the points that are meant to have the same x coordinates
		yp[0] = (y[0] + y[1]) / 2;
		yp[1] = yp[0];
		yp[2] = (y[2] + y[3]) / 2;
		yp[3] = yp[2];
		
		System.out.println("New point 1: (" + xp[0] + "," + yp[0] + ")");
		System.out.println("New point 2: (" + xp[1] + "," + yp[1] + ")");
		System.out.println("New point 3: (" + xp[2] + "," + yp[2] + ")");
		System.out.println("New point 4: (" + xp[3] + "," + yp[3] + ")");
		
		fillMatrixA();
	}
	
	//-------- The following two functions are basically identical, but have been made into 2 functions to make tracing easier
	//The following are simplifications found from http://math.stackexchange.com/questions/296794/finding-the-transform-matrix-from-4-projected-points-with-javascript
	
	//New attempt with Cramer's Rule
	
	//Fills matrix A
	public void fillMatrixA()
	{
		/*double t = ((x[3] - x[0]) - (y[3] - y[0])*(x[1] - x[0])/(y[1] - y[0]));
		double u = (y[3] - y[0])/(y[1] - y[0]) - (y[2] - y[0]) * t;
		double l = 1 - u - t;*/
		
		double delta = x[0]*(y[1] - y[2]) - y[0]*(x[1] - x[2]) + (x[1]*y[2] - y[1]*x[2]);
		
		double l = x[3]*(y[1] - y[2]) - y[3]*(x[1] - x[2]) + (x[1]*y[2] - y[1]*x[2]);
		l = l / delta;
		double u = x[0]*(y[3] - y[2]) - y[0]*(x[3] - x[2]) + (x[3]*y[2] - y[3]*x[2]);
		u = u / delta;
		double t = x[0]*(y[1] - y[3]) - y[0]*(x[1] - x[3]) + (x[1]*y[3] - y[1]*x[3]);
		t = t / delta;
		
		A[0][0] = l * x[0];
		A[1][0] = l * y[0];
		A[2][0] = l;
		
		A[0][1] = u * x[1];
		A[1][1] = u * y[1];
		A[2][1] = u;
		
		A[0][2] = t * x[2];
		A[1][2] = t * y[2];
		A[2][2] = t;
		
		fillMatrixB();
	}
	
	//Fills matrix B
	public void fillMatrixB()
	{
		/*double t = ((xp[3] - xp[0]) - (yp[3] - yp[0])*(xp[1] - xp[0])/(yp[1] - yp[0]));
		double u = (yp[3] - yp[0])/(yp[1] - yp[0]) - (yp[2] - yp[0]) * t;
		double l = 1 - u - t;*/
		
		double delta = xp[0]*(yp[1] - yp[2]) - yp[0]*(xp[1] - xp[2]) + (xp[1]*yp[2] - yp[1]*xp[2]);
		
		double l = xp[3]*(yp[1] - yp[2]) - yp[3]*(xp[1] - xp[2]) + (xp[1]*yp[2] - yp[1]*xp[2]);
		l = l / delta;
		double u = xp[0]*(yp[3] - yp[2]) - yp[0]*(xp[3] - xp[2]) + (xp[3]*yp[2] - yp[3]*xp[2]);
		u = u / delta;
		double t = xp[0]*(yp[1] - yp[3]) - yp[0]*(xp[1] - xp[3]) + (xp[1]*yp[3] - yp[1]*xp[3]);
		t = t / delta;
		
		System.out.println("t: " + t + "\nu: " + u + "\nl: " + l);
		
		B[0][0] = l * xp[0];
		B[1][0] = l * yp[0];
		B[2][0] = l;
		
		B[0][1] = u * xp[1];
		B[1][1] = u * yp[1];
		B[2][1] = u;
		
		B[0][2] = t * xp[2];
		B[1][2] = t * yp[2];
		B[2][2] = t;
		
		getInverseA();
	}
	//-----------------------------------------------------------------------------------------------------------------------

	//Get inverse of A
	public void getInverseA()
	{
		Matrix a = new Matrix(A);
		System.out.println("Matrix A:\n" + a);
		Matrix ai = a.getInverse();
		Ai = ai.getDoubleArray();
		System.out.println("Matrix A inverse:\n" + ai);
		
		System.out.println("Matrix B:\n" + (new Matrix(B)));
		
		getHomography();
	}
	
	//Calculate the homography matrix
	public void getHomography()
	{
		Matrix ai = new Matrix(Ai);
		Matrix b = new Matrix(B);
		
		H = b.dotProduct(ai);
		
		System.out.println("Matrix H:\n" + H);
		
		createNewImage();
	}
	
	public void createNewImage()
	{
		int imgWidth = myimg.getWidth();
		int imgHeight = myimg.getHeight();
		BufferedImage img = new BufferedImage(imgWidth, imgHeight, myimg.getType());
		
		for(int x = 0; x < imgWidth; x++)
		{
			for(int y = 0; y < imgHeight; y++)
			{
				Color atPixel = new Color(myimg.getRGB(x, y));
				int r = atPixel.getRed();
				int g = atPixel.getGreen();
				int b = atPixel.getBlue();
				int a = atPixel.getAlpha();
				
				int col = (a << 24) | (r << 16) | (g << 8) | b;
				
				double[][] p = {{x}, {y}, {1}};
				Matrix point = new Matrix(p);
				//System.out.println("Matrix for point (" + x + "," + y + "):\n" + point);
				
				Matrix homogPoint = H.dotProduct(point);
				//System.out.println("Matrix for homogenous point:\n" + homogPoint);
				double[][] np = homogPoint.getDoubleArray();
				
				int x11 = (int) (np[0][0] / np[2][0]);
				int y11 = (int) (np[1][0] / np[2][0]);
				
				if(x11 > 0 && y11 > 0 && x11 < imgWidth && y11 < imgHeight)
					img.setRGB(x11, y11, col);
			}
		}
		wantDots = false;
		
		ImageIcon icon = new ImageIcon(img);
		
		this.setContentPane(new JLabel(icon));
		this.pack();
		this.setVisible(true);
		
		System.out.println("Done");
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		if(count < 3)
		{
			count++;
			
			x[count] = e.getX();
			y[count] = e.getY();
			
			System.out.println("Clicked at point (" + x[count] + ", " + y[count] + ")");
			
			repaint();
			
			if(count == 3)
			{
				System.out.println("Done gathering points");
				calculateNewPoints();
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
