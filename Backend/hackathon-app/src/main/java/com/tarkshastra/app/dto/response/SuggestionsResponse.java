package com.tarkshastra.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestionsResponse {

    private String summary;
    private List<String> priorityAreas;
    private List<SuggestionItem> suggestions;
    private String motivationalNote;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuggestionItem {
        private String area;
        private String currentValue;
        private String targetValue;
        private String action;
        private String impact;
    }
}
