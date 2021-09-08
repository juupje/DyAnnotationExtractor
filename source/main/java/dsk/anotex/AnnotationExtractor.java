package dsk.anotex;

import dsk.anotex.core.AnnotatedDocument;
import dsk.anotex.core.FileFormat;
import dsk.anotex.exporter.AnnotationExporter;
import dsk.anotex.exporter.ExporterFactory;
import dsk.anotex.importer.AnnotationImporter;
import dsk.anotex.importer.ImporterFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

/**
 * Document annotation extractor.
 */
public class AnnotationExtractor {

    public AnnotationExtractor() {
        super();
    }

    /**
     * Execute annotation extraction from file.
     * @param inputFile Input file name.
     * @param settings Additional export settings.
     * @param outputFile Output file name. If null - default will be used. If the output file already
     * exists, it will be overwritten.
     * @return The name of the created output file.
     */
    public String extractAnnotations(String inputFile, Map<String, Object> settings, String outputFile) {
        // Extract the annotations.
        @SuppressWarnings("unchecked")
		AnnotatedDocument document = readAnnotations(inputFile, (ArrayList<Integer>) settings.getOrDefault(Constants.PAGES, null));

        // Get appropriate exporter.
        FileFormat exportFormat = (FileFormat) settings.get(Constants.EXPORT_FORMAT);
        if (exportFormat == null) {
            // Use the default export format.
            exportFormat = getDefaultExportFormat();
        }
        AnnotationExporter exporter = ExporterFactory.createExporter(exportFormat);

        // Write the output.
        if (outputFile == null) {
            // Use default output file.
            outputFile = inputFile + exportFormat.getExtension();
        }
        try (Writer output = getOutputWriter(outputFile)) {
            exporter.export(document, settings, output);
        }
        catch (IOException e) {
            throw new RuntimeException("Extraction error", e);
        }
        return outputFile;
    }

    public AnnotatedDocument readAnnotations(String fileName) {
    	return readAnnotations(fileName, null);
    }
    
    /**
     * Read annotations from given document file.
     * @param fileName Document file name.
     * @return Document annotations.
     */
    public AnnotatedDocument readAnnotations(String fileName, ArrayList<Integer> pages) {
        FileFormat format = FileFormat.detectFileFormat(fileName);
        AnnotationImporter importer = ImporterFactory.createImporter(format);
        AnnotatedDocument document = importer.readAnnotations(fileName, pages);
        postProcess(document);
        return document;
    }

    /**
     * Get the default export format.
     * @return Export format.
     */
    protected FileFormat getDefaultExportFormat() {
        return FileFormat.MARKDOWN;
    }

    /**
     * Get output writer for specified input file.
     * @param outputFile Output file name.
     * @return Output writer.
     */
    protected Writer getOutputWriter(String outputFile) {
        File outFile = new File(outputFile);

        // Create necessary directories fore the output path.
        File outFileDir = outFile.getParentFile();
        if (outFileDir != null) {
            outFileDir.mkdirs();
        }

        // Crate buffered file writer.
        Writer writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile),
                StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writer;
    }

    /**
     * Post-process annotated document. This is extension point.
     * @param document The annotated document.
     */
    @SuppressWarnings("unused")
    protected void postProcess(AnnotatedDocument document) {
    }

}

