package dsk.anotex.core;

import java.io.Serializable;

/**
 * Represents document annotation (highlight/comment). It is independent from the document format.
 */
public class Annotation implements Serializable {
	private static final long serialVersionUID = 4819870231356227612L;
	protected String text;
	protected String highlight;
	protected int page = 0;
	
    public Annotation() {
    }

    public Annotation(String text) {
        this();
        setText(text);
    }
    
    public Annotation(String text, String highlight) {
    	this(text);
        setHighlight(highlight);
    }

    public String getText() {
        return text;
    }

    public String getHighlight() {
    	return highlight;
    }
    
    public int getPage() {
    	return page;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public void setHighlight(String highlight) {
    	this.highlight = highlight;
    }
    
    public void setPage(int page) {
    	this.page = page;
    }
    
    public boolean isEmpty() {
    	return (highlight==null || highlight.length()==0) && (text==null || text.length()==0);
    }

    @Override
    public String toString() {
        return "{Text: " + text + ", Highlight: " + highlight + "}";
    }
}
