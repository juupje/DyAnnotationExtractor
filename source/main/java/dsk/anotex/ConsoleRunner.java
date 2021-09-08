package dsk.anotex;

import dsk.anotex.core.FileFormat;
import dsk.anotex.util.CommandLineParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Console runner for the application.
 */
public class ConsoleRunner {
    // Recognized command line arguments.
    public static final String ARG_INPUT = "input";
    public static final String ARG_PAGES = "pages";
    public static final String ARG_OUTPUT = "output";
    public static final String ARG_HELP = "help";

    /**
     * Execute annotation extraction from file.
     * @param inputFile Input file name.
     * @param settings Additional export settings.
     * @param outputFile Output file name.
     */
    public void doExtract(String inputFile, Map<String, Object> settings, String outputFile) {
        printMessage(String.format("Reading input document: '%s'", inputFile));
        String outFile = new AnnotationExtractor().extractAnnotations(inputFile, settings, outputFile);
        printMessage(String.format("Annotations extracted to: '%s'", outFile));
    }

    /**
     * Print message to the console.
     * @param message The message.
     */
    protected void printMessage(String message) {
        System.out.println(message);
    }

    /**
     * Print error message to the console.
     * @param error The error message.
     */
    protected void printError(String error) {
        System.err.println(error);
        System.err.flush();
    }

    /**
     * Get the application start message.
     * @return Start message.
     */
    protected String getStartMessage() {
        return String.format("%s %s (document annotation extractor)", Constants.APP_NAME,
            Constants.APP_VERSION);
    }

    /**
     * Get information about the supported command line arguments.
     * Override to add your application specific parameters.
     * @return Command line arguments description.
     */
    protected String getHelpMessage() {
        return "Usage:\n"
            + String.format("DyAnnotationExtractor -%s <inputFile> -%s <outputFile>\n",
                ARG_INPUT, ARG_OUTPUT)
            + "where:\n"
            + "<inputFile> = input file name.\n"
            + "<outputFile> = output file name (optional).\n"
            + "additional arguments:\n"
            + String.format("-%s : Prints the supported command line arguments.\n", ARG_HELP);
    }
    
    private static int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch(NumberFormatException e) {
			throw new RuntimeException("Cannot parse '" + s + "' as integer");
		}
    }
    
    static final Pattern pattern = Pattern.compile("(\\d+)-(\\d+)");
    private static ArrayList<Integer> parsePages(String pages) {
    	ArrayList<Integer> pagenumbers = new ArrayList<Integer>();
    	String[] cuts = pages.split(",");
    	for(String cut : cuts) {
    		Matcher match = pattern.matcher(cut);
    		if(match.matches()) {
    			int begin = parseInt(match.group(1));
    			int end = parseInt(match.group(2));
    			if(end<begin) continue;
    			for(int i = begin; i <= end; i++)
    				pagenumbers.add(i);
    		} else 
    			pagenumbers.add(parseInt(cut));
    	}
    	pagenumbers.sort(null);
    	int number = -1;
    	//remove duplicate and negative numbers
    	for(Iterator<Integer> iter = pagenumbers.iterator(); iter.hasNext();) {
    		int next = iter.next();
    		if(number==next || next<0)
    			iter.remove();
    		else
    			number = next;
    	}
    	return pagenumbers;
    }

    /**
     * Execution entry point.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        // Start the application.
        ConsoleRunner runner = new ConsoleRunner();
        runner.printMessage(runner.getStartMessage());

        // Parse the command line.
        CommandLineParser parser = new CommandLineParser();
        parser.parse(args);

        String inputFile = parser.getArgumentValue(ARG_INPUT);
        if ((inputFile != null)) {
            // Holder for additional execution settings.
            HashMap<String, Object> settings = new HashMap<>();
            // Retrieve the output file name.
            String outputFile = parser.getArgumentValue(ARG_OUTPUT);
            settings.put(Constants.EXPORT_FORMAT, FileFormat.detectFileFormat(outputFile));
            
            /// Retrieve the pages to be extracted
            String pages = parser.getArgumentValue(ARG_PAGES);
            if(pages != null && pages.length()>0)
            	settings.put(Constants.PAGES, parsePages(pages));
            
            // Execute the annotation extraction.
            runner.doExtract(inputFile, settings, outputFile);
        }
        else {
            // Print additional information.
            if (parser.hasArgument(ARG_HELP)) {
                runner.printMessage(runner.getHelpMessage());
            }
            else {
                runner.printError("Error: Invalid command line argument(s)");
                runner.printMessage(runner.getHelpMessage());
            }
        }
    }
}
