package org.dariah.desir.data;

import com.itextpdf.text.pdf.PdfReader;
import java.io.*;

public class Page {
    private Double width;
    private Double height;

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }


    public static Page getPageDimension(File tempFile) {
        Page page = new Page();
        try {
            PdfReader inputPdfReader = new PdfReader(new FileInputStream(tempFile));
            page.setHeight(Double.parseDouble(Float.toString(inputPdfReader.getPageSize(1).getHeight())));
            page.setWidth(Double.parseDouble(Float.toString(inputPdfReader.getPageSize(1).getWidth())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return page;
    }

}
