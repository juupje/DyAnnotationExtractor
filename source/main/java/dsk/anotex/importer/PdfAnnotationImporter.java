package dsk.anotex.importer;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfTextMarkupAnnotation;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.filter.TextRegionEventFilter;
import com.itextpdf.kernel.pdf.canvas.parser.listener.FilteredTextEventListener;
import dsk.anotex.core.AnnotatedDocument;
import dsk.anotex.core.Annotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Import annotations form PFD files.
 */
public class PdfAnnotationImporter implements AnnotationImporter {
    protected Logger log = LogManager.getLogger(this.getClass());

    public AnnotatedDocument readAnnotations(String fileName) {
        // Check the file existence.
        File file = new File(fileName).getAbsoluteFile();
        if (!file.isFile()) {
            String message = String.format("File '%s' does not exist", file.getName());
            throw new IllegalArgumentException(message);
        }

        // Extract the annotations.
        PdfDocument pdfDocument = readDocument(file);
        return extractAnnotations(pdfDocument);
    }

    /**
     * Read PDF document from file.
     * @param file File name.
     * @return PDF document.
     */
    protected PdfDocument readDocument(File file) {
        PdfDocument document;
        try {
            document = new PdfDocument(new PdfReader(file.getAbsolutePath()));
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return document;
    }

    /**
     * Extract annotations from given PDF document.
     * @param pdfDocument PDF document.
     * @return Extracted annotations.
     */
    protected AnnotatedDocument extractAnnotations(PdfDocument pdfDocument) {
        AnnotatedDocument document = new AnnotatedDocument();
        PdfDocumentInfo pdfInfo = pdfDocument.getDocumentInfo();
        document.setTitle(pdfInfo.getTitle());
        document.setSubject(pdfInfo.getSubject());
        document.setAuthor(pdfInfo.getAuthor());
        List<String> keywords = convertToKeywords(pdfInfo.getKeywords());
        document.setKeywords(keywords);

        List<Annotation> annotations = new LinkedList<>();
        for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
            PdfPage page = pdfDocument.getPage(i);
            for (PdfAnnotation pdfAnnotation : page.getAnnotations()) {
                Annotation annotation = convertAnnotation(pdfAnnotation, i);
                if (annotation != null) {
                    annotations.add(annotation);
                }
            } //
        } //
        document.setAnnotations(annotations);

        return document;
    }

    /**
     * Convert document annotation to independent format.
     * @param pdfAnnotation Annotation to be converted.
     * @param pagenr Page number of the Annotation, used for log output
     * @return Converted annotation.
     */
    protected Annotation convertAnnotation(PdfAnnotation pdfAnnotation, int pagenr) {
        String annotationText = null;
        String highlightedText = null;
        if (PdfName.Highlight.equals(pdfAnnotation.getSubtype())) {
            PdfTextMarkupAnnotation annotation = (PdfTextMarkupAnnotation) pdfAnnotation;
            highlightedText = extractText(annotation.getQuadPoints(), annotation.getPage(), pagenr);
            log.debug("Highlighted text: " + highlightedText);
            highlightedText = normalizeHighlightedText(highlightedText);
        }
        PdfString pdfText = pdfAnnotation.getContents();
        if (pdfText != null) {
            if (pdfText.getEncoding() == null) {
                annotationText = pdfText.toUnicodeString();
            }  else {
            	annotationText = pdfText.getValue();
            }
        }

        Annotation annotation = null;
        if (annotationText != null || highlightedText != null) {
            annotation = new Annotation();
            if(annotationText != null && !annotationText.matches("\\s*"))
            	annotation.setText(removePollutionChars(stripUnwantedChunks(annotationText)));
            if(highlightedText != null && !highlightedText.matches("\\s*"))
            	annotation.setHighlight(removePollutionChars(stripUnwantedChunks(highlightedText)));
            annotation.setPage(pagenr);
        }
        return annotation;
    }

