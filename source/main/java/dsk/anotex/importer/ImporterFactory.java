package dsk.anotex.importer;

import dsk.anotex.core.FileFormat;

/**
 * Annotation importer factory.
 */
public class ImporterFactory {

    /*
     * Prevent instance creation.
     */
    private ImporterFactory() {
    }

    /**
     * Create annotation importer for specified file format.
     * @param format Desired file format.
     * @return Importer instance for this format.
     */
    public static AnnotationImporter createImporter(FileFormat format) {
        switch(format) {
        case PDF:
        	return new PdfAnnotationImporter();
        default:
        	String message = String.format("Unsupported import format '%s'", format);
        	throw new IllegalArgumentException(message);
        }
    }

}
