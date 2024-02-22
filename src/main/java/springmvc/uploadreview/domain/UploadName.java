package springmvc.uploadreview.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadName {
    private String clientName;
    private String storageName;
}
