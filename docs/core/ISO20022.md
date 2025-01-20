# ISO 20022 XML to React Component Converter

A tool to automatically convert ISO 20022 XML schemas into type-safe React components with validation.

## Features

- Converts ISO 20022 XML to React components
- Generates TypeScript interfaces
- Implements form validation using Zod
- Integrates with React Hook Form
- Supports nested components
- Preserves ISO 20022 validation rules

## Installation

```bash
npm install iso20022-react-converter
```

## Usage

### Command Line

```bash
npx iso20022-converter --input schema.xml --output ./components
```

### Programmatic Usage

```typescript
import { ISO20022Parser, ReactComponentGenerator } from 'iso20022-converter';

// Parse XML
const parser = new ISO20022Parser();
const component = parser.parseXML(xmlContent);

// Generate React component
const generator = new ReactComponentGenerator();
const reactComponent = generator.generateComponent(component);
```

## Example

### Input XML
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.03">
  <CstmrCdtTrfInitn>
    <GrpHdr>
      <MsgId maxLength="35" required="true">Message Identification</MsgId>
      <CreDtTm required="true">Creation Date Time</CreDtTm>
      <NbOfTxs required="true" pattern="\\d+">Number of Transactions</NbOfTxs>
      <CtrlSum required="true">Control Sum</CtrlSum>
    </GrpHdr>
  </CstmrCdtTrfInitn>
</Document>
```

### Generated Component
```tsx
import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

interface GroupHeaderProps {
  onSubmit: (data: GroupHeaderData) => void;
  initialData?: GroupHeaderData;
}

interface GroupHeaderData {
  msgId: string;
  creationDateTime: string;
  numberOfTransactions: number;
  controlSum: number;
}

const validationSchema = z.object({
  msgId: z.string().max(35),
  creationDateTime: z.string(),
  numberOfTransactions: z.number().regex(/\d+/),
  controlSum: z.number()
});

export const GroupHeader: React.FC<GroupHeaderProps> = ({
  onSubmit,
  initialData
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<GroupHeaderData>({
    resolver: zodResolver(validationSchema),
    defaultValues: initialData
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      {/* Form fields */}
    </form>
  );
};
```

## Configuration

### Parser Options

You can configure the XML parser with additional options:

```typescript
const parser = new ISO20022Parser({
  validateSchema: true,
  preserveComments: false,
  strict: true
});
```

### Generator Options

Customize the React component generation:

```typescript
const generator = new ReactComponentGenerator({
  styling: 'tailwind', // or 'material-ui', 'styled-components'
  validation: 'zod',   // or 'yup', 'joi'
  framework: 'next',   // or 'react', 'remix'
});
```

## Validation Rules

The tool preserves ISO 20022 validation rules:

- Required fields
- Length restrictions
- Pattern matching
- Type validation
- Business rules

## Error Handling

Generated components include error handling:

```tsx
{errors.fieldName && (
  <span className="error">{errors.fieldName.message}</span>
)}
```

## Styling

Components can be styled using:

- Tailwind CSS
- Material-UI
- Styled Components
- CSS Modules

## Type Safety

All generated components include:

- TypeScript interfaces
- Zod validation schemas
- Runtime type checking

## Contributing

1. Fork the repository
2. Create your feature branch
3. Run tests: `npm test`
4. Submit a pull request

## License

MIT License

## References

- [ISO 20022 Documentation](https://www.iso20022.org/)
- [React Documentation](https://react.dev/)
- [Zod Documentation](https://zod.dev/)
- [React Hook Form](https://react-hook-form.com/)
