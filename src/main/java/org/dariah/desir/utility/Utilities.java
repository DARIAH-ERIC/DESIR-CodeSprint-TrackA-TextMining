package org.dariah.desir.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * All utilities are grouped here for managing dates, id generations, files DOM
 * nodes..
 *
 * @author Achraf
 */
public class Utilities {

    private static final Logger logger = LoggerFactory.getLogger(Utilities.class);

    private static Set<String> dates = new LinkedHashSet<String>();
    private static String tmpPath;

    public static void setTmpPath(String tmp_path) {
        tmpPath = tmp_path;
    }

    public static String getTmpPath() {
        return tmpPath;
    }

    static Calendar toDay = Calendar.getInstance();

    static int todayYear = toDay.get(Calendar.YEAR);

    static int minYear = 1900;

    static {
        int todayMonth = toDay.get(Calendar.MONTH) + 1;
        int todayDay = toDay.get(Calendar.DAY_OF_MONTH) + 1;
        for (int year = 1960; year <= todayYear; year++) {
            int monthYear = (year == todayYear) ? todayMonth : 12;
            for (int month = 1; month <= monthYear; month++) {
                for (int day = 1; day <= daysInMonth(year, month); day++) {
                    if ((year == todayYear) && (todayMonth == todayMonth) && (todayDay == day)) {
                        break;
                    }
                    StringBuilder date = new StringBuilder();
                    date.append(String.format("%04d", year));
                    date.append("-");
                    date.append(String.format("%02d", month));
                    date.append("-");
                    date.append(String.format("%02d", day));
                    getDates().add(date.toString());
                }
            }
        }
    }

    public static void updateDates(String fromDate, String untilDate) {
        boolean isOkDate = true;
        if (untilDate != null) {
            isOkDate = false;
        }
        String[] dates1 = new String[dates.size()];
        dates.toArray(dates1);
        for (String date : dates1) {
            if (date.equals(untilDate)) {
                isOkDate = true;
            }
            if (!isOkDate) {
                dates.remove(date);
            }
            if (fromDate != null) {
                if (date.equals(fromDate)) {
                    isOkDate = false;
                }
            }
        }
    }

