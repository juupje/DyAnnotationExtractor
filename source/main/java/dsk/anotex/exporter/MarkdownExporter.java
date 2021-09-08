package dsk.anotex.exporter;

import dsk.anotex.Constants;
import dsk.anotex.core.AnnotatedDocument;
import dsk.anotex.core.Annotation;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Export annotated document to Markdown format.
 */
public class MarkdownExporter implements AnnotationExporter {
	
    @Override
    public void export(AnnotatedDocument document, Map<String, Object> context, Writer output) {
    	int begin = (int) context.getOrDefault(Constants.BEGIN, 0);
    	int end = (int) context.getOrDefault(Constants.END, document.getNumberOfPages());
        String mdDocument = convert(document, begin, end);
        try {
            output.write(mdDocument);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert annotated document to string in Markdown format.
     * @param document Document to convert.
     * @return The document as string.
     */
    protected String convert(AnnotatedDocument document, int begin, int end) {
        final String BR = System.lineSeparator();
        // TODO: Use specialized Markdown library if the requirements evolve
        // (currently this would be overkill).
        StringBuilder buf = new StringBuilder(1024);
        if (document.getTitle() != null && document.getTitle().length()>0) {
            buf.append("# ").append(document.getTitle());
            buf.append(BR);
            buf.append(BR);
        }
        String subject = document.getSubject();
        if (subject != null && subject.length()>0) {
            buf.append("\"").append(subject).append("\"");
            buf.append(BR);
        }
        List<String> keywords = document.getKeywords();
        if (!keywords.isEmpty()) {
            buf.append("_").append(keywords.stream().reduce((s1,s2) -> s1 +","+s2).get());
            buf.append("_").append(BR);
        }
        if(begin!=0 || end != document.getNumberOfPages())
        	buf.append("Exported pages " + begin + "-" + end);
        buf.append(BR);

        //Assuming that the page numbers are sequential and don't need to be sorted.
        int currentPageNumber = -1;
        for (Annotation annotation : document.getAnnotations()) {
        	if(annotation.isEmpty()) continue;
        	int page = annotation.getPage();
        	if(page<begin) continue;
        	if(page>end) break;
        	if(page != currentPageNumber) {
        		buf.append("**Page " + page + "**").append(BR);
        		currentPageNumber = page;
        	}
        	if(annotation.getHighlight()!=null)
        		buf.append(annotation.getHighlight()).append(BR);
        	if(annotation.getText()!=null)
        		buf.append(">").append(annotation.getText()).append(BR);
        	buf.append(BR); //make the distinction between annotations clear. This break is also needed to end the cite.
        }
        return buf.toString();
    }
}