    /**
     * Extracts the text inside the region defined by the quadpoints. This is required for multiline highlights,
     * as the casll annotation.getRectangle() includes the entire first and last line of the highlight.
     * This method break the given quadpoints up into groups of 8 (corresponding to a single rectangle),
     * extracts the text within each group and finally appends the lines together.
     * @param quadpoints An array of length {@code 8*n}, corresponding to 8 values for each of the {@code n} lines.
     * Each group of 8 corresponds to the 4 coordinates of a rectangle (ulx, uly, urx, ury, llx, lly, lrx, lry).
     * @param page The page of the annotation
     * @param pagenr The page number corresponding to the page (only needed for a warning-message).
     * @return The extracted text
     */
    protected String extractText(PdfArray quadpoints, PdfPage page, int pagenr) {
    	if(quadpoints.size()%8!=0) {
    		log.warn("Quadpoints of annotation on page " + pagenr + " not a multiple of 8.");
    		return null;
    	}
    	float[] points = quadpoints.toFloatArray();
    	int lines = quadpoints.size()/8;
    	String text = "";
    	for(int line=0; line<lines; line++) {
    		//use floor and ceil to add a small extra margin around the characters to prevent them from being cut off.
    		//This might accidentally include an extra whitespace, but this is the better alternative to missing characters.
    		int width = (int)Math.ceil(points[line*8+2])-(int)Math.floor(points[line*8]);
    		float height = (float)Math.ceil(points[line*8+1])-(float)Math.floor(points[line*8+5]);
    		//It appears that highlighted text is often cut off as the characters lie slightly outside the rectangle.
    		//Until I get a better idea, I botched this together (make the rectangle 5% bigger in height)
    		float y = Math.min(page.getPageSize().getHeight(),Math.max(0, points[line*8+5]-height*0.025f));
    		height = height*1.05f;
    		float x = (float) Math.floor(points[line*8+4]);
    		Rectangle textCoordinates = new Rectangle(x, y, width, height);
    		PdfTextExtractionStrategy strategy = new PdfTextExtractionStrategy(textCoordinates);
            FilteredTextEventListener textFilter = new FilteredTextEventListener(
                strategy, new TextRegionEventFilter(textCoordinates));
            String highlightedText = PdfTextExtractor.getTextFromPage(page, textFilter);
            text += (line==lines-1 ? highlightedText : cleanHighlightLine(highlightedText) + " "); //this cleans possible hyphenated linebreaks
    	}
    	return text;
    }
    
    /**
     * Convert comma separated string to list of keywords.
     * @param sKeywords String to be converted.
     * @return List of keywords.
     */
    protected List<String> convertToKeywords(String sKeywords) {
        List<String> keywords;
        if ((sKeywords != null) && !sKeywords.isEmpty()) {
            // The string can be surrounded with double quotes.
            sKeywords = stripDoubleQuotes(sKeywords);
            // Split on comma (and trim around it).
            String[] words = sKeywords.split(" ?, ?");
            keywords = Arrays.asList(words);
        }
        else {
            keywords = new LinkedList<>();
        }
        return keywords;
    }

    /**
     * Strip double quotes enclosing string. For example:
     * <pre>
     *     "Flower" becomes Flower
     * </pre>
     * @param text Text to be stripped.
     * @return Stripped text.
     */
    protected String stripDoubleQuotes(String text) {
        String st = text;
        if (!text.isEmpty()) {
            char dQuota = '"';
            int endPos = text.length() - 1;
            if ((text.charAt(0) == dQuota) && (text.charAt(endPos) == dQuota)) {
                st = text.substring(1, endPos).trim();
            }
        }
        return st;
    }

    /**
     * Normalize highlighted text - when retrieved from PDF renderer, it contains defects (like
     * additional spaces, inappropriate characters).
     * @param highlightedText Highlighted text.
     * @return Normalized text.
     */
    protected String normalizeHighlightedText(String highlightedText) {
        return highlightedText.replaceAll("\\s+", " ").replaceAll("[“”]", "\"");
    }

    /**
     * Strip unwanted character before or after the annotation (these chunks are PDF library issue).
     * @param text The text to strip.
     * @return Stripped text.
     */
    protected String stripUnwantedChunks(String text) {
        text = text.replaceFirst("^\\p{javaLowerCase}?[.?!]? ", "")
            .replaceFirst(" \\p{IsAlphabetic}?$", "");
        text = stripDoubleQuotes(text);
        return text;
    }

    /**
     * Remove the pollution characters from the annotation text. These characters appear, without being
     * part of the original text:
     * <ul>
     *     <li>Tab chars appear between words if the original text it aligned on both sides.</li>
     * </ul>
     * @param text The text to clean.
     * @return Cleaned text.
     */
    protected String removePollutionChars(String text) {
        text = text.replaceAll("\t", " ");
        text = stripDoubleQuotes(text);
        return text;
    }
    
    /**
     * Removes a trailing hyphen at a linebreak. If the line ends with a whitespace after the linebreak, it is removed too.
     * This results in a cleaner output text in case of multiline highlights..
     * @param line The line to clean
     * @return Cleaned line
     */
    protected String cleanHighlightLine(String line) {
        if(line.endsWith("-"))
        	line = line.substring(0, line.length()-(
        			Character.isWhitespace(line.charAt(line.length()-2)) ? 2 : 1));
        return line;
    }
}
