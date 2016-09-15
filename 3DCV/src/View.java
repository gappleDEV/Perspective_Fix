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

import Jama.Matrix;
import Jama.SingularValueDecomposition;

@SuppressWarnings("serial")
public class View extends JFrame implements MouseListener
{
	
	double[] x = new double[5]; //original point x coordinate
	double[] y = new double[5]; //original point y coordinate
	
	double[] xp = new double[5]; //x prime; new point x coordinate
	double[] yp = new double[5]; //y prime; new point y coordinate
	
	double[][] B = new double[3][3];
	
	boolean wantDots = true;
	boolean horizonalOutput = true;
	boolean compactOutput = false;
	boolean inverseWarping = true;
	
	Matrix A1; //A sub matrices corresponding to (xi, yi)
	Matrix A2;
	Matrix A3;
	Matrix A4;
	Matrix A5;
	
	Matrix A; //combines the previous Ai matrices
	
	Matrix H; //homography matrix
	Matrix Hinv; //inverse of H
	
	int count = -1;
	String imgName;
	Shape circle = new Ellipse2D.Float(10,10,10,10);
	BufferedImage myimg;
	
	public View(String imgName)
	{
		this.imgName = imgName;
	}
	
	public String print2DArray(double[][] arr)
	{
		String toRet = "";
		for(int i = 0; i < arr.length; i++)
		{
			toRet += "[";
			for(int j = 0; j < arr[0].length; j++)
			{
				toRet += arr[i][j];
				if(j != arr[0].length - 1)//will run again
				{
					toRet += "\t";
				}
			}
			toRet+= "]\n";
		}
		return toRet;
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
				g.fillOval((int)x[i] - 5, (int)y[i] - 5, 10, 10);
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
		
		JOptionPane.showMessageDialog(null, "Use the following order:\n1) bottom left corner -> 2) bottom right corner -> 3) top right corner -> 4) top left corner -> 5) center point");

	}
	
	//Calculates the corresponding points
	public void calculateNewPoints()
	{
		System.out.println("Image width = " + myimg.getWidth());
		System.out.println("Image height = " + myimg.getHeight());
		if(horizonalOutput)
		{
			//Horizonal court
			if(compactOutput)
			{
				//Find the middle between the points that are meant to have the same x coordinates
				xp[0] = (x[0] + x[1]) / 2;
				xp[1] = xp[0];
				xp[2] = (x[2] + x[3]) / 2;
				xp[3] = xp[2];
				xp[4] = (xp[2] + xp[1]) / 2;
					
				//Find the middle between the points that are meant to have the same x coordinates
				yp[0] = (y[0] + y[3]) / 2;
				yp[3] = yp[0];
				yp[1] = (y[1] + y[2]) / 2;
				yp[2] = yp[1];
				yp[4] = (yp[1] + yp[0]) / 2;
			}
			else
			{
				//tries for a larger output image
				//Ideal for most visible output
				xp[0] = xp[1] = Math.min(x[0], x[1]);
				xp[2] = xp[3] = (x[2] + x[3]) / 2;
				xp[4] = (xp[2] + xp[1]) / 2;
				
				yp[0] = yp[3] = Math.min(y[0], y[3]);
				yp[1] = yp[2] = Math.max(y[1], y[2]);
				yp[4] = (yp[1] + yp[0]) / 2;
			}
		}
		else
		{
			//Vertical court
			if(compactOutput)
			{
				//Find the middle between the points that are meant to have the same x coordinates
				xp[0] = (x[0] + x[3]) / 2;
				xp[3] = xp[0];
				xp[1] = (x[1] + x[2]) / 2;
				xp[2] = xp[1];
				xp[4] = (xp[1] + xp[0]) / 2;
				
				//Find the middle between the points that are meant to have the same x coordinates
				yp[0] = (y[0] + y[1]) / 2;
				yp[1] = yp[0];
				yp[2] = (y[2] + y[3]) / 2;
				yp[3] = yp[2];
				yp[4] = (yp[3] + yp[0]) / 2;
			}
			else
			{
				//tries for a larger output image
				//Ideal for most visible output
				xp[0] = xp[3] = (x[0] + x[3]) / 2;
				xp[1] = xp[2] = Math.max(x[1], x[2]);
				xp[4] = (xp[1] + xp[0]) / 2;
				
				yp[0] = yp[1] = Math.max(y[0], y[1]);
				yp[2] = yp[3] = Math.min(y[2], y[3]);
				yp[4] = (yp[3] + yp[0]) / 2;
			}
		}
		
		System.out.println("New point 1: (" + xp[0] + "," + yp[0] + ")");
		System.out.println("New point 2: (" + xp[1] + "," + yp[1] + ")");
		System.out.println("New point 3: (" + xp[2] + "," + yp[2] + ")");
		System.out.println("New point 4: (" + xp[3] + "," + yp[3] + ")");
		System.out.println("New point 5: (" + xp[4] + "," + yp[4] + ")");
		
		A1 = new Matrix(fillMatrixAi(0));
		A2 = new Matrix(fillMatrixAi(1));
		A3 = new Matrix(fillMatrixAi(2));
		A4 = new Matrix(fillMatrixAi(3));
		A5 = new Matrix(fillMatrixAi(4));
		
		System.out.println("Matrix A1:\n" + print2DArray(A1.getArray()));
		System.out.println("Matrix A2:\n" + print2DArray(A2.getArray()));
		System.out.println("Matrix A3:\n" + print2DArray(A3.getArray()));
		System.out.println("Matrix A4:\n" + print2DArray(A4.getArray()));
		System.out.println("Matrix A5:\n" + print2DArray(A5.getArray()));
		
		fillA();
		getHomography();
		createNewImage();
	}
	
