package dsk.anotex.core;

/**
 * File format enumeration.
 */
public enum FileFormat {
    PDF("Pdf", ".pdf"),
    MARKDOWN("Markdown", ".md"),
    TEXT("Text", ".txt");

    String name;
    String extension;

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    FileFormat(String name, String fileExtension) {
        this.name = name;
        this.extension = fileExtension;
    }

    public static FileFormat getByName(String name) {
        FileFormat match = null;
        for (FileFormat v : values()) {
            if (v.getName().equals(name)) {
                match = v;
                break;
            }
        } //
        return match;
    }
    
    public static FileFormat getByExtension(String ext) {
        FileFormat match = null;
        for (FileFormat v : values()) {
            if (v.getExtension().equals(ext.toLowerCase())) {
                match = v;
                break;
            }
        }
        return match;
    }

    public static FileFormat detectFileFormat(String fileName) {
    	return getByExtension(getFileExtension(fileName));
    }
    
    /**
     * Get file name extension of specified file. Example: for 'file1.ext' it will return '.ext'
     * @param fileName The file name.
     * @return File extension (in lowercase) or empty string if there is no extension.
     */
    public static String getFileExtension(String fileName) {
        String ret = "";
        if (fileName != null) {
            int idx = fileName.lastIndexOf('.');
            if (idx > 0 && (idx < fileName.length() - 1)) {
                ret = fileName.substring(idx).toLowerCase();
            }
        }
        return ret;
    }
}
