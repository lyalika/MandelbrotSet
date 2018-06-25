package vyara.pf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.apache.commons.cli.*;

public class RunMe {
	private static int threads = 1;
	private static boolean quiet = false;
	private static int width = 640;
	private static int height = 480;
	private static double xMin = -2.0;
	private static double xMax = 2.0;
	private static double yMin = -2.0;
	private static double yMax = 2.0;
	private static String imgName = "zad18.png";
	private static long startTime;
	private static long endTime;
	private static long totalTime;
	
	public static void conditionalPrint(String message) {
		if (!quiet) {
			System.out.print(message);
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		/* Settings */
		
		Options options = new Options();
		options.addOption("t", "tasks", true, "Number of threads (default 1)");
		options.addOption("q", "quiet", false, "Quiet mode");
		options.addOption("s", "size", true, "Output image size <width>x<height> (default 640x480)");
		options.addOption("r", "range", true, "Range of the complex plane we will traverse xMin:xMax:yMin:yMax (default -2.0:2.0:-2.0:2.0)");
		options.addOption("o", "output", true, "Name of the output image");
		
		HelpFormatter formatter = new HelpFormatter();
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e1) {
			formatter.printHelp("vyara-mandelbrot", options );
			System.exit(1);
			return;
		}
		
		if(cmd.hasOption("t")) {
			try {
				threads = Integer.parseInt(cmd.getOptionValue("t"));
			} catch (NumberFormatException boom) {
				formatter.printHelp("vyara-mandelbrot", options );
				System.exit(1);
			}
		}
		
		quiet = cmd.hasOption("q");
		
		if(cmd.hasOption("s")) {
			try {
				String[] size = cmd.getOptionValue("s").split("x");
				if (size.length == 2) {
					width = Integer.parseInt(size[0]);
					height = Integer.parseInt(size[1]);
				} else {
					formatter.printHelp("vyara-mandelbrot", options );
					System.exit(1);
				}
			} catch (NumberFormatException boom) {
				formatter.printHelp("vyara-mandelbrot", options );
				System.exit(1);
			}
		}
		
		if(cmd.hasOption("r")) {
			try {
				String[] range = cmd.getOptionValue("r").split(":");
				if (range.length == 4) {
					xMin = Double.parseDouble(range[0]);
					xMax = Double.parseDouble(range[1]);
					yMin = Double.parseDouble(range[2]);
					yMax = Double.parseDouble(range[3]);
				} else {
					formatter.printHelp("vyara-mandelbrot", options );
					System.exit(1);
				}
			} catch (NumberFormatException boom) {
				formatter.printHelp("vyara-mandelbrot", options );
				System.exit(1);
			}
		}
		
		if(cmd.hasOption("o")) {
			try {
				imgName = cmd.getOptionValue("o");
			} catch (NumberFormatException boom) {
				formatter.printHelp("vyara-mandelbrot", options );
				System.exit(1);
			}
		}
		
		/* Instantiate and calculate fractal */
		conditionalPrint(String.format("Initializing MandelbrotSet %d threads for %dx%d\n", threads, width, height));
		MandelbrotSet test = new MandelbrotSet(width, height, xMin, xMax, yMin, yMax);
		startTime = Calendar.getInstance().getTimeInMillis();
		int[] colors = test.pcalculate(threads);
		endTime = Calendar.getInstance().getTimeInMillis();
		
		/* Write image in file system */
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

		Graphics2D g2d = bi.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, width, height);
		
		for (int i = 0; i < colors.length; i++) {
			bi.setRGB(i % width, i / width, colors[i]);
		}
		
		g2d.setColor(Color.GRAY);
		g2d.drawRect(0, 0, width - 2, height - 2);

		try {
			ImageIO.write(bi, "PNG", new File(imgName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		totalTime = endTime - startTime;
		
		System.out.printf("Total execution time for current run  %d ms\n", totalTime);

	}
}
