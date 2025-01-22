import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.*;

public class HierarchicalJsonValidator {
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, ValidatorDefinition> validatorDefinitions = new HashMap<>();

    // Validator definition structure
    static class ValidatorDefinition {
        String name;                       // Validator name
        String type;                       // Validator type
        Map<String, Object> config;        // Configuration parameters
        String message;                    // Error message
        List<ValidatorDefinition> validators; // Nested validators
        
        public ValidatorDefinition() {
            this.validators = new ArrayList<>();
        }
    }

    static class ValidationResult {
        boolean valid = true;
        Map<String, List<String>> errors = new HashMap<>();

        void addError(String field, String message) {
            valid = false;
            errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
        }
    }

    // Load validator definitions from JSON
    public void loadValidatorDefinitions(String validatorConfigJson) throws JsonProcessingException {
        JsonNode config = mapper.readTree(validatorConfigJson);
        
        // Clear existing definitions
        validatorDefinitions.clear();
        
        // Process each validator definition
        config.get("validators").forEach(validatorNode -> {
            ValidatorDefinition def = parseValidatorDefinition(validatorNode);
            validatorDefinitions.put(def.name, def);
        });
    }

    // Parse validator definition from JSON node
    private ValidatorDefinition parseValidatorDefinition(JsonNode node) {
        ValidatorDefinition def = new ValidatorDefinition();
        def.name = node.get("name").asText();
        def.type = node.get("type").asText();
        def.message = node.has("message") ? node.get("message").asText() : null;
        
        // Parse configuration
        if (node.has("config")) {
            def.config = mapper.convertValue(node.get("config"), Map.class);
        }
        
        // Parse nested validators
        if (node.has("validators") && node.get("validators").isArray()) {
            node.get("validators").forEach(validatorNode -> {
                def.validators.add(parseValidatorDefinition(validatorNode));
            });
        }
        
        return def;
    }

    // Validate data against schema
    public ValidationResult validate(String json, String schemaJson) {
        try {
            JsonNode data = mapper.readTree(json);
            JsonNode schema = mapper.readTree(schemaJson);
            return validate(data, schema);
        } catch (JsonProcessingException e) {
            ValidationResult result = new ValidationResult();
            result.addError("$", "Invalid JSON: " + e.getMessage());
            return result;
        }
    }

    private ValidationResult validate(JsonNode data, JsonNode schema) {
        ValidationResult result = new ValidationResult();
        
        schema.fields().forEachRemaining(entry -> {
            String field = entry.getKey();
            JsonNode validatorList = entry.getValue();
            
            if (!validatorList.isArray()) {
                result.addError(field, "Validators must be an array");
                return;
            }

            JsonNode value = getNestedValue(data, field);
            
            for (JsonNode validatorNode : validatorList) {
                String validatorName = validatorNode.get("validator").asText();
                ValidatorDefinition def = validatorDefinitions.get(validatorName);
                
                if (def == null) {
                    result.addError(field, "Unknown validator: " + validatorName);
                    continue;
                }

                validateWithDefinition(value, validatorNode, def, field, result);
            }
        });

        return result;
    }

    // Validate value using validator definition
    private void validateWithDefinition(JsonNode value, JsonNode validatorNode, 
                                      ValidatorDefinition def, String field, 
                                      ValidationResult result) {
        // Validate current level
        boolean currentValid = validateValue(value, def, validatorNode);
        if (!currentValid) {
            String message = validatorNode.has("message") ? 
                           validatorNode.get("message").asText() : 
                           def.message != null ? def.message : 
                           "Validation failed for " + field;
            result.addError(field, message);
            return;
        }

        // Process nested validators
        if (!def.validators.isEmpty()) {
            for (ValidatorDefinition nestedDef : def.validators) {
                validateWithDefinition(value, validatorNode, nestedDef, field, result);
            }
        }
    }

    // Validate single value against validator definition
    private boolean validateValue(JsonNode value, ValidatorDefinition def, JsonNode config) {
        switch (def.type) {
            case "type":
                return validateType(value, def.config);
            case "length":
                return validateLength(value, def.config);
            case "range":
                return validateRange(value, def.config);
            case "pattern":
                return validatePattern(value, def.config);
            case "enum":
                return validateEnum(value, def.config);
            case "custom":
                return validateCustom(value, def.config);
            default:
                return true;
        }
    }

