import React, { useState } from 'react';

/**
 * AjaxForm component that submits form data via Ajax and renders the response
 * 
 * @param {string} url - The URL to submit the form to
 * @param {string} method - HTTP method (GET or POST)
 * @param {function} onSuccess - Callback after successful form submission
 */
const AjaxForm = ({ url, method = 'POST', onSuccess }) => {
  const [formData, setFormData] = useState({});
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Handle input changes
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prevData => ({
      ...prevData,
      [name]: value
    }));
  };
  
  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    
    try {
      // For GET requests with query parameters
      let fetchUrl = url;
      let fetchOptions = {
        method: method,
        headers: {
          'X-Requested-With': 'XMLHttpRequest'
        }
      };
      
      // If using POST, send form data in the request body
      if (method.toUpperCase() === 'POST') {
        fetchOptions.headers['Content-Type'] = 'application/x-www-form-urlencoded';
        fetchOptions.body = new URLSearchParams(formData).toString();
      } else if (method.toUpperCase() === 'GET') {
        // For GET, append form data as query parameters
        const queryParams = new URLSearchParams(formData).toString();
        fetchUrl = `${url}?${queryParams}`;
      }
      
      const response = await fetch(fetchUrl, fetchOptions);
      
      if (!response.ok) {
        throw new Error(`HTTP error ${response.status}`);
      }
      
      const responseText = await response.text();
      setResult(responseText);
      
      // Call the success callback if provided
      if (onSuccess && typeof onSuccess === 'function') {
        onSuccess(responseText);
      }
    } catch (err) {
      console.error('Form submission failed:', err);
      setError(`Failed to submit form: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="ajax-form-container">
      <form onSubmit={handleSubmit}>
        {/* Form fields would be added here according to your needs */}
        {/* Example field: */}
        <div className="form-field">
          <label htmlFor="exampleField">Example Field:</label>
          <input
            type="text"
            id="exampleField"
            name="exampleField"
            value={formData.exampleField || ''}
            onChange={handleInputChange}
          />
        </div>
        
        <button 
          type="submit" 
          disabled={loading}
          className="submit-button"
        >
          {loading ? 'Submitting...' : 'Submit'}
        </button>
      </form>
      
      {error && <div className="error-message">{error}</div>}
      
      {result && (
        <div className="result-container">
          <h3>Response:</h3>
          <div 
            className="response-content"
            dangerouslySetInnerHTML={{ __html: result }} 
          />
        </div>
      )}
    </div>
  );
};

export default AjaxForm;