package dev.porama.gradingcore.common.seaweed;

import lombok.Data;

@Data
public class LocateResponse {
    private String url;
    private String publicUrl;

    public LocateResponse(String url, String publicUrl) {
        this.url = url;
        this.publicUrl = publicUrl;
    }
}
