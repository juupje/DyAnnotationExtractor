package dsk.anotex.exporter;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import dsk.anotex.Constants;
import dsk.anotex.core.AnnotatedDocument;
import dsk.anotex.core.Annotation;

public class PlainTextExporter implements AnnotationExporter{

	@Override
	public void export(AnnotatedDocument document, Map<String, Object> context, Writer output) {
		int begin = (int) context.getOrDefault(Constants.BEGIN, 0);
    	int end = (int) context.getOrDefault(Constants.END, document.getNumberOfPages());
		String textDocument = convert(document, begin, end);
		try {
			output.write(textDocument);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected String convert(AnnotatedDocument document, int begin, int end) {
		final String BR = System.lineSeparator();
		StringBuilder buf = new StringBuilder(1024);
		buf.append("Annotations extracted by DyAnnotationExtractor");
        if (document.getTitle() != null && document.getTitle().length()>0)
            buf.append("Title: ").append(document.getTitle()).append(BR);
        String subject = document.getSubject();
        if (subject != null && subject.length()>0)
            buf.append("Subject: ").append(subject).append(BR);
        List<String> keywords = document.getKeywords();
        if (!keywords.isEmpty()) {
            buf.append("Keywords: ").append(keywords.stream().reduce((s1,s2) -> s1 +","+s2).get());
            buf.append(BR);
        }
        if(begin!=0 || end != document.getNumberOfPages())
        	buf.append("Exported pages " + begin + "-" + end);
        if(buf.length()>0)
        	buf.append(BR);
        //Assuming that the page numbers are sequential and don't need to be sorted.
        int currentPageNumber = -1;
        for (Annotation annotation : document.getAnnotations()) {
        	if(annotation.getPage()<begin || annotation.isEmpty()) continue;
        	if(annotation.getPage()>end) break;
        	int page = annotation.getPage();
        	if(page != currentPageNumber) {
        		buf.append("--------- Page " + page + " ---------").append(BR);
        		currentPageNumber = page;
        	}
        	if(annotation.getHighlight()!=null) {
        		buf.append("Highlight: ");
        		buf.append(annotation.getHighlight()).append(BR);
        	}
        	if(annotation.getText()!=null)
        		buf.append("Annotation: ").append(annotation.getText()).append(BR);
        	buf.append(BR); //make the distinction between annotations clear.
        }
        return buf.toString();
	}
}
