package dev.porama.gradingcore.common.seaweed;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.URISyntaxException;

public class AllocationResponse {
    @Getter
    @Setter
    @SerializedName("fid")
    private String fileId;
    @Getter
    @Setter
    @SerializedName("url")
    private String urlString;
    @Getter
    @Setter
    @SerializedName("publicUrl")
    private String publicUrlString;

    @Override
    public String toString() {
        return "AllocationResponse{" +
               "fileId='" + fileId + '\'' +
               ", urlString='" + urlString + '\'' +
               ", publicUrlString='" + publicUrlString + '\'' +
               '}';
    }
}