    private boolean validateType(JsonNode value, Map<String, Object> config) {
        String type = (String) config.get("type");
        return switch (type) {
            case "string" -> value.isTextual();
            case "number" -> value.isNumber();
            case "boolean" -> value.isBoolean();
            case "object" -> value.isObject();
            case "array" -> value.isArray();
            default -> false;
        };
    }

    private boolean validateLength(JsonNode value, Map<String, Object> config) {
        if (!value.isTextual()) return false;
        String text = value.asText();
        Integer min = (Integer) config.get("min");
        Integer max = (Integer) config.get("max");
        
        return (min == null || text.length() >= min) && 
               (max == null || text.length() <= max);
    }

    private boolean validateRange(JsonNode value, Map<String, Object> config) {
        if (!value.isNumber()) return false;
        double num = value.asDouble();
        Double min = (Double) config.get("min");
        Double max = (Double) config.get("max");
        
        return (min == null || num >= min) && 
               (max == null || num <= max);
    }

    private boolean validatePattern(JsonNode value, Map<String, Object> config) {
        if (!value.isTextual()) return false;
        String pattern = (String) config.get("pattern");
        return value.asText().matches(pattern);
    }

    private boolean validateEnum(JsonNode value, Map<String, Object> config) {
        @SuppressWarnings("unchecked")
        List<String> allowed = (List<String>) config.get("values");
        return allowed.contains(value.asText());
    }

    private boolean validateCustom(JsonNode value, Map<String, Object> config) {
        // Implement custom validation logic
        return true;
    }

    private JsonNode getNestedValue(JsonNode data, String field) {
        String[] parts = field.split("\\.");
        JsonNode current = data;
        
        for (String part : parts) {
            if (current == null || !current.isObject()) {
                return null;
            }
            current = current.get(part);
        }
        
        return current;
    }

    // Example usage
    public static void main(String[] args) throws JsonProcessingException {
        HierarchicalJsonValidator validator = new HierarchicalJsonValidator();

        // Load validator definitions with nested validators
        String validatorConfigJson = """
        {
            "validators": [
                {
                    "name": "string",
                    "type": "type",
                    "config": {
                        "type": "string"
                    },
                    "message": "Must be a string",
                    "validators": [
                        {
                            "name": "length",
                            "type": "length",
                            "config": {
                                "min": 1,
                                "max": 100
                            },
                            "message": "Length must be between 1 and 100"
                        },
                        {
                            "name": "pattern",
                            "type": "pattern",
                            "config": {
                                "pattern": "^[A-Za-z0-9\\s]+$"
                            },
                            "message": "Must contain only letters, numbers and spaces"
                        }
                    ]
                },
                {
                    "name": "email",
                    "type": "type",
                    "config": {
                        "type": "string"
                    },
                    "validators": [
                        {
                            "name": "emailFormat",
                            "type": "pattern",
                            "config": {
                                "pattern": "^[A-Za-z0-9+_.-]+@(.+)$"
                            },
                            "message": "Invalid email format"
                        }
                    ]
                },
                {
                    "name": "number",
                    "type": "type",
                    "config": {
                        "type": "number"
                    },
                    "validators": [
                        {
                            "name": "range",
                            "type": "range",
                            "config": {
                                "min": 0,
                                "max": 100
                            },
                            "message": "Number must be between 0 and 100"
                        }
                    ]
                }
            ]
        }
        """;

        validator.loadValidatorDefinitions(validatorConfigJson);

        // Example schema using nested validators
        String schemaJson = """
        {
            "user.name": [
                {
                    "validator": "string",
                    "message": "Invalid name format"
                }
            ],
            "user.email": [
                {
                    "validator": "email",
                    "message": "Invalid email"
                }
            ],
            "user.age": [
                {
                    "validator": "number",
                    "message": "Invalid age"
                }
            ]
        }
        """;

        // Data to validate
        String jsonData = """
        {
            "user": {
                "name": "John123!",
                "email": "invalid-email",
                "age": 150
            }
        }
        """;

        ValidationResult result = validator.validate(jsonData, schemaJson);
        
        // Print results
        System.out.println("Valid: " + result.valid);
        if (!result.valid) {
            result.errors.forEach((field, errors) -> {
                System.out.println(field + ": " + String.join(", ", errors));
            });
        }
    }
}