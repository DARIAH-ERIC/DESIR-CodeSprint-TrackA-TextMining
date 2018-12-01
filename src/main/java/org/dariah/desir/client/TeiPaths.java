
package org.dariah.desir.client;

/**
 *
 * @author achraf
 */
public interface TeiPaths {
    
    public final static String MetadataElement = "/TEI/teiHeader";
    public final static String FulltextTeiHeaderAuthors = "/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author";
    public final static String MonogrElement = "/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/monogr";
    public final static String IdnoElement = "/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/idno";
    public final static String TitleElement = "/TEI/teiHeader/fileDesc/titleStmt/title";
    public final static String LanguageElement = "/TEI/teiHeader/profileDesc/langUsage/language";
    public final static String TypologyElement = "/TEI/teiHeader/profileDesc/textClass/classCode[@scheme=\"typology\"]";
    public final static String SubmissionDateElement = "/TEI/teiHeader/fileDesc/editionStmt/edition[@type=\"current\"]/date[@type=\"whenSubmitted\"]";
    public final static String DomainElement = "/TEI/teiHeader/profileDesc/textClass/classCode[@scheme=\"domain\"]";
    public final static String EditorElement = "/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/editor";
    public final static String AuthorElement = "/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author";
    
}