	public double[][] fillMatrixAi(int i)
	{
		double[][] mat = new double[2][9];
		
		mat[0][0] = 0;
		mat[0][1] = 0;
		mat[0][2] = 0;
		mat[0][3] = -1 * x[i];
		mat[0][4] = -1 * y[i];
		mat[0][5] = -1;
		mat[0][6] = yp[i]*x[i];
		mat[0][7] = yp[i]*y[i];
		mat[0][8] = yp[i];
		
		mat[1][0] = x[i];
		mat[1][1] = y[i];
		mat[1][2] = 1;
		mat[1][3] = 0;
		mat[1][4] = 0;
		mat[1][5] = 0;
		mat[1][6] = -1*xp[i]*x[i];
		mat[1][7] = -1*xp[i]*y[i];
		mat[1][8] = -1*xp[i];
		
		return mat;
	}
	
	public void fillA()
	{
		double[][] fullA = new double[10][9];
		
		//Put in A1
		for(int i = 0; i < 2; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				fullA[i][j] = A1.getArray()[i][j];
			}
		}
		
		//Put in A2
		for(int i = 0; i < 2; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				fullA[i+2][j] = A2.getArray()[i][j];
			}
		}
		
		//Put in A3
		for(int i = 0; i < 2; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				fullA[i+4][j] = A3.getArray()[i][j];
			}
		}
		
		//Put in A4
		for(int i = 0; i < 2; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				fullA[i+6][j] = A4.getArray()[i][j];
			}
		}
		
		//Put in A5
		for(int i = 0; i < 2; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				fullA[i+8][j] = A5.getArray()[i][j];
			}
		}
		
		A = new Matrix(fullA);
		
		System.out.println("Matrix A:\n" + print2DArray(A.getArray()));
	}
	
	public void getHomography()
	{
		SingularValueDecomposition s = A.svd();
		
		Matrix V = s.getV();
		
		System.out.println("Matrix V:\n" + print2DArray(V.getArray()));
		
		double[][] temp = new double[9][1];
		
		for(int i = 0; i < 9; i++)
		{
			temp[i][0] = V.getArray()[i][8];
		}
		
		double[][] realH = new double[3][3];
		int tIndex = 0;
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				realH[i][j] = temp[tIndex++][0];
			}
		}
		
		H = new Matrix(realH);
		Hinv = H.inverse();
		
		System.out.println("Matrix H:\n" + print2DArray(H.getArray()));
		
	}
	
	public void createNewImage()
	{
		int imgWidth = myimg.getWidth();
		int imgHeight = myimg.getHeight();
		BufferedImage img = new BufferedImage(imgWidth, imgHeight, myimg.getType());
		
		if(!inverseWarping)
		{
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
					
					Matrix homogPoint = H.times(point);
					//System.out.println("Matrix for homogenous point:\n" + homogPoint);
					double[][] np = homogPoint.getArray();
					
					int x11 = (int) (np[0][0] / np[2][0]);
					int y11 = (int) (np[1][0] / np[2][0]);
					
					if(x11 > 0 && y11 > 0 && x11 < imgWidth && y11 < imgHeight)
						img.setRGB(x11, y11, col);
				}
			}
		}
		else
		{
			//Inverse Warping
			for(int x = 0; x < imgWidth; x++)
			{
				for(int y = 0; y < imgHeight; y++)
				{
					double[][] p = {{x}, {y}, {1}};
					Matrix point = new Matrix(p);
					
					Matrix origPoint = Hinv.times(point);
					
					int x11 = (int) (origPoint.getArray()[0][0] / origPoint.getArray()[2][0]);
					int y11 = (int) (origPoint.getArray()[1][0] / origPoint.getArray()[2][0]);
					
					if(x11 > 0 && y11 > 0 && x11 < imgWidth && y11 < imgHeight)
					{
						Color atPixel = new Color(myimg.getRGB(x11, y11));
						int r = atPixel.getRed();
						int g = atPixel.getGreen();
						int b = atPixel.getBlue();
						int a = atPixel.getAlpha();
						
						int col = (a << 24) | (r << 16) | (g << 8) | b;
						
						img.setRGB(x, y, col);
					}
				}
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
		if(count < 4)
		{
			count++;
			
			x[count] = e.getX();
			y[count] = e.getY();
			
			System.out.println("Clicked at point (" + x[count] + ", " + y[count] + ")");
			
			repaint();
			
			if(count == 4)
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
