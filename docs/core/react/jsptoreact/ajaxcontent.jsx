import React, { useState, useEffect } from 'react';

/**
 * AjaxContent component that makes Ajax calls to the same endpoints as your JSP application
 * and renders the returned HTML content
 * 
 * @param {string} url - The URL to make the Ajax request to
 * @param {object} params - Query parameters to include in the request
 * @param {boolean} loadOnMount - Whether to load the content when the component mounts
 */
const AjaxContent = ({ url, params = {}, loadOnMount = true }) => {
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Function to load content via Ajax
  const loadContent = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Build query string from params
      const queryString = Object.keys(params)
        .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
        .join('&');
      
      const fullUrl = queryString ? `${url}?${queryString}` : url;
      
      const response = await fetch(fullUrl, {
        method: 'GET',
        headers: {
          'X-Requested-With': 'XMLHttpRequest'
        }
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error ${response.status}`);
      }
      
      const htmlContent = await response.text();
      setContent(htmlContent);
    } catch (err) {
      console.error('Ajax request failed:', err);
      setError(`Failed to load content: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };
  
  // Load content on mount if specified
  useEffect(() => {
    if (loadOnMount) {
      loadContent();
    }
  }, [url, JSON.stringify(params)]); // Reload when URL or params change
  
  // Render HTML content using dangerouslySetInnerHTML
  return (
    <div className="ajax-content-container">
      {loading && <div className="loading-indicator">Loading...</div>}
      
      {error && <div className="error-message">{error}</div>}
      
      {!loading && !error && (
        <div 
          className="ajax-content"
          dangerouslySetInnerHTML={{ __html: content }} 
        />
      )}
      
      <button 
        onClick={loadContent} 
        disabled={loading}
        className="reload-button"
      >
        Reload Content
      </button>
    </div>
  );
};

export default AjaxContent;