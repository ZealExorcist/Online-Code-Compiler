package com.example.compiler.model;

public class ShareResponse {
    private String shareUrl;
    private String shortUrl;
    private String shareId;

    // Default constructor
    public ShareResponse() {}

    // Constructor with parameters
    public ShareResponse(String shareUrl, String shortUrl, String shareId) {
        this.shareUrl = shareUrl;
        this.shortUrl = shortUrl;
        this.shareId = shareId;
    }

    // Getters and setters
    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }
}
