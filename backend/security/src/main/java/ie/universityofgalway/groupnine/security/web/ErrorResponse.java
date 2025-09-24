package ie.universityofgalway.groupnine.security.web;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String error, String message) {
}
