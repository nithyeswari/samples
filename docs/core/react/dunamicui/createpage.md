# DynamicUI: Simple Page Navigation

A streamlined approach to building dynamic interfaces with simple success-based navigation between pages.

## Overview

This guide focuses on a simplified pattern for DynamicUI that handles:
1. Dynamic component rendering on a single page
2. Navigation to another page upon success (form submission, action completion, etc.)

## Basic Implementation

### 1. Single Page Setup

Start with a single DynamicUI page:

```jsx
import React from 'react';
import { DynamicUI } from './DynamicUI';
import * as DesignSystem from './components/design-system';

const LoginPage = () => {
  // Component libraries
  const componentLibraries = {
    'core': DesignSystem
  };

  // Component mapping
  const componentMappingConfig = {
    'input': { libraryName: 'core', component: 'Input' },
    'button': { libraryName: 'core', component: 'Button' },
    'card': { libraryName: 'core', component: 'Card' }
  };

  // Navigation handler
  const handleLoginSuccess = () => {
    // Navigate to dashboard on success
    window.location.href = '/dashboard';
  };

  // Form submission handler
  const handleSubmit = (formData) => {
    // Process login
    loginUser(formData)
      .then(() => {
        handleLoginSuccess();
      })
      .catch(error => {
        console.error('Login failed:', error);
      });
  };

  return (
    <DynamicUI
      componentMappingConfig={componentMappingConfig}
      componentLibraries={componentLibraries}
      layout={{
        type: 'card',
        props: { className: 'p-4 max-w-md mx-auto mt-10' },
        children: [
          {
            type: 'text',
            props: { variant: 'h1', className: 'mb-4' },
            children: 'Login'
          },
          {
            type: 'form',
            props: { onSubmit: 'handleSubmit' },
            children: [
              {
                type: 'input',
                props: {
                  label: 'Email',
                  type: 'email',
                  name: 'email',
                  required: true
                }
              },
              {
                type: 'input',
                props: {
                  label: 'Password',
                  type: 'password',
                  name: 'password',
                  required: true
                }
              },
              {
                type: 'button',
                props: {
                  type: 'submit',
                  variant: 'primary',
                  className: 'mt-4'
                },
                children: 'Login'
              }
            ]
          }
        ]
      }}
      context={{
        handleSubmit
      }}
    />
  );
};

export default LoginPage;
```

### 2. Success Navigation Pattern

Define a simple navigation handling approach:

```jsx
// Navigation handler in your page component
const handleActionSuccess = (destination, params = {}) => {
  // Basic navigation
  if (typeof destination === 'string') {
    window.location.href = destination;
    return;
  }
  
  // Build URL with parameters
  if (destination.path && destination.params) {
    const url = buildUrlWithParams(destination.path, destination.params);
    window.location.href = url;
    return;
  }
  
  console.error('Invalid navigation destination', destination);
};

// Helper for building URLs with parameters
const buildUrlWithParams = (path, params) => {
  let url = path;
  
  // Replace path parameters (:id)
  Object.entries(params).forEach(([key, value]) => {
    url = url.replace(`:${key}`, value);
  });
  
  return url;
};
```

### 3. Context-based Navigation

Pass navigation functions through context:

```jsx
const ProductPage = () => {
  // ... component setup ...
  
  // Define success handlers
  const context = {
    // Form submission with redirect
    handleProductSubmit: (formData) => {
      saveProduct(formData)
        .then(product => {
          // Navigate to product detail on success
          window.location.href = `/products/${product.id}`;
        })
        .catch(error => {
          console.error('Save failed:', error);
        });
    },
    
    // Simple button action with redirect
    viewProductList: () => {
      window.location.href = '/products';
    }
  };
  
  return (
    <DynamicUI
      // ... configuration ...
      context={context}
    />
  );
};
```

## Example: Multi-step Form with Navigation

A common pattern is a multi-step form that navigates to another page on completion:

```jsx
const RegistrationPage = () => {
  const [formData, setFormData] = useState({});
  const [currentStep, setCurrentStep] = useState(1);
  
  // Navigation handler
  const completeRegistration = () => {
    window.location.href = '/welcome';
  };
  
  // Step handlers
  const context = {
    // Step 1 completion
    completeStep1: (data) => {
      setFormData(prev => ({ ...prev, ...data }));
      setCurrentStep(2);
    },
    
    // Step 2 completion
    completeStep2: (data) => {
      const completeData = { ...formData, ...data };
      
      // Submit all data
      registerUser(completeData)
        .then(() => {
          completeRegistration();
        })
        .catch(error => {
          console.error('Registration failed:', error);
        });
    },
    
    // Go back to previous step
    goBack: () => {
      setCurrentStep(1);
    }
  };
  
  // Select the appropriate layout based on current step
  const layout = currentStep === 1 
    ? stepOneLayout 
    : stepTwoLayout;
  
  return (
    <DynamicUI
      // ... configuration ...
      layout={layout}
      context={context}
    />
  );
};
```

## Integration with React Router

For more advanced navigation in a React application, integrate with React Router:

```jsx
import { useNavigate } from 'react-router-dom';

const ProductPage = () => {
  const navigate = useNavigate();
  
  // Use React Router's navigate function
  const handleSuccess = (productId) => {
    navigate(`/products/${productId}`);
  };
  
  const context = {
    saveProduct: (formData) => {
      createProduct(formData)
        .then(product => {
          handleSuccess(product.id);
        })
        .catch(error => {
          console.error('Failed to create product:', error);
        });
    }
  };
  
  return (
    <DynamicUI
      // ... configuration ...
      context={context}
    />
  );
};
```

## Best Practices

1. **Keep Navigation Logic Separate**
   - Define navigation handlers outside your JSON layout
   - Use context to connect actions to navigation

2. **Use Standard Navigation Patterns**
   - For simple apps, window.location.href is sufficient
   - For React apps, use React Router's navigation hooks

3. **Handle Navigation States**
   - Show loading indicators during async operations
   - Disable navigation triggers during processing

4. **Provide Feedback Before Navigation**
   - Show success messages or confirmations
   - Use timeouts to delay navigation for better UX

```jsx
const context = {
  submitForm: (data) => {
    setIsSubmitting(true);
    
    saveData(data)
      .then(() => {
        setSuccessMessage('Data saved successfully!');
        
        // Delay navigation for feedback
        setTimeout(() => {
          window.location.href = '/success';
        }, 1500);
      })
      .catch(error => {
        setErrorMessage('Failed to save data');
      })
      .finally(() => {
        setIsSubmitting(false);
      });
  }
};
```

## Summary

This simplified approach focuses on:
1. Building dynamic UI for individual pages
2. Handling success-based navigation between pages
3. Maintaining a clean separation between UI definition and navigation logic

By keeping the focus on single pages with simple navigation patterns, you can leverage the power of DynamicUI while avoiding the complexity of a full multi-page framework.