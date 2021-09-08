package dsk.anotex.importer;

import java.util.ArrayList;

import dsk.anotex.core.AnnotatedDocument;

/**
 * Interface for importing annotations for different documents.
 */
public interface AnnotationImporter {

    /**
     * Read annotations from given document file.
     * @param fileName Document file name.
     * @return Document annotations.
     */
    public AnnotatedDocument readAnnotations(String fileName, ArrayList<Integer> pages);

}
