# DynamicPage

A powerful React component for building dynamic forms with API integration, field validation, and conditional rendering.

## Features

- **Dynamic Field Rendering**: Generate forms from JSON configuration
- **API Integration**: Built-in support for loading data, validation, and submission
- **Form Validation**: Client and server-side validation
- **Conditional Fields**: Show/hide fields based on form values
- **Loading States**: Automatic handling of loading and submission states
- **Error Handling**: Comprehensive error handling for API calls

## Installation

```bash
npm install dynamic-page-react
# or
yarn add dynamic-page-react
```

## Basic Usage

```jsx
import React from 'react';
import DynamicPage from 'dynamic-page-react';
import { TextField, EmailField, SelectField } from './FormComponents';

const UserProfilePage = () => {
  // Define component mapping
  const componentMapping = {
    text: TextField,
    email: EmailField,
    select: SelectField,
  };
  
  // Define page configuration
  const pageConfig = {
    title: 'User Profile',
    submitLabel: 'Update Profile',
    
    // Define fields
    fields: [
      {
        type: 'text',
        name: 'firstName',
        label: 'First Name',
        required: true
      },
      {
        type: 'email',
        name: 'email',
        label: 'Email Address',
        required: true
      }
    ],
    
    // API configuration
    api: {
      onLoad: () => fetch('/api/user').then(res => res.json()),
      validate: (data) => fetch('/api/validate', {
        method: 'POST',
        body: JSON.stringify(data)
      }).then(res => res.json()),
      submit: (data) => fetch('/api/user', {
        method: 'PUT',
        body: JSON.stringify(data)
      }).then(res => res.json())
    }
  };
  
  return (
    <DynamicPage
      config={pageConfig}
      componentMapping={componentMapping}
      onSuccess={(response) => console.log('Success:', response)}
      onError={(error) => console.error('Error:', error)}
    />
  );
};
```

## Configuration

### Page Configuration

The `config` prop accepts an object with the following properties:

| Property | Type | Description |
|----------|------|-------------|
| `title` | string | The title of the page |
| `submitLabel` | string | Label for the submit button |
| `cancelLabel` | string | Label for the cancel button (optional) |
| `onCancel` | function | Callback for cancel button click |
| `fields` | array | Array of field configurations |
| `api` | object | API configuration for loading, validation, and submission |

### Field Configuration

Each field in the `fields` array accepts the following properties:

| Property | Type | Description |
|----------|------|-------------|
| `type` | string | The type of field (must match a key in componentMapping) |
| `name` | string | The name of the field (used as form value key) |
| `label` | string | Label displayed above the field |
| `required` | boolean | Whether the field is required |
| `placeholder` | string | Placeholder text for input fields |
| `condition` | function | Function that returns boolean to determine if field should be displayed |
| `...props` | any | Additional props passed to the field component |

### API Configuration

The `api` object accepts the following properties:

| Property | Type | Description |
|----------|------|-------------|
| `onLoad` | function | Function called when the page loads, should return a Promise |
| `validate` | function | Function called to validate form data, should return a Promise that resolves to `{errors}` or `{valid: true}` |
| `submit` | function | Function called when the form is submitted, should return a Promise |

## Loading Data

When the page loads, `api.onLoad()` is called to fetch initial data. This should return a Promise that resolves to an object with the following structure:

```js
{
  formValues: {
    // Initial values for form fields
    firstName: 'John',
    email: 'john@example.com'
  },
  additionalData: {
    // Optional additional data
    userId: '123',
    createdAt: '2023-01-15'
  }
}
```

## Validation

When the form is submitted, `api.validate(formValues)` is called to validate the form data. This should return a Promise that resolves to one of:

```js
// Validation errors
{
  errors: {
    firstName: 'First name is required',
    email: 'Invalid email address'
  }
}

// Valid form
{
  valid: true
}
```

## Submission

After validation succeeds, `api.submit(formValues)` is called to submit the form data. This should return a Promise that resolves to a response object.

## Success and Error Handling

The `onSuccess` and `onError` props are called when the form submission succeeds or fails:

```jsx
<DynamicPage
  // ...other props
  onSuccess={(response) => {
    // Handle successful submission
    alert('Form submitted successfully!');
    navigate('/dashboard');
  }}
  onError={(error) => {
    // Handle error
    console.error('Submission failed:', error);
  }}
/>
```

## Conditional Fields

You can conditionally render fields based on the values of other fields:

```jsx
const fields = [
  {
    type: 'select',
    name: 'userType',
    label: 'User Type',
    options: [
      { value: 'personal', label: 'Personal' },
      { value: 'business', label: 'Business' }
    ]
  },
  {
    type: 'text',
    name: 'companyName',
    label: 'Company Name',
    condition: (formValues) => formValues.userType === 'business'
  }
];
```

## Integrating with Form Components

DynamicPage works with any form components that accept standard props:

```jsx
// Example TextField component
const TextField = ({ 
  name, 
  label, 
  value, 
  onChange, 
  error,
  required,
  ...props 
}) => (
  <div>
    <label>{label} {required && '*'}</label>
    <input
      name={name}
      value={value}
      onChange={onChange}
      {...props}
    />
    {error && <div className="error">{error}</div>}
  </div>
);
```

## Advanced Example

Here's an example of a more complex form with conditional fields and API integration:

```jsx
const productConfig = {
  title: 'Product Configuration',
  submitLabel: 'Save Product',
  fields: [
    {
      type: 'select',
      name: 'productType',
      label: 'Product Type',
      required: true,
      options: [
        { value: 'physical', label: 'Physical Product' },
        { value: 'digital', label: 'Digital Product' }
      ]
    },
    {
      type: 'text',
      name: 'productName',
      label: 'Product Name',
      required: true
    },
    {
      type: 'number',
      name: 'weight',
      label: 'Weight (kg)',
      condition: (formValues) => formValues.productType === 'physical'
    },
    {
      type: 'select',
      name: 'shippingMethod',
      label: 'Shipping Method',
      condition: (formValues) => formValues.productType === 'physical',
      options: [
        { value: 'standard', label: 'Standard Shipping' },
        { value: 'express', label: 'Express Shipping' }
      ]
    },
    {
      type: 'text',
      name: 'downloadLink',
      label: 'Download Link',
      condition: (formValues) => formValues.productType === 'digital'
    }
  ],
  api: {
    onLoad: (productId) => productService.getProduct(productId),
    validate: productService.validateProduct,
    submit: productService.saveProduct
  }
};
```

## License

MIT