    protected static int daysInMonth(int year, int month) {
        int daysInMonth;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                daysInMonth = 31;
                break;
            case 2:
                if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
                    daysInMonth = 29;
                } else {
                    daysInMonth = 28;
                }
                break;
            default:
                // returns 30 even for nonexistant months 
                daysInMonth = 30;
        }
        return daysInMonth;
    }

    public static boolean isValidDate(String dateString) {
        //consider other options (YY, YY-MM)?
        return dates.contains(dateString);
    }

    public static String completeDate(String date) {
        if (date.endsWith("-")) {
            date = date.substring(0, date.length()-1);
        }

        String val = "";
        if (date.length() < 4) {
            return val;
        } else if (date.length() == 4) {
            val = date + "-12-31";
        } else if ((date.length() == 7) || (date.length() == 6)) {
            int ind = date.indexOf("-");
            String monthStr = date.substring(ind + 1, date.length());
            if (monthStr.length() == 1) {
                monthStr = "0" + monthStr;
            }
            if (monthStr.equals("02")) {
                val = date.substring(0, 4) + "-" + monthStr + "-28";
            } else if ((monthStr.equals("04")) || (monthStr.equals("06")) || (monthStr.equals("09"))
                    || (monthStr.equals("11"))) {
                val = date.substring(0, 4) + "-" + monthStr + "-30";
            } else {
                int month = Integer.parseInt(monthStr);
                if (month > 12 || month < 1) {
                    monthStr = "12";
                }
                val = date.substring(0, 4) + "-" + monthStr + "-31";
            }
        } else {
            int ind = date.indexOf("-");
            int ind2 = date.lastIndexOf("-");
            String monthStr = date.substring(ind + 1, ind + 3);
            try {
                int month = Integer.parseInt(monthStr);
                if (month > 12 || month < 1) {
                    val = date.substring(0, 4) + "-12" + date.substring(ind + 3, date.length());
                }
                String dayStr = date.substring(ind2 + 1, ind2 + 3);
                int day = Integer.parseInt(dayStr);
                if (day > 31 || day < 1) {
                    val = date.substring(0, 8) + "28";// so naif i know
                }
            } catch (Exception e) {
            }
            val = date.trim();
            // we have the "lazy programmer" case where the month is 00, e.g. 2012-00-31
            // which means the month is unknown

            ///val = val.replace("-00-", "-12-");
        }
        val = val.replace(" ", "T"); // this is for the dateOptionalTime elasticSearch format 

        if (!val.matches("\\d{4}-\\d{2}-\\d{2}") || (Integer.parseInt(val.substring(0, 4)) < minYear || todayYear < Integer.parseInt(val.substring(0, 4)))) {
            val = "";
        }
        return val;
    }


    public static String innerXmlToString(Node node) {
        DOMImplementationLS lsImpl
                = (DOMImplementationLS) node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer lsSerializer = lsImpl.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("xml-declaration", false);
        NodeList childNodes = node.getChildNodes();
        StringBuilder sb = new StringBuilder();
        //for (int i = 0; i < childNodes.getLength(); i++) {
            sb.append(lsSerializer.writeToString(node));
        //}
        return sb.toString();
    }

    public static Element findNode(String attribut, String value, NodeList orgs) {
        Element org = null;
        for (int i = orgs.getLength() - 1; i >= 0; i--) {
            if (orgs.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) orgs.item(i);
                if (element.getAttribute(attribut) == null) {
                    continue;
                }
                if (element.getAttribute(attribut).equals(value)) {
                    org = element;
                    break;
                }
            }
        }
        return org;
    }



    /**
     * Remove starting and ending end-of-line in XML element text content
     * recursively
     */
    public static void trimEOL(Node node, Document doc) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getNodeValue();
            if (text.replaceAll("[ \\t\\r\\n]+", "").length() != 0) {
                while (text.startsWith("\n") && text.length() > 0) {
                    text = text.substring(1, text.length());
                }
                while (text.endsWith("\n") && text.length() > 0) {
                    text = text.substring(0, text.length() - 1);
                }
                node.setNodeValue(text);
            }
        }
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            //if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
            trimEOL(currentNode, doc);
            //}
        }
    }


    public static String storeTmpFile(InputStream inBinary) throws IOException {
        File f = File.createTempFile("tmp", ".pdf", new File(tmpPath));
        // deletes file when the virtual machine terminate
        f.deleteOnExit();
        String filePath = f.getAbsolutePath();
        if (inBinary == null) {
            System.out.println("null");
        }
        getBinaryURLContent(f, inBinary);
        return filePath;
    }

    public static String storeToTmpXmlFile(InputStream inBinary) throws IOException {
        File f = File.createTempFile("tmp", ".xml", new File(tmpPath));
        // deletes file when the virtual machine terminate
        f.deleteOnExit();
        String filePath = f.getAbsolutePath();
        getBinaryURLContent(f, inBinary);
        return filePath;
    }

    /**
     * Download binaries from a given URL
     */
    public static void getBinaryURLContent(File file, InputStream in) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        DataOutputStream writer = new DataOutputStream(fos);
        try {
            byte[] buf = new byte[4 * 1024]; // 4K buffer
            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1) {
                writer.write(buf, 0, bytesRead);
            }
        } //exception null inputstream
        finally {
            in.close();
        }
    }

    public static String formatDate(Date date) {
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
        return dt1.format(date);
    }

    public static Date parseStringDate(String dateString) throws ParseException {
        Date date = null;
        if (dateString != null) {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            date = format.parse(dateString);
        }
        return date;
    }

    public static String getHalIDFromHalDocID(String halDocID) {
        if ( (halDocID == null) || (halDocID.length() == 0) ) 
            return "";
        int ind = halDocID.indexOf("v");
        if (ind != -1) {
            return halDocID.substring(0, ind);
        } else
            return halDocID;
    }

    public static String getVersionFromURI(String uri) {
        int ind = uri.lastIndexOf("v");
        String version = "";
        if (ind > -1) {
            version = uri.substring(ind, uri.length());
        }
        return version;
    }

    public static String getHalURIFromFilename(String filename) {
        int ind = filename.indexOf(".");
        String halURI = filename.substring(0, ind);
        return halURI;
    }

    /**
     * @return the dates
     */
    public static Set<String> getDates() {
        return dates;
    }

    public static String trimEncodedCharaters(String string) {
        return string.replaceAll("&amp\\s+;", "&amp;").
                replaceAll("&quot[^;]|&amp;quot\\s*;", "&quot;").
                replaceAll("&lt[^;]|&amp;lt\\s*;", "&lt;").
                replaceAll("&gt[^;]|&amp;gt\\s*;", "&gt;").
                replaceAll("&apos[^;]|&amp;apos\\s*;", "&apos;");
    }



    public static String formatXMLString(String xmlString) {
        String formatedXml = null;
        try {
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new ByteArrayInputStream(xmlString.getBytes("utf-8"))));

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']",
                    document,
                    XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);

            transformer.transform(new DOMSource(document), streamResult);

            formatedXml = stringWriter.toString();
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | DOMException | IllegalArgumentException | TransformerException e) {
            e.printStackTrace();
        }
        return formatedXml;
    }


    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
    static public String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } finally {
            is.close();
        }
        return sb.toString();
    }

    /**
     * To replace accented characters in a unicode string by unaccented
     * equivalents: é -> e, ü -> ue, ß -> ss, etc. following the standard
     * transcription conventions
     *
     * @param input the string to be processed.
     * @return Returns the string without accent.
     */
    public final static String removeAccents(String input) {
        if (input == null) {
            return null;
        }
        final StringBuffer output = new StringBuffer();
        for (int i = 0; i < input.length(); i++) {
            switch (input.charAt(i)) {
                case '\u00C0': // Ã€
                case '\u00C1': // Ã
                case '\u00C2': // Ã‚
                case '\u00C3': // Ãƒ
                case '\u00C5': // Ã…
                    output.append("A");
                    break;
                case '\u00C4': // Ã„
                case '\u00C6': // Ã†
                    output.append("AE");
                    break;
                case '\u00C7': // Ã‡
                    output.append("C");
                    break;
                case '\u00C8': // Ãˆ
                case '\u00C9': // Ã‰
                case '\u00CA': // ÃŠ
                case '\u00CB': // Ã‹
                    output.append("E");
                    break;
                case '\u00CC': // ÃŒ
                case '\u00CD': // Ã
                case '\u00CE': // ÃŽ
                case '\u00CF': // Ã
                    output.append("I");
                    break;
                case '\u00D0': // Ã
                    output.append("D");
                    break;
                case '\u00D1': // Ã‘
                    output.append("N");
                    break;
                case '\u00D2': // Ã’
                case '\u00D3': // Ã“
                case '\u00D4': // Ã”
                case '\u00D5': // Ã•
                case '\u00D8': // Ã˜
                    output.append("O");
                    break;
                case '\u00D6': // Ã–
                case '\u0152': // Å’
                    output.append("OE");
                    break;
                case '\u00DE': // Ãž
                    output.append("TH");
                    break;
                case '\u00D9': // Ã™
                case '\u00DA': // Ãš
                case '\u00DB': // Ã›
                    output.append("U");
                    break;
                case '\u00DC': // Ãœ
                    output.append("UE");
                    break;
                case '\u00DD': // Ã
                case '\u0178': // Å¸
                    output.append("Y");
                    break;
                case '\u00E0': // Ã
                case '\u00E1': // Ã¡
                case '\u00E2': // Ã¢
                case '\u00E3': // Ã£
                case '\u00E5': // Ã¥
                    output.append("a");
                    break;
                case '\u00E4': // Ã¤
                case '\u00E6': // Ã¦
                    output.append("ae");
                    break;
                case '\u00E7': // Ã§
                    output.append("c");
                    break;
                case '\u00E8': // Ã¨
                case '\u00E9': // Ã©
                case '\u00EA': // Ãª
                case '\u00EB': // Ã«
                    output.append("e");
                    break;
                case '\u00EC': // Ã¬
                case '\u00ED': // Ã
                case '\u00EE': // Ã®
                case '\u00EF': // Ã¯
                    output.append("i");
                    break;
                case '\u00F0': // Ã°
                    output.append("d");
                    break;
                case '\u00F1': // Ã±
                    output.append("n");
                    break;
                case '\u00F2': // Ã²
                case '\u00F3': // Ã³
                case '\u00F4': // Ã´
                case '\u00F5': // Ãµ
                case '\u00F8': // Ã¸
                    output.append("o");
                    break;
                case '\u00F6': // Ã¶
                case '\u0153': // Å“
                    output.append("oe");
                    break;
                case '\u00DF': // ÃŸ
                    output.append("ss");
                    break;
                case '\u00FE': // Ã¾
                    output.append("th");
                    break;
                case '\u00F9': // Ã¹
                case '\u00FA': // Ãº
                case '\u00FB': // Ã»
                    output.append("u");
                    break;
                case '\u00FC': // Ã¼
                    output.append("ue");
                    break;
                case '\u00FD': // Ã½
                case '\u00FF': // Ã¿
                    output.append("y");
                    break;
                default:
                    output.append(input.charAt(i));
                    break;
            }
        }
        return output.toString();
    }

    public final static Element getElementByAttribute(String attr, String value, Element root) {
        Element found = null;
        if (root.hasAttribute(attr) && root.getAttribute(attr).equals(value)) {
            return root;
        }
        NodeList nl = root.getChildNodes();
        for (int i = nl.getLength() - 1; i >= 0; i--) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                found = getElementByAttribute(attr, value, (Element) nl.item(i));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }


}
