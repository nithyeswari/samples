// services/iso20022RuleConverter.ts
interface ISO20022Field {
  name: string;
  type: string;
  maxLength?: number;
  minLength?: number;
  pattern?: string;
  required?: boolean;
  options?: string[];
  dependencies?: any[];
}

interface ISO20022Message {
  messageId: string;
  fields: ISO20022Field[];
  rules: any[];
}

class ISO20022RuleConverter {
  convertToFormRules(message: ISO20022Message): SectionRule[] {
    return [
      {
        id: message.messageId,
        title: this.formatTitle(message.messageId),
        fields: message.fields.map(field => this.convertField(field))
      }
    ];
  }

  private convertField(field: ISO20022Field): FieldRule {
    const validations: ValidationRule[] = [];
    
    if (field.required) {
      validations.push({
        type: 'required',
        message: `${field.name} is required`
      });
    }

    if (field.pattern) {
      validations.push({
        type: 'pattern',
        value: new RegExp(field.pattern),
        message: `${field.name} format is invalid`
      });
    }

    if (field.minLength) {
      validations.push({
        type: 'minLength',
        value: field.minLength,
        message: `Minimum length is ${field.minLength}`
      });
    }

    if (field.maxLength) {
      validations.push({
        type: 'maxLength',
        value: field.maxLength,
        message: `Maximum length is ${field.maxLength}`
      });
    }

    return {
      id: field.name,
      type: this.mapFieldType(field.type),
      label: this.formatLabel(field.name),
      validations,
      options: field.options?.map(opt => ({
        value: opt,
        label: this.formatLabel(opt)
      })),
      dependencies: field.dependencies?.map(dep => ({
        field: dep.field,
        value: dep.value,
        action: dep.action
      }))
    };
  }

  private mapFieldType(isoType: string): FieldRule['type'] {
    const typeMap: Record<string, FieldRule['type']> = {
      'Text': 'text',
      'Number': 'number',
      'Date': 'date',
      'Code': 'select',
      'Binary': 'custom'
    };
    return typeMap[isoType] || 'text';
  }

  private formatLabel(name: string): string {
    return name
      .split(/(?=[A-Z])/)
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  private formatTitle(messageId: string): string {
    return messageId
      .split('.')
      .map(part => this.formatLabel(part))
      .join(' - ');
  }
}

// hooks/useISO20022Form.ts
export const useISO20022Form = (messageId: string) => {
  const [rules, setRules] = useState<SectionRule[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAndConvertRules = async () => {
      try {
        setLoading(true);
        
        // Fetch ISO 20022 rules from API
        const response = await fetch(`/api/iso20022/messages/${messageId}`);
        const message: ISO20022Message = await response.json();

        // Convert to form rules
        const converter = new ISO20022RuleConverter();
        const formRules = converter.convertToFormRules(message);
        
        setRules(formRules);
      } catch (error) {
        console.error('Failed to fetch ISO 20022 rules:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchAndConvertRules();
  }, [messageId]);

  return { rules, loading };
};

// components/ISO20022Form.tsx
interface ISO20022FormProps {
  messageId: string;
  onSubmit: (data: any) => void;
  initialData?: any;
}

export const ISO20022Form: React.FC<ISO20022FormProps> = ({
  messageId,
  onSubmit,
  initialData
}) => {
  const { rules, loading } = useISO20022Form(messageId);

  if (loading) {
    return <div>Loading ISO 20022 form rules...</div>;
  }

  return (
    <DynamicForm
      rules={rules}
      onSubmit={onSubmit}
      initialData={initialData}
    />
  );
};

// Example usage
// App.tsx
const App = () => {
  const handleSubmit = async (data: any) => {
    try {
      const response = await fetch('/api/messages', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
      });
      
      if (!response.ok) {
        throw new Error('Failed to submit message');
      }

      console.log('Message submitted successfully');
    } catch (error) {
      console.error('Error submitting message:', error);
    }
  };

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">ISO 20022 Message Form
