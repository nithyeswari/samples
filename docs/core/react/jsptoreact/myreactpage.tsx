import React, { useState } from 'react';
import AjaxContent from './AjaxContent';

/**
 * This component demonstrates how to replace your JSP <choose> and <link>
 * functionality with React components that make the same Ajax calls
 */
const MyReactPage = () => {
  // State to track which link/option is selected
  const [selectedOption, setSelectedOption] = useState(null);
  
  // Define your options similar to what you had in your JSP <choose>
  const options = [
    { id: 'option1', label: 'Option 1', url: '/your-jsp-endpoint-1.jsp' },
    { id: 'option2', label: 'Option 2', url: '/your-jsp-endpoint-2.jsp' },
    { id: 'option3', label: 'Option 3', url: '/your-jsp-endpoint-3.jsp' }
  ];
  
  // Handle option selection
  const handleOptionClick = (option) => {
    setSelectedOption(option);
  };
  
  return (
    <div className="my-react-page">
      <h1>My React Application</h1>
      
      {/* Navigation links (replacing your JSP links) */}
      <div className="navigation-links">
        {options.map(option => (
          <button
            key={option.id}
            className={selectedOption?.id === option.id ? 'active' : ''}
            onClick={() => handleOptionClick(option)}
          >
            {option.label}
          </button>
        ))}
      </div>
      
      {/* Content area that loads via Ajax */}
      {selectedOption && (
        <AjaxContent 
          url={selectedOption.url}
          params={{ 
            /* Add any parameters needed for your Ajax calls */
            mode: 'ajax',
            id: selectedOption.id
          }}
        />
      )}
    </div>
  );
};

export default MyReactPage;