package dto;

import java.util.List;
import java.util.Map;

public record ErrorResponse(String error, String message, Map<String, List<String>> details) {

}
