// ValidatorRegistry stores and manages all validator configurations
class ValidatorRegistry {
    constructor() {
        this.validators = new Map();
    }

    // Add a new validator configuration
    addValidator(validatorConfig) {
        const validator = new Validator(validatorConfig);
        this.validators.set(validatorConfig.name, validator);
    }

    // Get a validator by name
    getValidator(name) {
        return this.validators.get(name);
    }
}

// Validator class handles individual validator logic
class Validator {
    constructor(config) {
        this.name = config.name;
        this.rules = this.compileRules(config.rules);
    }

    // Compile rules into executable functions for efficiency
    compileRules(rules) {
        return rules.map(rule => {
            const validationFn = this.getValidationFunction(rule);
            return {
                type: rule.type,
                validate: validationFn,
                error: rule.error || this.getDefaultError(rule)
            };
        });
    }

    // Get the appropriate validation function based on rule type
    getValidationFunction(rule) {
        switch (rule.type) {
            case 'required':
                return (value) => value !== undefined && value !== null && value !== '';
                
            case 'string':
                return (value) => typeof value === 'string';
                
            case 'number':
                return (value) => typeof value === 'number' && !isNaN(value);
                
            case 'length':
                return (value) => {
                    if (typeof value !== 'string') return false;
                    const length = value.length;
                    return (!rule.min || length >= rule.min) && 
                           (!rule.max || length <= rule.max);
                };
                
            case 'range':
                return (value) => {
                    const num = Number(value);
                    return !isNaN(num) && 
                           (!rule.min || num >= rule.min) && 
                           (!rule.max || num <= rule.max);
                };
                
            case 'pattern':
                const regex = new RegExp(rule.pattern);
                return (value) => regex.test(String(value));
                
            case 'email':
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                return (value) => emailRegex.test(String(value));
                
            case 'enum':
                const validValues = new Set(rule.values);
                return (value) => validValues.has(value);
                
            default:
                return () => true;
        }
    }

    // Get default error message based on rule type
    getDefaultError(rule) {
        switch (rule.type) {
            case 'required': return 'Field is required';
            case 'string': return 'Must be a string';
            case 'number': return 'Must be a number';
            case 'length': return `Length must be between ${rule.min || 0} and ${rule.max || 'unlimited'}`;
            case 'range': return `Value must be between ${rule.min || '-∞'} and ${rule.max || '∞'}`;
            case 'pattern': return 'Invalid format';
            case 'email': return 'Invalid email format';
            case 'enum': return `Must be one of: ${rule.values.join(', ')}`;
            default: return 'Validation failed';
        }
    }

    // Validate a value against all rules
    validate(value) {
        const errors = [];

        for (const rule of this.rules) {
            if (!rule.validate(value)) {
                errors.push(rule.error);
            }
        }

        return {
            isValid: errors.length === 0,
            errors
        };
    }
}

// SchemaValidator handles validation of complete schemas
class SchemaValidator {
    constructor(registry) {
        this.registry = registry;
    }

    // Validate data against a schema
    validate(data, schema) {
        const errors = {};
        let hasErrors = false;

        for (const [field, validatorName] of Object.entries(schema)) {
            const validator = this.registry.getValidator(validatorName);
            if (!validator) {
                console.warn(`No validator found for: ${validatorName}`);
                continue;
            }

            const value = this.getNestedValue(data, field);
            const result = validator.validate(value);

            if (!result.isValid) {
                hasErrors = true;
                errors[field] = result.errors;
            }
        }

        return {
            isValid: !hasErrors,
            errors
        };
    }

    // Get nested value using dot notation
    getNestedValue(obj, path) {
        return path.split('.').reduce((current, part) => {
            return current && current[part];
        }, obj);
    }
}

// Example usage:

// 1. Create validator registry
const registry = new ValidatorRegistry();

// 2. Define validator configurations
const validatorConfigs = [
    {
        name: 'shortString',
        rules: [
            { type: 'required' },
            { type: 'string' },
            { type: 'length', min: 1, max: 10 }
        ]
    },
    {
        name: 'emailValidator',
        rules: [
            { type: 'required' },
            { type: 'email' }
        ]
    },
    {
        name: 'positiveNumber',
        rules: [
            { type: 'required' },
            { type: 'number' },
            { type: 'range', min: 0 }
        ]
    },
    {
        name: 'status',
        rules: [
            { type: 'required' },
            { type: 'enum', values: ['ACTIVE', 'INACTIVE', 'PENDING'] }
        ]
    }
];

// 3. Register validators
validatorConfigs.forEach(config => registry.addValidator(config));

// 4. Create schema validator
const schemaValidator = new SchemaValidator(registry);

// 5. Define validation schema
const schema = {
    'user.name': 'shortString',
    'user.email': 'emailValidator',
    'user.age': 'positiveNumber',
    'user.status': 'status'
};

// 6. Test data
const testData = {
    user: {
        name: 'John Doe', // Too long for shortString
        email: 'invalid-email', // Invalid email
        age: 25, // Valid
        status: 'ACTIVE' // Valid
    }
};

// 7. Validate
const result = schemaValidator.validate(testData, schema);
console.log(JSON.stringify(result, null, 2));

module.exports = {
    ValidatorRegistry,
    Validator,
    SchemaValidator
};
