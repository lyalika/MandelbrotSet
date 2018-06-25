package vyara.pf;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.complex.Complex;

public class MandelbrotSet {
	private static int ITERATIONS = 640;
	private int width;
	private int height;
	private double x_min;
	private double y_min;
	private double complexPlaneWidth;
	private double complexPlaneHeight;
	private int[] points;
	
	public MandelbrotSet(int width, int height, double x_min, double x_max, double y_min, double y_max) {
		this.width = width;
		this.height = height;
		this.x_min = x_min;
		this.y_min = y_min;
		this.points = new int[height * width];
		complexPlaneWidth = Math.sqrt(Math.pow(x_max - x_min, 2));
		complexPlaneHeight = Math.sqrt(Math.pow(y_max - y_min, 2));
	}
	
	public MandelbrotSet() {
		new MandelbrotSet(640, 480, -2.0, 2.0, -1.0, 1.0);
	}
	
	/**
	 * Fill in the points array with a single thread
	 * @return the points array
	 */
	public int[] calculate() {
		for (int i = 0; i < points.length; i++) {
			points[i] = checkPoint(transformPointIndexToComplexPlaneCoordinates(i)) ? 0x000000 : 0xffffff;
		}
		return points;
	}
	
	/**
	 * Fill in the points array in parallel
	 * @param paralellism The number of threads
	 * @return The points array
	 * @throws InterruptedException
	 */
	public int[] pcalculate(int paralellism) throws InterruptedException {
		int batchSize = (int) Math.ceil(((double) points.length / (paralellism * 4)));
		
		ExecutorService executor = Executors.newFixedThreadPool(paralellism);
		
		for (int i = 0; i < points.length; i += batchSize) {
			executor.execute(new BackgroundCalculator(i, batchSize));
		}
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		
		return points;
	}
	
	/**
	 * Convert an index in the points array to complex number for use in the formula
	 * @param n The offset
	 * @return The complex number corresponding to this offset
	 */
	private Complex transformPointIndexToComplexPlaneCoordinates(int n) {
		double x = n % width;
		double y = n / width;
		double a = x_min + (x / width) * complexPlaneWidth;
		double b = y_min + (y / height) * complexPlaneHeight;
		return new Complex(a, b);
	}
	
	private static Complex iteration(Complex z, Complex c) {
		return c.multiply(z.cos());
	}
	
	private static boolean checkPoint(Complex c) {
		Complex z_0 = new Complex(0.0, 0.0);

		Complex z_n = z_0;
		Double a_n;

		for(int n = 0; n < ITERATIONS; n++) {
			z_n = iteration(z_n, c);
			a_n = z_n.getReal();

			if (a_n.isInfinite() || a_n.isNaN()) {
				return false;
			}
		}

		return true;		
	}
	
	/**
	 * Private class for background calculation of a portion of the fractal
	 */
	private class BackgroundCalculator implements Runnable {
		private int firstOffset;
		private int lastOffset;

		/**
		 * Initialize by starting offset and maximum number of points
		 * @param offset The offset
		 * @param maxCount The maximum number of points to check
		 */
		public BackgroundCalculator(int offset, int maxCount) {
			this.firstOffset = offset;
			if (offset + maxCount > points.length) {
				this.lastOffset = points.length - 1;
			} else {
				this.lastOffset = offset + maxCount - 1;
			}
		}
		
		public void run() {
			long startTime;
			long endTime;
			long totalTime;
			startTime = Calendar.getInstance().getTimeInMillis();
			RunMe.conditionalPrint(String.format("Starting thread %d - %d\n", firstOffset, lastOffset));
			for (int i = firstOffset; i <= lastOffset; i++) {
				points[i] = checkPoint(transformPointIndexToComplexPlaneCoordinates(i)) ? 0x000000 : 0xffffff;
			}
			endTime = Calendar.getInstance().getTimeInMillis();
			totalTime = endTime - startTime;
			RunMe.conditionalPrint(String.format("Stopping thread %d - %d, execution time %d ms\n", firstOffset, lastOffset, totalTime));
		}
	}
}
