package dev.dammak.notificationservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDto {
    private String to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String content;
    private Boolean isHtml;
    private List<String> attachments;
    private Map<String, Object> variables;
}