package com.example.chatapp.models;

public class Media {
    private String type;
    private String nameFile;
    private String pathFile;
    private long bytes;
    private long kb;
    //
    public Media(String type, String nameFile, String pathFile, long bytes, long kb) {
        this.type = type;
        this.nameFile = nameFile;
        this.pathFile = pathFile;
        this.bytes = bytes;
        this.kb = kb;
    }

    @Override
    public String toString() {
        return "Media{" +
                "type='" + type + '\'' +
                ", nameFile='" + nameFile + '\'' +
                ", pathFile='" + pathFile + '\'' +
                ", bytes=" + bytes +
                ", kb=" + kb +
                '}';
    }

    //
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public String getPathFile() {
        return pathFile;
    }

    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public long getKb() {
        return kb;
    }

    public void setKb(long kb) {
        this.kb = kb;
    }
}
