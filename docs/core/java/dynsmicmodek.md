// 1. First, create a mapping configuration JSON
// mapping-config.json
{
  "mappings": [
    {
      "sourceField": "firstName",
      "targetField": "fullName",
      "transformationType": "CONCAT",
      "additionalFields": ["lastName"],
      "validations": ["REQUIRED", "MAX_LENGTH:50"]
    },
    {
      "sourceField": "emailAddress",
      "targetField": "email",
      "transformationType": "DIRECT",
      "validations": ["EMAIL", "REQUIRED"]
    },
    {
      "sourceField": "phoneNumber",
      "targetField": "contact",
      "transformationType": "PHONE_FORMAT",
      "validations": ["PHONE"]
    },
    {
      "sourceField": "address.street",
      "targetField": "addressDetails.streetAddress",
      "transformationType": "DIRECT",
      "validations": ["REQUIRED"]
    }
  ]
}

// 2. Create configuration models
@Data
public class MappingConfig {
    private List<FieldMapping> mappings;
}

@Data
public class FieldMapping {
    private String sourceField;
    private String targetField;
    private TransformationType transformationType;
    private List<String> additionalFields;
    private List<String> validations;
}

public enum TransformationType {
    DIRECT,
    CONCAT,
    PHONE_FORMAT,
    DATE_FORMAT,
    CUSTOM
}

// 3. Create the dynamic mapper
@Service
@Slf4j
public class DynamicModelMapper {
    private final MappingConfig mappingConfig;
    private final ObjectMapper objectMapper;

    public DynamicModelMapper() {
        this.objectMapper = new ObjectMapper();
        this.mappingConfig = loadMappingConfig();
    }

    private MappingConfig loadMappingConfig() {
        try {
            Resource resource = new ClassPathResource("mapping-config.json");
            return objectMapper.readValue(resource.getInputStream(), MappingConfig.class);
        } catch (IOException e) {
            log.error("Failed to load mapping configuration", e);
            throw new RuntimeException("Failed to load mapping configuration", e);
        }
    }

    public Object mapObject(Object source, Class<?> targetClass) {
        try {
            Object target = targetClass.getDeclaredConstructor().newInstance();
            
            for (FieldMapping mapping : mappingConfig.getMappings()) {
                Object value = getValueFromSource(source, mapping);
                value = transformValue(value, mapping);
                validateValue(value, mapping);
                setValueInTarget(target, value, mapping);
            }
            
            return target;
        } catch (Exception e) {
            log.error("Failed to map object", e);
            throw new RuntimeException("Failed to map object", e);
        }
    }

    private Object getValueFromSource(Object source, FieldMapping mapping) {
        try {
            String[] fields = mapping.getSourceField().split("\\.");
            Object value = source;
            
            for (String field : fields) {
                PropertyDescriptor pd = new PropertyDescriptor(field, value.getClass());
                value = pd.getReadMethod().invoke(value);
                if (value == null) break;
            }
            
            return value;
        } catch (Exception e) {
            log.error("Failed to get value from source", e);
            throw new RuntimeException("Failed to get value from source", e);
        }
    }

    private Object transformValue(Object value, FieldMapping mapping) {
        switch (mapping.getTransformationType()) {
            case DIRECT:
                return value;
                
            case CONCAT:
                return concatenateValues(value, mapping);
                
            case PHONE_FORMAT:
                return formatPhoneNumber(value);
                
            case DATE_FORMAT:
                return formatDate(value);
                
            default:
                return value;
        }
    }

    private void validateValue(Object value, FieldMapping mapping) {
        for (String validation : mapping.getValidations()) {
            if (validation.equals("REQUIRED") && value == null) {
                throw new ValidationException(mapping.getSourceField() + " is required");
            }
            if (validation.equals("EMAIL") && !isValidEmail(value)) {
                throw new ValidationException("Invalid email format");
            }
            if (validation.startsWith("MAX_LENGTH:")) {
                int maxLength = Integer.parseInt(validation.split(":")[1]);
                if (value.toString().length() > maxLength) {
                    throw new ValidationException("Value exceeds maximum length");
                }
            }
        }
    }

    private void setValueInTarget(Object target, Object value, FieldMapping mapping) {
        try {
            String[] fields = mapping.getTargetField().split("\\.");
            Object currentObject = target;
            
            // Navigate to the last parent object
            for (int i = 0; i < fields.length - 1; i++) {
                PropertyDescriptor pd = new PropertyDescriptor(fields[i], currentObject.getClass());
                Object nextObject = pd.getReadMethod().invoke(currentObject);
                if (nextObject == null) {
                    nextObject = pd.getPropertyType().getDeclaredConstructor().newInstance();
                    pd.getWriteMethod().invoke(currentObject, nextObject);
                }
                currentObject = nextObject;
            }
            
            // Set the value in the last field
            PropertyDescriptor pd = new PropertyDescriptor(fields[fields.length - 1], currentObject.getClass());
            pd.getWriteMethod().invoke(currentObject, value);
            
        } catch (Exception e) {
            log.error("Failed to set value in target", e);
            throw new RuntimeException("Failed to set value in target", e);
        }
    }

    // Example usage
    public APIModel convertToApiModel(UIModel uiModel) {
        return (APIModel) mapObject(uiModel, APIModel.class);
    }
}

// 4. Usage Example
@RestController
@RequestMapping("/api")
public class ModelController {
    private final DynamicModelMapper mapper;

    public ModelController(DynamicModelMapper mapper) {
        this.mapper = mapper;
    }

    @PostMapping("/convert")
    public APIModel convertModel(@RequestBody UIModel uiModel) {
        return mapper.convertToApiModel(uiModel);
    }
}