package org.dariah.desir.secondeCodeSprint;

// object of text annotation result containing the label, the offset positions, and the sub string text

public class Acknowledgment {
    private String label;
    private String text;
    private int start_offset;
    private int end_offset;

    public int getStart_offset() {
        return start_offset;
    }

    public void setStart_offset(int start_offset) {
        this.start_offset = start_offset;
    }

    public int getEnd_offset() {
        return end_offset;
    }

    public void setEnd_offset(int end_offset) {
        this.end_offset = end_offset;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
