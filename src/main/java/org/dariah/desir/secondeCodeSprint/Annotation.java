package org.dariah.desir.secondeCodeSprint;

public class Annotation {
    int annotatorIdx;
    int offset;
    int length;
    int numberAnnotation;

    String text;
    String category;

    public int getNumberAnnotation() {
        return numberAnnotation;
    }

    public void setNumberAnnotation(int numberAnnotation) {
        this.numberAnnotation = numberAnnotation;
    }



    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public int getAnnotatorIdx() {
        return annotatorIdx;
    }

    public void setAnnotatorIdx(int annotatorIdx) {
        this.annotatorIdx = annotatorIdx;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
