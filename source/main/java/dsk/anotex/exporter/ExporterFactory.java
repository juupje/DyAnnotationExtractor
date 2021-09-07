package dsk.anotex.exporter;

import dsk.anotex.core.FileFormat;

/**
 * Annotation exporter factory.
 */
public class ExporterFactory {

    /*
     * Prevent instance creation.
     */
    private ExporterFactory() {
    }

    /**
     * Create annotation exporter for specified file format.
     * @param format Desired file format.
     * @return Exporter instance for this format.
     */
    public static AnnotationExporter createExporter(FileFormat format) {
        switch(format) {
        case MARKDOWN:
            return new MarkdownExporter();
        case TEXT:
        	return new PlainTextExporter();
        default:
        		String message = String.format("Unsupported export format '%s'", format);
        		throw new IllegalArgumentException(message);
        }
    }
}